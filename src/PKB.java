import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Program Knowledge Base
public class PKB {
  private final Map<Integer, Set<Integer>> follows;
  private final Map<Integer, Set<Integer>> parent;
  private final Map<Integer, Set<String>> modifies;
  private final Map<Integer, Set<String>> uses;
  private final Map<Integer, ASTNode> ast;

  public PKB() {
    follows = new HashMap<>();
    parent = new LinkedHashMap<>();
    modifies = new HashMap<>();
    uses = new HashMap<>();
    ast = new HashMap<>();
  }

  public void addFollows(int s1, int s2) {
    follows.computeIfAbsent(s1, k -> new HashSet<>()).add(s2);
  }

  public void addParent(int s1, int s2) {
    parent.computeIfAbsent(s1, k -> new LinkedHashSet<>()).add(s2);
  }

  public void addModifies(int s, String v) {
    modifies.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  public void addUses(int s, String v) {
    uses.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  public void addAST(int id, ASTNode node) {
    ast.put(id, node);
  }
}
