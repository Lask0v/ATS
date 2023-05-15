package queryprocessor.preprocessor;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.preprocessor.extractors.ConditionExtractor;
import queryprocessor.preprocessor.extractors.PatternExtractor;
import queryprocessor.preprocessor.extractors.RelationshipExtractor;
import queryprocessor.preprocessor.extractors.ResultSynonymExtractor;
import queryprocessor.preprocessor.synonyms.*;
import queryprocessor.preprocessor.validators.Validator;
import queryprocessor.preprocessor.validators.ValidatorFactory;
import queryprocessor.querytree.*;

public class QueryPreprocessorBase implements QueryPreprocessor
{
    private final HashSet<Synonym<?>> declaredSynonyms = new HashSet<>();
    public final static HashSet<Keyword> synonymsKeywords = new HashSet<>(
            Arrays.asList(
                    Keyword.WHILE,
                    Keyword.ASSIGN,
                    Keyword.STATEMENT,
                    Keyword.IF,
                    Keyword.CALL,
                    Keyword.PROG_LINE,
                    Keyword.PROCEDURE,
                    Keyword.CONSTANT,
                    Keyword.VARIABLE)
    );
    public final static HashSet<Keyword> relationshipsKeywords = new HashSet<>(
            Arrays.asList(
                    Keyword.FOLLOWS, Keyword.T_FOLLOWS,
                    Keyword.PARENT, Keyword.T_PARENT,
                    Keyword.MODIFIES, Keyword.T_MODIFIES,
                    Keyword.CALLS, Keyword.T_CALLS,
                    Keyword.USES, Keyword.T_USES,
                    Keyword.NEXT, Keyword.T_NEXT,
                    Keyword.AFFECTS, Keyword.T_AFFECTS)
    );

    private final static String querySeparator = ";";

    @Override
    public QueryTree parseQuery(String query) throws InvalidQueryException, MissingArgumentException
    {
        if (query.length() == 0)
            throw new InvalidQueryException("Empty query");

        String[] array = query.split(querySeparator); // positive look behind - query.split("((?<=;))");

        return parseQuery(array);
    }

    @Override
    public QueryTree parseQuery(String[] queryLines) throws InvalidQueryException, MissingArgumentException
    {
        if (queryLines.length == 0)
            throw new InvalidQueryException("The query not properly ended. "+querySeparator+" is missing", 1);

        ParsingProgress progress = null;
        QTree tree = new QTree();
        declaredSynonyms.clear();

        boolean selectClauseExists = false;
        for (int i = 0; i < queryLines.length; i++)
        {
            String line = queryLines[i];
            if (line.length() == 0)
                continue;

            // Check if the current line is a synonym declaration or the query itself (contains a select clause)
            if (containsClause(Keyword.SELECT, line, progress)) {
                if (selectClauseExists)
                    throw new InvalidQueryException("Multiple select clauses", i, line);
                else
                    selectClauseExists = true;
            }

            // Synonyms declarations
            if (!selectClauseExists)
                parseSynonymDeclarations(line, i);
            else
            {
                progress = new ParsingProgress(line);
                Matcher matcher = Pattern.compile(Keyword.SELECT.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(line);
                var match = matcher.results().findFirst().get();
                progress.setParsed(match.start(), match.end());

                //region SELECT CLAUSE
                var rse = new ResultSynonymExtractor(this, progress);
                var resultSynonyms = rse.extractSynonyms(line);

                if(resultSynonyms.isEmpty())
                    throw new InvalidQueryException("Empty select results. You need to specify the result or result's tuple in the select clause", i, line);

                tree.createResultsNode();

                for (var res: resultSynonyms) {
                    tree.addResNode(res);
                }

                //endregion

                // region SUCH THAT
                if(containsClause(Keyword.SUCH_THAT, line, progress))
                {
                    RelationshipExtractor re = new RelationshipExtractor(this, progress);
                    List<RelationshipRef> relationships = re.extractRelationships(line);

                    tree.createSuchThatNode();

                    if(relationships.isEmpty())
                        throw new InvalidQueryException("Invalid 'such that' clause", i, line);

                    for (RelationshipRef rel: relationships) {
                        Validator validator = ValidatorFactory.createRelationshipValidator(rel);

                        if(!validator.isValid())
                            throw new InvalidQueryException(validator.getErrorMsg(), i, rel.getLabel());

                        tree.addRelationshipNode(rel);
                    }
                }
                //endregion

                //region WITH
                if(containsClause(Keyword.WITH, line, progress))
                {
                    ConditionExtractor ce = new ConditionExtractor(this, progress);
                    List<Condition> conditions = ce.extractConditions(line);

                    tree.createWithNode();

                    if(conditions.isEmpty())
                        throw new InvalidQueryException("Invalid 'with' clause", i, line);

                    for (Condition cond: conditions) {
                        Validator validator = ValidatorFactory.createConditionValidator(cond);

                        if(!validator.isValid())
                            throw new InvalidQueryException(validator.getErrorMsg(), i, line);

                        tree.addConditionNode(cond);
                    }
                }
                //endregion

                // region PATTERN
                if(containsClause(Keyword.PATTERN, line, progress))
                {
                    PatternExtractor pe = new PatternExtractor(this, progress);
                    List<ExpressionPattern> patterns = pe.extractPatterns(line);

                    tree.createPatterNode();

                    for (ExpressionPattern pattern: patterns) {
                        Validator validator = ValidatorFactory.createPatternValidator(pattern);
                        if(!validator.isValid())
                            throw new InvalidQueryException(validator.getErrorMsg(), i, line);

                        tree.addPatternNode(pattern);
                    }
                }
                //endregion
            }
        }

        if (!selectClauseExists) {
            throw new InvalidQueryException("No select clause was found");
        }

        if(!progress.isCompleted())
            throw new InvalidQueryException("Failed to parse the query. Please check the syntax.", queryLines[queryLines.length-1]);

        return tree;
    }

    private void parseSynonymDeclarations(String line, int lineNumber) throws InvalidQueryException
    {
        Keyword type = null;
        int start = 0;
        int cnt = 0;

        for (Keyword keyword : synonymsKeywords) {
            Matcher matcher = Pattern.compile(Pattern.quote(keyword.getRegExpr()),
                    Pattern.CASE_INSENSITIVE).matcher(line);

            if (matcher.find()) {
                type = keyword;
                start = matcher.end();
                cnt++;
            }
        }

        if (cnt > 1)
            throw new InvalidQueryException("Multiple synonym types in one declaration", lineNumber, line);

        if (type == null)
            throw new InvalidQueryException("Unrecognized synonym definition", lineNumber, line);

        var synonyms = Pattern.compile(Keyword.SYNONYM.getRegExpr())
                .matcher(line)
                .region(start, line.length())
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toList());

        if (synonyms.isEmpty())
            throw new InvalidQueryException("Missing synonym identifier", lineNumber, line);

        for (String synonym : synonyms) {
            var s = SynonymFactory.create(synonym, type);
            if (declaredSynonyms.contains(s)) {
                throw new InvalidQueryException("Synonym already declared", lineNumber, synonym);
            } else {
                declaredSynonyms.add(s);
            }
        }
    }

    private boolean containsClause(Keyword clause, String query, ParsingProgress parsingProgress) {
        Matcher matcher = Pattern.compile(clause.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
        boolean res = matcher.find();
        if(res && parsingProgress != null)
            parsingProgress.setParsed(matcher.toMatchResult().start(), matcher.toMatchResult().end());

        return res;
    }

    @Override
    public Synonym<?> getDeclaredSynonym(String id) {
        Optional<Synonym<?>> f = this.declaredSynonyms.stream().filter(s -> s.getIdentifier().equals(id)).findFirst();

        return f.orElse(null);
    }
}
