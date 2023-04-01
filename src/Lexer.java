import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

  private final Pattern tokenPattern;

  // (?<%s>%s) -> named capturing group.
  // The former %s is the name of the group
  // The latter %s is the pattern that the group should capture.
  public Lexer() {
    StringBuilder tokenPatterns = new StringBuilder();
    for (TokenType tokenType : TokenType.values()) {
      tokenPatterns.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));
    }
    tokenPattern = Pattern.compile(tokenPatterns.substring(1));
  }

  public List<Token> tokenize(String filePath) {
    List<Token> tokens = new ArrayList<>();
    try {
      String input = Files.readString(Path.of(filePath));
      Matcher matcher = tokenPattern.matcher(input);

      while (matcher.find()) {
        for (TokenType type : TokenType.values()) {
          String value = matcher.group(type.name());
          if (value != null) {
            if (type != TokenType.WHITESPACE) {
              tokens.add(new Token(type, value));
            }
            break;
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return tokens;
  }
}

