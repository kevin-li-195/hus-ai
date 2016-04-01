package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import student_player.mytools.Functions;
import student_player.mytools.Minimax.MinimaxNode;
import student_player.mytools.Minimax.NodeType;

public class AlphaBeta {
    public static class SearchThread extends Thread {
        private HusMove bestMove; // Always to be changed via the synchronized setter setMove.

        private MinimaxNode searchNode;
        private Functions.EvaluationFunction evalFunc;
        private int myID;
        
        SearchThread(MinimaxNode n, Functions.EvaluationFunction f, int id) {
            searchNode = n;
            evalFunc = f;
            myID = id;
        }

        public void run() {
            int LOW = Integer.MIN_VALUE;
            // We have no upper bound because we assume that the root node is a max node.

            Iterator<MinimaxNode> it = searchNode.getChildren().iterator();
            while (it.hasNext()) {
                MinimaxNode node = it.next();
                alphaBetaPrune(node, evalFunc, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (node.getValue() > LOW) {
                    LOW = node.getValue();
                    setMove(node.getMove());
                }
            }
        }

        // Recursively expand and prune nodes. Sets the value field of the node that this is called on
        // to its optimal value based on its NodeType, the evaluation function used, and alpha-beta pruning.
        void alphaBetaPrune(
                MinimaxNode currentNode,
                Functions.EvaluationFunction f, 
                int lowerBound, 
                int upperBound) {

            ArrayList<MinimaxNode> l = currentNode.getChildren();

            // Base case at the end of the minimax tree.
            if (l.isEmpty()) {
                currentNode.setValue(f.compute(myID, currentNode.getState()));
                return;
            }

            Iterator<MinimaxNode> it = l.iterator();

            while (it.hasNext()) {
                // Always check that if the optimal move selected right now is outside of
                // bounds, then we can never get a better move and so we should return.
                if (currentNode.getValue() != -1 
                        && (currentNode.getValue() < lowerBound || currentNode.getValue() > upperBound)) { return; }

                MinimaxNode nextNode = it.next();
                alphaBetaPrune(nextNode, f, lowerBound, upperBound);
                int nextNodeValue = nextNode.getValue();

                switch (currentNode.getType()) {
                    case MAX:
                        // First check if nextnodevalue is greater than lower bound and if
                        // it's greater than current value.
                        //
                        // If yes, check if it's greater than upper bound. If true, then
                        // return, ensuring that the previous node that called this node
                        // we just return as we have ascertained that our current best option
                        // will never get selected. If not then we assign nextnodevalue to
                        // current value.
                        if (nextNodeValue > currentNode.getValue() || currentNode.getValue() == -1) {
                            if (nextNodeValue > upperBound) {
                                return;
                            }
                            currentNode.setValue(nextNodeValue);
                            lowerBound = currentNode.getValue();
                        }
                        break;

                    case MIN:
                        if (nextNodeValue < currentNode.getValue() || currentNode.getValue() == -1) {
                            if (nextNodeValue < lowerBound) {
                                return;
                            }
                            currentNode.setValue(nextNode.getValue());
                            upperBound = currentNode.getValue();
                        }
                }
            }
        }

        // Use this to update the best move in a synchronized way so that we can 
        // safely get the best move every time when the thread times out.
        synchronized setMove(HusMove m) {
            bestMove = m;
        }
    }
}
