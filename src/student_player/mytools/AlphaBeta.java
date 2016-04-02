package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.util.Iterator;

import student_player.mytools.Functions;

public class AlphaBeta {
    public static class SearchThread extends Thread {
        private HusMove bestMove; // Always to be changed via the synchronized setter setMove.

        private HusBoardState startingState;
        private Functions.EvaluationFunction evalFunc;
        private int myID;
        private int maxDepth;
        
        public SearchThread(HusBoardState s, Functions.EvaluationFunction f, int id, int depth) {
            startingState = s;
            evalFunc = f;
            myID = id;
            maxDepth = depth;
        }

        public void run() {
            int LOW = Integer.MIN_VALUE;
            // We have no upper bound because we assume that the root node is a max node.

            Iterator<HusMove> it = startingState.getLegalMoves().iterator();

            while (it.hasNext() && !this.interrupted()) {
                HusMove nextMove = it.next();
                HusBoardState nextState = (HusBoardState) startingState.clone();
                nextState.move(nextMove);
                int val = alphaBetaPrune(nextState, evalFunc, Integer.MIN_VALUE, Integer.MAX_VALUE, maxDepth, true);
                if (val > LOW) {
                    LOW = val;
                    setMove(nextMove);
                }
            }
        }

        // Recursively expand and prune nodes. Returns the value of the node that this is called on
        // to its optimal value based on its node type, the evaluation function used, and alpha-beta pruning.
        int alphaBetaPrune(
                HusBoardState currentState,
                Functions.EvaluationFunction f, 
                int lowerBound, 
                int upperBound,
                int maxDepth,
                boolean isMax) {

            int bestValue = -1;

            // Base case at the end of the minimax tree.
            if (maxDepth == 0) {
                return f.compute(myID, currentState);
            }

            Iterator<HusMove> it = currentState.getLegalMoves().iterator();

            while (it.hasNext()) {
                // Always check that if the optimal move selected right now is outside of
                // bounds, then we can never get a better move and so we should return.
               
                HusMove nextMove = it.next();
                HusBoardState newState = (HusBoardState) currentState.clone();
                newState.move(nextMove);


                if (isMax) {
                    int val = alphaBetaPrune(newState, f, lowerBound, upperBound, maxDepth-1, false);
                    // First check if nextnodevalue is greater than lower bound and if
                    // it's greater than current value.
                    //
                    // If yes, check if it's greater than upper bound. If true, then
                    // return, ensuring that the previous node that called this node
                    // we just return as we have ascertained that our current best option
                    // will never get selected. If not then we assign nextnodevalue to
                    // current value.
                    if (val > bestValue || bestValue == -1) {
                        if (val > upperBound) {
                            return -1;
                        } else {
                            bestValue = val;
                            lowerBound = val;
                        }
                    }
                } else { // isMin
                    int val = alphaBetaPrune(newState, f, lowerBound, upperBound, maxDepth-1, true);
                    if (val < bestValue || bestValue == -1) {
                        if (val < lowerBound) {
                            return -1;
                        } else {
                            bestValue = val;
                            upperBound = val;
                        }
                    }
                }
            }

            return bestValue;
        }

        // Use this to update the best move in a synchronized way so that we can 
        // safely get the best move every time when the thread times out.
        synchronized void setMove(HusMove m) {
            bestMove = m;
        }

        public synchronized HusMove getMove() {
            return bestMove;
        }
    }
}
