package queryprocessor.preprocessor.extractors;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.ParsingProgress;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Extractor
{
    protected ParsingProgress parsingProgress;

    public Extractor(ParsingProgress parsingProgress) {
        this.parsingProgress = parsingProgress;
    }

    protected List<Pair<Integer, Integer>> extractRegions(String query, Keyword searchedClause) {
        ArrayList<Pair<Integer, Integer>> regions = new ArrayList<Pair<Integer, Integer>>();

        Matcher clauseMatcher = Pattern.compile(searchedClause.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(query);
        var clasues = clauseMatcher.results().collect(Collectors.toList());

        for (var clause: clasues) {
            parsingProgress.setParsed(clause.start(), clause.end());

            var matcher = Pattern.compile(
                    String.format("%s|%s|%s",
                            Keyword.SUCH_THAT.getRegExpr(),
                            Keyword.WITH.getRegExpr(),
                            Keyword.PATTERN.getRegExpr()),
                    Pattern.CASE_INSENSITIVE)
                    .matcher(query)
                    .region(clause.end(), query.length());

            var firstGroup = matcher.results().findFirst();

            var endPos = query.length();
            if(firstGroup.isPresent())
                endPos = firstGroup.get().start();

            regions.add(new Pair<>(clause.start(), endPos));
        }

        return regions;
    }

    protected long getConcatenatorCount(String line, int start, int end) {
        Matcher matcher = Pattern.compile(Keyword.AND.getRegExpr(), Pattern.CASE_INSENSITIVE).matcher(line).region(start, end);
        var results = matcher.results().collect(Collectors.toList());

        if (!results.isEmpty())
            results.forEach(matchResult -> parsingProgress.setParsed(matchResult.start(), matchResult.end()));

        return results.size();
    }
}
