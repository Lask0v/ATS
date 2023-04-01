import java.util.List;

public class Main {

  public static void main(String[] args) {
    Lexer lexer = new Lexer();
    List<Token> tokens = lexer.tokenize("example_source_code.txt");
    PKB pkb = new PKB();
    Parser parser = new Parser(tokens, pkb);
    ASTNode ast = parser.parse();
    System.out.println(ast);
  }
}
