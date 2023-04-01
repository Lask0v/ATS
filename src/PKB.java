import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

// Program Knowledge Base
public class PKB {

  private final Map<Integer, Set<Integer>> follows;

  private final Map<Integer, Set<Integer>> followsStar;

  private final Map<Integer, Set<Integer>> parent;

  private final Map<Integer, Set<Integer>> parentStar;

  private final Map<Integer, Set<String>> modifies;

  private final Map<Integer, Set<String>> uses;

  private final Map<Integer, ASTNode> ast;

  public PKB() {
    follows = new HashMap<>();
    followsStar = new HashMap<>();
    parent = new LinkedHashMap<>();
    parentStar = new LinkedHashMap<>();
    modifies = new HashMap<>();
    uses = new HashMap<>();
    ast = new HashMap<>();
  }

  public void addFollows(int s1, int s2) {
    follows.computeIfAbsent(s1, k -> new HashSet<>()).add(s2);
  }

  private void addFollowsStars(int s1, Set<Integer> followsSet) {
    followsStar.put(s1, followsSet);
  }

  public void addParent(int s1, int s2) {
    parent.computeIfAbsent(s1, k -> new LinkedHashSet<>()).add(s2);
  }

  public void addModifies(int s, String v) {
    modifies.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  private void addParentStars(int s1, Set<Integer> followsSet) {
    parentStar.put(s1, followsSet);
  }

  public void addUses(int s, String v) {
    uses.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(v);
  }

  public void addAST(int id, ASTNode node) {
    ast.put(id, node);
  }

  public void computeTransitiveClosures() {
    computeTransitiveClosure(follows, this::addFollowsStars);
    computeTransitiveClosure(parent, this::addParentStars);
  }

  private void computeTransitiveClosure(Map<Integer, Set<Integer>> directRelation,
      BiConsumer<Integer, Set<Integer>> addTransitiveClosure) {
    for (Integer stmt1 : directRelation.keySet()) {
      Set<Integer> transitiveClosure = new HashSet<>();
      computeTransitiveClosure(stmt1, directRelation, transitiveClosure);
      addTransitiveClosure.accept(stmt1, transitiveClosure);
    }
  }

  private void computeTransitiveClosure(
      int currentStmt,
      Map<Integer, Set<Integer>> directRelation,
      Set<Integer> transitiveClosure
  ) {
    Set<Integer> direct = directRelation.get(currentStmt);
    if (direct != null) {
      for (Integer nextStmt : direct) {
        if (transitiveClosure.add(nextStmt)) {
          computeTransitiveClosure(nextStmt, directRelation, transitiveClosure);
        }
      }
    }
  }
}
