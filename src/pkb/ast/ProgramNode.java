package pkb.ast;

import java.util.List;
import pkb.ast.abstraction.ASTNode;

public class ProgramNode extends ASTNode {

  public final List<ProcedureNode> procedures;

  public ProgramNode(List<ProcedureNode> procedures) {
    this.procedures = procedures;

    ASTNode last = null;
    this.setFirstChild(procedures.get(0));
    for (ProcedureNode p: procedures) {
      p.setParent(this);

      if(last!= null)
        last.setRightSibling(p);

      last = p;
    }
  }
}
