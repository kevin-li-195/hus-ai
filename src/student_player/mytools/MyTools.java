package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.util.Iterator;

public class MyTools {
    // Note: Evaluation functions also fall under the abstract
    // class called Heuristic due to their both being called on states.
    // They return the expected cost to a goal state.

    enum NodeType { MAX, MIN }

    // Constructs a minimax tree of max depth d and returns the root.
    // Input: HusBoardState state, int d
    // Output: MinimaxNode root
    public static MinimaxNode makeMinimaxTree(HusBoardState s, int id, int depth) {
        Iterator<HusMove> a = s.getLegalMoves().iterator();
        MinimaxNode root = new MinimaxNode(NodeType.MAX, s, id);

        while (a.hasNext()) {
            HusMove m = a.next();
            HusBoardState newState = (HusBoardState) s.clone();
            newState.move(m);
            MinimaxNode child = new MinimaxNode(NodeType.MIN, newState, m, id);
            root.setChild(child);
            child.setParent(root); // Do we need parent setting? Keep for now.
            recMakeMinimax(child, id, depth-1);
        }

        return root;
    }

    // Helper function to recursively make minimax nodes.
    static void recMakeMinimax(MinimaxNode node, int id, int depth) {
        if (depth == 0) { return; }

        HusBoardState s = node.getState();
        Iterator<HusMove> it = s.getLegalMoves().iterator();
        NodeType t = node.getType();

        while (it.hasNext()) {
            HusMove m = it.next();
            HusBoardState ns = (HusBoardState) s.clone();
            ns.move(m);

            MinimaxNode newNode;

            switch (t) {
                case MAX:
                    newNode = new MinimaxNode(NodeType.MIN, ns, m, id);
                    break;
                default:
                    newNode = new MinimaxNode(NodeType.MAX, ns, m, id);
            }

            node.setChild(newNode);
            newNode.setParent(node); // Do we need parent setting? Keep for now.

            recMakeMinimax(newNode, id, depth-1);
        }
    }


    public static class MinimaxNode {
        private NodeType type; // MAX or MIN minimax node.
        private ArrayList<MinimaxNode> children; // Children of node.
        private MinimaxNode parent; // Parent of node.
        private HusBoardState state; // Board state corresponding to this node.
        private HusMove move; // Move that resulted in this node.
        private int value = 101; // Minimax value of this node. 101 is outside of range.
        private int myID; // Player id.

        MinimaxNode(NodeType t, HusBoardState s, int id) {
            type = t;
            state = s;
            myID = id;
        }

        MinimaxNode(NodeType t, HusBoardState s, HusMove m, int id) {
            type = t;
            state = s;
            move = m;
            myID = id;
        }

        // Input: int depth, Heuristic h
        // Output: HusMove mv
        public HusMove getMinimaxMove(Heuristic h) {
            // Handle error for when there are no children.
            Iterator<MinimaxNode> c = getChildren().iterator();
            MinimaxNode n = c.next();
            n.backup(h);
            int maxScore = n.getValue();
            HusMove optMove = n.getMove();
            while (c.hasNext()) {
                n = c.next();
                n.backup(h);
                if (n.getValue() > maxScore) {
                    maxScore = n.getValue();
                    optMove = n.getMove();
                }
            }
            return optMove;
        }

        // Helper function to recursively back up the Minimax tree.
        // Call on root node to get full back up, or on partial tree to get a partial backing-up.
        //
        // Terminates when it encounters a node that has no children.
        //
        // Upgrade this to alpha-beta pruning later.
        void backup(Heuristic h) {
            if (state.gameOver()) { // If the game is over at this state, then we check for win loss.
                if (state.getWinner() == myID) {
                    setValue(0); // Winner gets cost of 0.
                    return;
                }
                else {
                    setValue(100); // Lose or cancelled for any reason gets cost of 100.
                    return;
                }
            }
            if (getChildren().isEmpty()) {
                setValue(h.compute(myID, state));; // Having no legal moves means we're at the bottom of the minimax tree.
                return;
            }

            Iterator<MinimaxNode> it = getChildren().iterator();
            MinimaxNode nextNode = it.next();
            nextNode.backup(h);
            int optVal = nextNode.getValue();

            while (it.hasNext()) {
                nextNode = it.next();
                nextNode.backup(h);
                int v = nextNode.getValue();
                switch(type) {
                    case MAX:
                        if (v > optVal) {
                            optVal = v;
                        }
                        break;
                    case MIN:
                        if (v < optVal) {
                            optVal = v;
                        }
                }
            }

            setValue(optVal);
        }

        NodeType getType() { return type; }

        void setParent(MinimaxNode p) {
            parent = p;
        }
        MinimaxNode getParent() { return parent; }

        void setChild(MinimaxNode c) {
            children.add(c);
        }
        ArrayList<MinimaxNode> getChildren() { return children; }

        void setValue(int v) { value = v; }
        int getValue() { return value; }
        
        HusMove getMove() { return move; }
        void setMove(HusMove m) { move = m; }

        HusBoardState getState() { return state; }
    }
    
    // Abstract class for heuristics on HusBoardState states.
    // Heuristics return a normalized cost from 0 to 100.
    public abstract static class Heuristic {
        abstract int compute(int id, HusBoardState s);
    }

    // Heuristic factory to produce different heuristics for generic states
    // for the purpose of quickly testing different heuristics.
    public static class HeuristicFactory {
        public Heuristic getHeuristic(String s) {
            switch (s) {
                case "basic":
                    return new BasicHeuristic();
                default:
                    return new BasicHeuristic();
            }
        }
    }

    // Super basic, non-admissible testing heuristic function that gives a lower cost
    // if we have more pits than the opponent.
    // Normalized from 0 to 100.
    static class BasicHeuristic extends Heuristic {
        public int compute(int id, HusBoardState s) {
            int[][] pits = s.getPits();
            int[] my_pits = pits[id];
            int[] opp_pits = pits[(id + 1) % 2];
            int a = 0;
            int b = 0;
            for (int i = 0; i < my_pits.length; i++) {
                a += my_pits[i];
                b += opp_pits[i];
            }
            return (((48 - (a - b))/48)*100);
        }
    }
}
