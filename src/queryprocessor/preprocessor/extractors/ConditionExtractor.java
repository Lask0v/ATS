package queryprocessor.preprocessor.extractors;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.ParsingProgress;
import queryprocessor.preprocessor.QueryPreprocessor;
import queryprocessor.preprocessor.exceptions.InvalidQueryException;
import queryprocessor.preprocessor.synonyms.Synonym;
import queryprocessor.querytree.*;
import utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConditionExtractor extends Extractor {
    private final QueryPreprocessor queryPreprocessor;
    private final ParsingProgress parsingProgress;

    public ConditionExtractor(QueryPreprocessor queryPreprocessor, ParsingProgress parsingProgress) {
        super(parsingProgress);
        this.queryPreprocessor = queryPreprocessor;
        this.parsingProgress = parsingProgress;
    }

    public List<Condition> extractConditions(String query) throws InvalidQueryException {
        ArrayList<Condition> conditions = new ArrayList<Condition>();

        List<Pair<Integer, Integer>> regions = extractRegions(query, Keyword.WITH);
        for (Pair<Integer, Integer> region: regions) {
            Matcher matcher = Pattern.compile(Keyword.WITH_CLAUSE.getRegExpr(), Pattern.CASE_INSENSITIVE)
                    .matcher(query)
                    .region(region.getFirst(), region.getSecond());
            var matchResults = matcher.results().collect(Collectors.toList());

            for (var match : matchResults) {
                var result = match.group();
                conditions.addAll(extractAttributes(result));
                parsingProgress.setParsed(match.start(), match.end());
            }

            if((matchResults.size()-1) != getConcatenatorCount(query, region.getFirst(), region.getSecond()))
                throw new InvalidQueryException("Missing 'and' concatenator or missing condition");
        }

        return conditions;
    }

    public List<Condition> extractAttributes(String group) throws InvalidQueryException {
        List<String> conditionPart = Arrays.stream(group.split("=")).map(String::trim).collect(Collectors.toList());
        String part0 = conditionPart.get(0);
        part0 = ' ' + part0;
        part0 = part0.replaceAll(String.format("%s|%s", Keyword.WITH.getRegExpr(), Keyword.AND.getRegExpr()), "");
        conditionPart.set(0, part0.trim());

        List<Condition> conditionNodes = new ArrayList<>();
        List<Pair<Synonym<?>, AttrName>> refs = new ArrayList<>();

        for (String c : conditionPart) {
            if (!c.contains("."))
                continue;

            List<String> attrs = Arrays.stream(c.split("\\.")).map(String::trim).collect(Collectors.toList());

            Synonym<?> synonym = queryPreprocessor.getDeclaredSynonym(attrs.get(0));
            AttrName attr = findAttrName(attrs.get(1));

            if (synonym == null)
                throw new InvalidQueryException(String.format("Unrecognized synonym %s", attrs.get(0)), group);

            if (attr == null)
                throw new InvalidQueryException(String.format("Unrecognized attribute %s", attrs.get(1)), group);

            refs.add(new Pair<>(synonym, attr));
        }

        if (refs.size() == 1) {
            String value = conditionPart.get(1);

            if (value.contains("\""))
                value = value.replaceAll("\"", "");

            Pair<Synonym<?>, AttrName> pair = refs.get(0);
            AttrRef attrRef = new AttrRef(pair.getFirst(), pair.getSecond());
            AttrValue attrVal = new AttrValue(value);
            conditionNodes.add(new ConditionRefValue(attrRef, attrVal));
        } else {
            AttrRef ref1 = new AttrRef(refs.get(0).getFirst(), refs.get(0).getSecond());
            AttrRef ref2 = new AttrRef(refs.get(1).getFirst(), refs.get(1).getSecond());
            conditionNodes.add(new ConditionRefRef(ref1, ref2));
        }

        return conditionNodes;
    }

    public static AttrName findAttrName(String name) {
        for (AttrName attr: AttrName.values()) {
            if(name.equals(attr.getName()))
                return attr;
        }

        return null;
    }
}
