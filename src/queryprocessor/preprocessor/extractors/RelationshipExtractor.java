package queryprocessor.preprocessor.extractors;

import queryprocessor.preprocessor.ArgumentTypeDeducer;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.ParsingProgress;
import queryprocessor.preprocessor.QueryPreprocessorBase;
import queryprocessor.preprocessor.QueryPreprocessor;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.exceptions.MissingArgumentException;
import queryprocessor.preprocessor.synonyms.StatementIdSynonym;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.querytree.RelationshipRef;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RelationshipExtractor extends Extractor {
    private final QueryPreprocessor queryPreprocessor;
    private final ParsingProgress parsingProgress;

    public RelationshipExtractor(QueryPreprocessor queryPreprocessor, ParsingProgress parsingProgress) {
        super(parsingProgress);
        this.queryPreprocessor = queryPreprocessor;
        this.parsingProgress = parsingProgress;
    }

    public List<RelationshipRef> extractRelationships(String query) throws InvalidQueryException, MissingArgumentException {
        ArrayList<RelationshipRef> relationships = new ArrayList<RelationshipRef>();

        List<utils.Pair<Integer, Integer>> regions = extractRegions(query, Keyword.SUCH_THAT);
        for (utils.Pair<Integer, Integer> region: regions)
        {
            int relationshipCount = 0;
            for (Keyword rel : QueryPreprocessorBase.relationshipsKeywords) {
                Matcher relMatcher = Pattern.compile(rel.getRegExpr(), Pattern.CASE_INSENSITIVE)
                        .matcher(query)
                        .region(region.getFirst(), region.getSecond());
                var matchResults = relMatcher.results().collect(Collectors.toList());

                if (matchResults.isEmpty())
                    continue;

                for (var match : matchResults) {
                    var args = extractArguments(query, rel, match.start(), match.end());
                    relationships.add(new RelationshipRef(rel, args));
                    parsingProgress.setParsed(match.start(), match.end());
                    relationshipCount++;
                }
            }

            if ((relationshipCount - 1) != getConcatenatorCount(query, region.getFirst(), region.getSecond()))
                throw new InvalidQueryException("Missing 'and' concatenator or missing relationships.", query.substring(region.getFirst(), region.getSecond()));
        }

        return relationships;
    }

    public List<Synonym<?>> extractArguments(String query, Keyword relType, int start, int end) throws MissingArgumentException, InvalidQueryException {
        Matcher argsMatcher = Pattern.compile(Keyword.REL_ARGS.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query).region(start, end);

        if (!argsMatcher.find())
            throw new MissingArgumentException(relType.getName(), 0, query);

        String[] args = argsMatcher.group().split(",");

        ArrayList<Synonym<?>> arguments = new ArrayList<Synonym<?>>();
        int argN = 0;
        for (String arg : args) {
            argN++;
            arg = arg.trim();

            Synonym<?> synonym;
            if (arg.contains("\"") || arg.equals(Keyword.PLACEHOLDER.getRegExpr())) {
                ArgumentTypeDeducer td = new ArgumentTypeDeducer();
                arg = arg.replaceAll("\"", "").trim();
                synonym = td.deduce(relType, arg, argN);
            } else if (isNumeric(arg))
                synonym = new StatementIdSynonym(arg);
            else
                synonym = queryPreprocessor.getDeclaredSynonym(arg.trim());

            if (synonym == null)
                throw new InvalidQueryException("Unrecognized parameter synonym", arg);

            arguments.add(synonym);
        }

        return arguments;
    }

    private boolean isNumeric(String strNum) {
        final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null)
            return false;

        return pattern.matcher(strNum).matches();
    }
}
