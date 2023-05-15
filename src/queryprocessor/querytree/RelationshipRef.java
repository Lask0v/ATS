package queryprocessor.querytree;

import queryprocessor.preprocessor.Keyword;
import queryprocessor.preprocessor.synonyms.NamedSynonym;
import queryprocessor.preprocessor.synonyms.NamedVariableSynonym;
import queryprocessor.preprocessor.synonyms.StatementIdSynonym;
import queryprocessor.preprocessor.synonyms.Synonym;

import java.util.ArrayList;
import java.util.List;

public class RelationshipRef extends QTNode
{
    private final List<ArgNode> args;
    private final Keyword relType;
    private final ComputingPriority computingPriority;

    public RelationshipRef(Keyword relType, List<Synonym<?>> argsSynonyms) {
        super(relType.getName());
        this.relType = relType;
        this.args = new ArrayList<>();

        int prioritizedSynonyms = 0;
        for (int i = 0; i < argsSynonyms.size(); i++)
        {
            Synonym<?> synonym = argsSynonyms.get(i);
            ArgNode arg = new ArgNode(synonym, i+1);

            if(synonym instanceof NamedSynonym || synonym instanceof StatementIdSynonym)
                prioritizedSynonyms++;

            if(!args.isEmpty())
                args.get(args.size()-1).setRightSibling(arg);
            else
                this.setFirstChild(arg);

            arg.setParent(this);
            args.add(arg);
        }

        if(relType == Keyword.NEXT || relType == Keyword.T_NEXT
                || relType == Keyword.AFFECTS || relType == Keyword.T_AFFECTS)
            computingPriority = ComputingPriority.LOW;
        else if(prioritizedSynonyms == 2)
            computingPriority = ComputingPriority.HIGHEST;
        else if(prioritizedSynonyms == 1)
            computingPriority = ComputingPriority.HIGH;
        else
            computingPriority = ComputingPriority.NORMAL;
    }

    public int getArgSize() {
        return args.size();
    }

    public ArgNode getArg(int id) {
        return args.get(id);
    }
    public Keyword getRelationshipType() {
        return relType;
    }

    public ComputingPriority getComputingPriority() {
        return computingPriority;
    }
}
