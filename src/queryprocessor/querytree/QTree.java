package queryprocessor.querytree;


public class QTree implements QueryTree
{
    private QTNode resultsNode = null;
    private QTNode suchThatNode = null;
    private QTNode withNode = null;

    public QTree() {
    }

    public QTNode createResultsNode() {
        if(resultsNode == null)
            resultsNode = new ResultNode();

        return resultsNode;
    }
    public QTNode createSuchThatNode() {
        if(suchThatNode == null)
            suchThatNode = new SuchThatNode();

        return suchThatNode;
    }
    public QTNode createWithNode() {
        if(withNode == null)
            withNode = new WithNode();

        return withNode;
    }

    public void addResNode(ResNode node) {
        if(resultsNode == null)
            this.createResultsNode();

        this.setNode(node, resultsNode);
    }

    public void addRelationshipNode(RelationshipNode node) {
        if(suchThatNode == null)
            this.createSuchThatNode();

        this.setNode(node, suchThatNode);
    }

    public void setWithNode(WithNode withNode) {
        this.withNode = withNode;
    }

    private void setNode(QTNode node, QTNode parent)
    {
        if(parent.getFirstChild() == null)
            parent.setFirstChild(node);
        else
        {
            var rNode = parent.getFirstChild();
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
}
