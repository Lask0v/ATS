public enum TokenType {
  PROCEDURE("procedure"),
  WHILE("while"),
  LBRACE("\\{"),
  RBRACE("\\}"),
  EQUALS("="),
  PLUS("\\+"),
  SEMICOLON(";"),
  NAME("[a-zA-Z][a-zA-Z\\d#]*"),
  CONSTANT("\\d+"),
  WHITESPACE("\\s+");

  public final String pattern;

  TokenType(String pattern) {
    this.pattern = pattern;
  }
}
