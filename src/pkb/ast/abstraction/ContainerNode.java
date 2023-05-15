package pkb.ast.abstraction;

import pkb.ast.VariableNode;
import java.util.List;

public abstract class ContainerNode extends StatementNode {

  public final VariableNode condition;

  public final List<StatementNode> statements;

  protected ContainerNode(int statementId, VariableNode condition, List<StatementNode> statements) {
    super(statementId);
    this.condition = condition;
    this.statements = statements;

    this.setFirstChild(condition);
    condition.setRightSibling(statements.get(0));
    setParentAndSibling(statements);
  }

  public List<StatementNode> getStatements() {
    return statements;
  }

  protected void setParentAndSibling(List<StatementNode> statements) {
    ASTNode last = null;
    for (StatementNode s: statements) {
      s.setParent(this);

      if(last!= null)
        last.setRightSibling(s);

      last = s;
    }
  }
}
