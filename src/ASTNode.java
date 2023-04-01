import java.util.List;

abstract class ASTNode {

}

abstract class ExpressionNode extends ASTNode {

}

abstract class StatementNode extends ASTNode {

  int statementId;

  StatementNode(int statementId) {
    this.statementId = statementId;
  }
}

class ProcedureNode extends ASTNode {

  String name;

  List<StatementNode> statements;

  ProcedureNode(String name, List<StatementNode> statements) {
    this.name = name;
    this.statements = statements;
  }
}

class WhileNode extends StatementNode {

  VariableNode condition;

  List<StatementNode> statements;

  WhileNode(int statementId, VariableNode condition, List<StatementNode> statements) {
    super(statementId);
    this.condition = condition;
    this.statements = statements;
  }
}

class AssignmentNode extends StatementNode {

  String name;

  ExpressionNode expression;

  AssignmentNode(int statementId, String name, ExpressionNode expression) {
    super(statementId);
    this.name = name;
    this.expression = expression;
  }
}

class PlusNode extends ExpressionNode {

  ExpressionNode left;

  ExpressionNode right;

  PlusNode(ExpressionNode left, ExpressionNode right) {
    this.left = left;
    this.right = right;
  }
}

class ConstantNode extends ExpressionNode {

  int value;

  ConstantNode(int value) {
    this.value = value;
  }
}

class VariableNode extends ExpressionNode {

  String name;

  VariableNode(String name) {
    this.name = name;
  }
}
