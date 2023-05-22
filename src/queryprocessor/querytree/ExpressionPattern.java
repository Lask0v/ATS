package queryprocessor.querytree;

import pkb.ast.AssignmentNode;
import pkb.ast.IfNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import queryprocessor.preprocessor.synonyms.Synonym;

import java.util.Optional;
import java.util.Set;

public class ExpressionPattern extends QTNode
{
    private final Synonym<?> synonym;
    private final Optional<ASTNode> subExpressionTree;
    private final ArgNode leftHandExpression;
    private final boolean lookBehind;
    private final boolean lookAhead;

    public ExpressionPattern(Synonym<?> synonym, Synonym<?> leftHand, ASTNode subExpressionTree, boolean lookAhead, boolean lookBehind)
    {
        super("");
        this.synonym = synonym;
        this.subExpressionTree = Optional.ofNullable(subExpressionTree);
        this.leftHandExpression = new ArgNode(leftHand, 1);
        this.lookBehind = lookBehind;
        this.lookAhead = lookAhead;
    }

    public Synonym<?> getSynonym() {
        return synonym;
    }

    public ArgNode getLeftHandExpression() {
        return leftHandExpression;
    }

    public boolean matchesPattern(ASTNode node, Set<ASTNode> allowedVariables) {
        if(!synonym.isDerivative(node))
            return false;

        if(node instanceof IfNode || node instanceof WhileNode){
            if(!isVariableAllowed(node, allowedVariables) || !matchesVariable(node))
                return false;

            // While & If node cannot have a body
            // Placeholder fits anything e.g. while(_,_), if(_,_)
            return subExpressionTree.isEmpty(); // empty exp tree means the pattern is correct since those nodes cannot have a body
        }

        if(node instanceof AssignmentNode) {
            if(!isVariableAllowed(node, allowedVariables) || !matchesVariable(node) )
                return false;

            if(node.getFirstChild() == null)
                return false;

            if(subExpressionTree.isEmpty())
                return true;

            var steps = 0;
            var expressionNode = node.getFirstChild().getRightSibling();
            while(expressionNode != null)
            {
                if(!lookBehind && steps > 1)
                    return false;

                var depth = new Depth();
                if(compareTrees(subExpressionTree.get(), expressionNode, depth, lookAhead)) {
                    if(lookAhead)
                        return true;

                    var lastNode = expressionNode;
                    var i = depth.level;
                    while(lastNode != null && i > 0) {
                        i--;
                        //lastNode = lastNode.getRightSibling();
                    }

                    // the special case when comparing fragment tree to bigger tree e.g. x*y+z+v with x*y+z
                    // Matches only if lookAhead is allowed.
                    // pattern a("x", "x*y+z"_)
                    if(lastNode == null || lastNode.getRightSibling() == null)
                        return true;
                }

                expressionNode = expressionNode.getRightSibling();
                steps++;
            }
        }

        return false;
    }

    private class Depth {
        public Integer level = 0;
    }

    boolean compareTrees(ASTNode a, ASTNode b, Depth depth, final boolean lookAhead)
    {
        /*1. both empty */
        if (a == null && b == null)
            return true;
        else if (lookAhead && a == null)
            return true;

        /* 2. both non-empty -> compare them */
        if (a != null && b != null) {
            depth.level++;
            return (a.equals(b)
                    && compareTrees(a.getFirstChild(), b.getFirstChild(), depth, lookAhead)
                    && compareTrees(a.getRightSibling(), b.getRightSibling(), depth, lookAhead));
        }

        /* 3. one empty, one not -> false */
        return false;
    }

    private boolean isVariableAllowed(ASTNode node, Set<ASTNode> allowedVariables) {
        var leftHandVar = node.getFirstChild();

        if(leftHandVar == null)
            return false;

        return allowedVariables.contains(leftHandVar);
    }

    private boolean matchesVariable(ASTNode node) {
        var leftHandVar = node.getFirstChild();

        if(leftHandVar == null)
            return false;

        return leftHandExpression.getSynonym().isDerivative(leftHandVar);
    }
}
