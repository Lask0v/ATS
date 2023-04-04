package frontend.parser;

import static frontend.parser.ParseStatements.parseStatements;
import static frontend.parser.Parser.match;

import frontend.ast.ProcedureNode;
import frontend.ast.abstraction.StatementNode;
import frontend.lexer.TokenType;
import java.util.List;

class ParseProcedure {

  static ProcedureNode parseProcedure() {
    match(TokenType.PROCEDURE);
    String name = match(TokenType.NAME).getValue();
    match(TokenType.LBRACE);
    List<StatementNode> statements = parseStatements();
    match(TokenType.RBRACE);
    ProcedureNode procedureNode = new ProcedureNode(name, statements);
    for (StatementNode statement : procedureNode.statements) {
      statement.setParent(procedureNode);
    }
    return procedureNode;
  }
}