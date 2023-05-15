package queryprocessor.evaluator;

import pkb.ProgramKnowledgeBaseAPI;
import pkb.ast.AssignmentNode;
import pkb.ast.IfNode;
import pkb.ast.ProcedureNode;
import pkb.ast.WhileNode;
import pkb.ast.abstraction.ASTNode;
import pkb.ast.abstraction.StatementNode;
import pkb.cfg.CfgNode;
import pkb.cfg.ControlFlowGraph;
import queryprocessor.evaluator.abstraction.EvaluationEngine;
import utils.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class EvalEngine implements EvaluationEngine
{
    private final ProgramKnowledgeBaseAPI pkb;

    public EvalEngine(ProgramKnowledgeBaseAPI api) {
        this.pkb = api;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateParentRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates) {
        Set<Pair<ASTNode, ASTNode>> pairs = new HashSet<>();
        //parentCandidates = parentCandidates.stream().filter(p -> !childCandidates.contains(p)).collect(Collectors.toSet());

        for (ASTNode cCandidate: childCandidates) {
            if(cCandidate.getParent() == null)
                continue;

            ASTNode parent = cCandidate.getParent();
            if(parentCandidates.contains(parent) && parent != cCandidate)
                pairs.add(new Pair<>(parent, cCandidate));
        }

        return pairs;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateParentTransitiveRel(Set<ASTNode> parentCandidates, Set<ASTNode> childCandidates) {
        Set<Pair<ASTNode, ASTNode>> pairs = new HashSet<>();

        for (ASTNode childCandidate: childCandidates) {
            ASTNode parent = childCandidate.getParent();
            while (parent != null) {
                if(parentCandidates.contains(parent))
                    pairs.add(new Pair<>(parent, childCandidate));

                parent = parent.getParent();
            }
        }

        return pairs;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateUsesRel(Set<ASTNode> statements, Set<ASTNode> variables) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (ASTNode statement: statements) {
            Set<pkb.ast.VariableNode> uses = pkb.getUses(statement);

            for (ASTNode variable: variables)
            {
               if(uses.contains(variable))
                    pairSet.add(new Pair<>(statement, variable));
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateModifiesRel(Set<ASTNode> statements, Set<ASTNode> variables) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (ASTNode statement: statements) {
            Set<pkb.ast.VariableNode> modifies = pkb.getModifies(statement);

            for (ASTNode variable: variables) {
                if(modifies.contains(variable)) {
                    pairSet.add(new Pair<>(statement, variable));
                }
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateCallsRel(Set<ASTNode> callingCandidate, Set<ASTNode> beingCalledCandidate) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (ASTNode caller: callingCandidate) {
            Set<ProcedureNode> calledProcedures = pkb.getCalls(caller);

            for (ASTNode called: beingCalledCandidate) {
                if(calledProcedures.contains(called) && caller != called)
                    pairSet.add(new Pair<>(caller, called));
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateFollowsRel(Set<ASTNode> precedingCandidate, Set<ASTNode> followingCandidate) {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (ASTNode pre: precedingCandidate) {
            for (ASTNode following: followingCandidate) {
                if(pre.getRightSibling() == following )
                {
                    pairSet.add(new Pair<>(pre, following));
                    break;
                }
            }
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateFollowsTransitiveRel(Set<ASTNode> precedingCandidate, Set<ASTNode> followingCandidate)
    {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (ASTNode pre: precedingCandidate) {
            Stack<ASTNode> stack = new Stack<>();
               ASTNode node = pre.getRightSibling();

                do {
                    if(node == null) {
                        if(!stack.empty())
                            node = stack.pop();
                        continue;
                    }
                    stack.add(node.getRightSibling());

                    if(node instanceof StatementNode && followingCandidate.contains(node))
                        pairSet.add(new Pair<>(pre, node));

                    node = node.getRightSibling();
                } while(!stack.empty() || node != null);
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateNextRel(Set<ASTNode> precedingProgramLine, Set<ASTNode> followingProgramLine)
    {
        Set<Pair<ASTNode, ASTNode>> pairSet = new HashSet<>();

        for (ASTNode programLine: precedingProgramLine) {
            ProcedureNode ownerProcedure = getStatementOwner(programLine);

            if(ownerProcedure == null)
                continue;

            pkb.cfg.ControlFlowGraph graph = pkb.getControlFlowGraph(ownerProcedure);

            Pair<CfgNode, CfgNode> branching = graph.getBranching(programLine);

            CfgNode first = branching.getFirst();
            if(first != null && followingProgramLine.contains(first.getAstNode()))
                pairSet.add(new Pair<>(programLine, first.getAstNode()));

            CfgNode second = branching.getSecond();
            if(second != null && followingProgramLine.contains(second.getAstNode()))
                pairSet.add(new Pair<>(programLine, second.getAstNode()));
        }

        return pairSet;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateNextTransitiveRel(Set<ASTNode> precedingProgramLine, Set<ASTNode> followingProgramLine) {
        HashSet<Pair<ASTNode, ASTNode>> resultPairs = new HashSet<Pair<ASTNode, ASTNode>>();

        List<ASTNode> preceding = precedingProgramLine.stream().sorted(Comparator.comparingInt(a -> ((StatementNode) a).getStatementId())).collect(Collectors.toList());
        List<ASTNode> following = followingProgramLine.stream().sorted((a1, a2) -> ((StatementNode)a2).getStatementId() - ((StatementNode)a1).getStatementId()).collect(Collectors.toList());

        HashSet<Pair<ASTNode, ASTNode>> computed = new HashSet<Pair<ASTNode, ASTNode>>();

        for (ASTNode programLine: preceding)
        {
            for (ASTNode destination: following)
            {
                if(computed.contains(new Pair<>(programLine, destination)))
                    continue;

                ProcedureNode ownerProcedure = getStatementOwner(programLine);
                if(ownerProcedure == null)
                    continue;

                ControlFlowGraph controlFlowGraph = pkb.getControlFlowGraph(ownerProcedure);

                List<List<CfgNode>> flowPaths = controlFlowGraph.getFlowPaths(programLine, destination);

                for (List<CfgNode> path: flowPaths) {
                    List<StatementNode> astPath = path.stream().map(CfgNode::getAstNode).collect(Collectors.toList());

                    for (StatementNode astNode: astPath)
                    {
                        if(!followingProgramLine.contains(astNode))
                            continue;

                        computed.add(new Pair<>(programLine, astNode));
                        resultPairs.add(new Pair<>(programLine, astNode));
                    }
                }
            }
        }

        return resultPairs;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateAffectRel(Set<ASTNode> assign1, Set<ASTNode> assign2)
    {
        HashSet<Pair<ASTNode, ASTNode>> resultsPairs = new HashSet<>();

        for (ASTNode a1: assign1) {
            if(!(a1 instanceof AssignmentNode))
                continue;

            Set<pkb.ast.VariableNode> modifies = pkb.getModifies(a1);
            if(modifies.isEmpty())
                continue;

            pkb.ast.VariableNode variable = modifies.stream().findFirst().get();

            ProcedureNode ownerProcedure = getStatementOwner(a1);

            for (ASTNode a2: assign2) {
                //if(a1 == a2)
                    //continue;

                if(!(a2 instanceof AssignmentNode))
                    continue;

                if(!ownerProcedure.equals(getStatementOwner(a2)))
                    continue;

                Set<pkb.ast.VariableNode> uses = pkb.getUses(a2);
                if(uses.isEmpty())
                    continue;

                if(!uses.contains(variable))
                    continue;

                ControlFlowGraph cfg = pkb.getControlFlowGraph(ownerProcedure);
                if(cfg == null)
                    continue;

                List<List<CfgNode>> flowPaths = cfg.getFlowPaths(a1, a2);
                flowPaths = flowPaths.stream().filter(l -> !l.isEmpty()).collect(Collectors.toList());

                if(flowPaths.isEmpty())
                    continue;

                for (List<CfgNode> flowPath: flowPaths) {
                    boolean forbidden = false;
                    for (CfgNode step: flowPath) {
                        StatementNode node = step.getAstNode();
                        if(node == null)
                            continue;

                        if(node instanceof WhileNode || node instanceof IfNode)
                            if(node.getStatementId() < ((AssignmentNode) a2).getStatementId())
                                continue;

                        if(node.equals(a1) || node.equals(a2))
                            continue;

                        Set<pkb.ast.VariableNode> nodeModifies = pkb.getModifies(node);
                        if(nodeModifies.isEmpty())
                            continue;

                        if(nodeModifies.contains(variable)) {
                            forbidden = true;
                            break;
                        }
                    }

                    if(!forbidden) {
                        resultsPairs.add(new Pair<>(a1, a2));
                        break;
                    }
                }
            }
        }

        return resultsPairs;
    }

    @Override
    public Set<Pair<ASTNode, ASTNode>> evaluateAffectTransitiveRel(Set<ASTNode> assign1Candidates, Set<ASTNode> assign2Candidates) {
        HashSet<Pair<ASTNode, ASTNode>> resultPairs = new HashSet<Pair<ASTNode, ASTNode>>();

        for (ASTNode a1: assign1Candidates) {
            if (!(a1 instanceof AssignmentNode))
                continue;

            Set<pkb.ast.VariableNode> modifies = pkb.getModifies(a1);
            if (modifies.isEmpty())
                continue;

            ProcedureNode ownerProcedure = getStatementOwner(a1);
            for (ASTNode a2 : assign2Candidates) {

                if (!(a2 instanceof AssignmentNode))
                    continue;

                if (!ownerProcedure.equals(getStatementOwner(a2)))
                    continue;

                ControlFlowGraph cfg = pkb.getControlFlowGraph(ownerProcedure);
                if (cfg == null)
                    continue;

                List<List<CfgNode>> flowPaths = cfg.getFlowPaths(a1, a2);
                flowPaths = flowPaths.stream().filter(l -> !l.isEmpty()).collect(Collectors.toList());

                if (flowPaths.isEmpty())
                    continue;

                if(!evaluateAffectRel(Set.of(a1), Set.of(a2)).isEmpty()) {
                    resultPairs.add(new Pair<>(a1, a2));
                }

                for (List<CfgNode> flowPath : flowPaths) {
                    flowPath = flowPath.stream().filter(cfgNode ->
                    {
                        StatementNode node = cfgNode.getAstNode();
                        return node != null ;//&& !node.equals(a1) && !node.equals(a2);
                    }).collect(Collectors.toList());

                    for (int i = 0; i < flowPath.size(); i++) {
                        CfgNode step = flowPath.get(i);
                        StatementNode node = step.getAstNode();

                        Set<Pair<ASTNode, ASTNode>> affectsResult = evaluateAffectRel(Set.of(a1), Set.of(node));
                        if(affectsResult.isEmpty())
                            continue;

                        if(i+1 >= flowPath.size())
                            continue;


                        Set<Pair<ASTNode, ASTNode>> nextAffectsResult = evaluateAffectTransitiveRel(Set.of(flowPath.get(i+1).getAstNode()), Set.of(a2));
                        if(nextAffectsResult.isEmpty())
                            continue;

                        resultPairs.add(new Pair<>(a1, a2));
                    }
                }
            }
        }

        return resultPairs;
    }

    private ProcedureNode getStatementOwner(ASTNode node)
    {
        while (node != null) {
            if(node instanceof ProcedureNode)
                return (ProcedureNode) node;

            node = node.getParent();
        }

        return null;
    }
}
