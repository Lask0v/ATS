package queryprocessor.querytree;


public class QTree implements QueryTree
{
    private QTNode resultsNode = null;
    private QTNode suchThatNode = null;
    private QTNode withNode = null;
    private QTNode patternNode = null;

    public QTree() {
    }

    public void createResultsNode() {
        if(resultsNode == null)
            resultsNode = new ResultNode();

    }
    public void createSuchThatNode() {
        if(suchThatNode == null)
            suchThatNode = new SuchThatNode();

    }
    public void createWithNode() {
        if(withNode == null)
            withNode = new WithNode();

    }

    public void createPatterNode() {
        if(patternNode == null)
            patternNode = new PatternNode();
    }

    public void addResNode(ResNode node) {
        if(resultsNode == null)
            this.createResultsNode();

        this.setNode(node, resultsNode);
    }

    public void addRelationshipNode(RelationshipRef node) {
        if(suchThatNode == null)
            this.createSuchThatNode();

        this.setNode(node, suchThatNode);
    }

    public void addConditionNode(Condition node) {
        if(withNode == null)
            this.createWithNode();

        this.setNode(node, withNode);
    }

    public void addPatternNode(ExpressionPattern node) {
        if(patternNode == null)
            this.createPatterNode();

        this.setNode(node, patternNode);
    }

    private void setNode(QTNode node, QTNode parent)
    {
        if(parent.getFirstChild() == null)
            parent.setFirstChild(node);
        else
        {
            QTNode rNode = parent.getFirstChild();
            while (rNode.getRightSibling() != null)
                rNode = rNode.getRightSibling();

            rNode.setRightSibling(node);
        }

        node.setParent(parent);
    }

    @Override
    public QTNode getResultsNode() {
        return resultsNode;
    }

    @Override
    public QTNode getSuchThatNode() {
        return suchThatNode;
    }

    @Override
    public QTNode getWithNode() {
        return withNode;
    }

    @Override
    public QTNode getPatternNode() {
        return patternNode;
    }
}
