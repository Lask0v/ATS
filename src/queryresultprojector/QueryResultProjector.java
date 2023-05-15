package queryresultprojector;

import pkb.ast.abstraction.ASTNode;
import queryprocessor.evaluator.EvaluationResult;
import queryprocessor.evaluator.PartialResult;
import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.synonyms.Synonym;
import utils.Pair;

import java.util.*;
import java.util.function.Function;

public class QueryResultProjector
{
    private final static String NoResultMsg = "none";
    private EvaluationResult evaluationResult;

    public void setEvaluationResult(EvaluationResult result) {
        this.evaluationResult = result;
    }

    public String format() {
        if(evaluationResult == null || evaluationResult.getPartialResults().isEmpty()) {
            return NoResultMsg;
        }

        StringBuilder builder = new StringBuilder();
        Map<Synonym<?>, java.util.function.Function<ASTNode, String>> extractorMap = evaluationResult.getExtractors();
        ArrayList<Synonym<?>> synonyms = new ArrayList<>(extractorMap.keySet());

        List<PartialResult> partialResults = evaluationResult.getPartialResults();
        LinkedHashMap<Synonym<?>, PartialResult> map  = new LinkedHashMap<Synonym<?>, PartialResult>();

        HashMap<Pair<Synonym<?>, Synonym<?>>, Set<Pair<ASTNode, ASTNode>>> relationshipsForKey = new HashMap<Pair<Synonym<?>, Synonym<?>>, Set<Pair<ASTNode, ASTNode>>>();

        for (Synonym<?> synonym: synonyms) {
            for (PartialResult pr: partialResults) {
                if(pr.containsKey(synonym))
                    map.putIfAbsent(synonym, pr);

                Pair<Synonym<?>, Synonym<?>> pair = pr.getKeyPair();
                if(pair != null) {
                    relationshipsForKey.putIfAbsent(pair, (Set<Pair<ASTNode, ASTNode>>) pr.getValue());
                }
            }
        }

        if(synonyms.stream().anyMatch(s -> s.getKeyword().equals(Keyword.BOOLEAN))) {
            boolean empty = partialResults.stream().anyMatch(pr -> pr.getValue().isEmpty()) || partialResults.isEmpty();
            if(empty)
                builder.append("false");
            else
                builder.append("true");

            return builder.toString();
        }

        List<List<ASTNode>> results = new ArrayList<>();

        List<List<ASTNode>> source = new ArrayList<>();
        for (Map.Entry<Synonym<?>, PartialResult> entry: map.entrySet()) {
            ArrayList<ASTNode> values = new ArrayList<>(entry.getValue().getValue(entry.getKey()));
            source.add(values);
        }

        results = recursiveSetCombination(results, source, 0);

        results = new ArrayList<>(new HashSet<>(results));

        boolean[] synonymsInRel = new boolean[synonyms.size()];
        boolean[] synonymsChecked = new boolean[synonyms.size()];

        boolean relExists = false;
        for (int i = 0; i < synonyms.size(); i++) {
            Synonym<?> synonym = synonyms.get(i);
            if(relationshipsForKey.containsKey(synonym)) {
                synonymsInRel[i] = true;
                relExists = true;
            }
        }

        List<List<ASTNode>> filtered2 = relExists ? new ArrayList<>() : results;
        boolean[] resultValidity = new boolean[results.size()];

        for (int i = 0; i < results.size(); i++)
        {
            List<ASTNode> result = results.get(i);
            int valid = 0;
            for (Map.Entry<Pair<Synonym<?>, Synonym<?>>, Set<Pair<ASTNode, ASTNode>>> entry : relationshipsForKey.entrySet()) {
                Pair<Synonym<?>, Synonym<?>> pair = entry.getKey();

                Synonym<?> synonym = pair.getFirst();
                int synonymPos = synonyms.indexOf(synonym);
                Set<Pair<ASTNode, ASTNode>> value = entry.getValue();
                Synonym<?> secondSynonym = pair.getSecond();
                int secondSynonymPos = synonyms.indexOf(secondSynonym);

               if(secondSynonymPos == -1 || synonymPos == -1) {
                    resultValidity[i] = true;
                    continue;
                }

                //synonymsChecked[synonymPos] = true;
                //synonymsChecked[secondSynonymPos] = true;

                ASTNode node1 = result.get(synonymPos);
                ASTNode node2 = result.get(secondSynonymPos);

                if (value.contains(new Pair<>(node1, node2)))
                    valid++;
            }

            if(valid == relationshipsForKey.keySet().size())
                resultValidity[i] = true;
        }

        results = filtered2;

        //builder.append(String.format("%d result(s): \n", results.size()));

        ArrayList<Function<ASTNode, String>> extractors = new ArrayList<>(extractorMap.values());
        int printed = 0;
        for(int p = 0; p < results.size(); p++)
        {
            if(!resultValidity[p])
                continue;

            if(printed > 0)
                builder.append(", ");

            List<ASTNode> list = results.get(p);

            for(int i = 0; i < list.size(); i++)
            {
                assert (extractors.size() == list.size());
                Function<ASTNode, String> extractor = extractors.get(i);
                ASTNode node = list.get(i);
                builder.append(extractor.apply(node));

                if(i < list.size()-1)
                    builder.append(" ");
            }

            printed++;
        }

        return builder.toString();
    }

    List<List<ASTNode>> recursiveSetCombination(List<List<ASTNode>> dest, List<List<ASTNode>> source, int step) {
        if(step == source.size())
            return dest;

        if(step == 0) {
            for(int i = 0; i < source.get(step).size(); i++)
                dest.add(Collections.singletonList(source.get(step).get(i)));

            return recursiveSetCombination(dest, source, step+1);
        }
        else {
            ArrayList<List<ASTNode>> newResult = new ArrayList<>();

            for (List<ASTNode> res: dest) {
                for (ASTNode s: source.get(step)) {
                    ArrayList<ASTNode> newList = new ArrayList<ASTNode>(res.size());
                    newList.addAll(res);
                    newList.add(s);
                    newResult.add(newList);
                }
            }

            return recursiveSetCombination(newResult, source, step+1);
        }
    }
}
