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
        private int branchesPruned = 0;
        private int topLevelBranchesSearched = 0;
        private int branchingFactor = 0;
        
        public SearchThread(HusBoardState s, Functions.EvaluationFunction f, int id, int depth) {
            startingState = s;
            evalFunc = f;
            myID = id;
            maxDepth = depth;
        }

        public void run() {
            int LOW = Integer.MIN_VALUE;
            // We have no upper bound because we assume that the root node is a max node.

            ArrayList<HusMove> l = startingState.getLegalMoves();
            Iterator<HusMove> it = l.iterator();
            branchingFactor = l.size();

            while (it.hasNext() && !this.interrupted()) {
                HusMove nextMove = it.next();
                HusBoardState nextState = (HusBoardState) startingState.clone();
                nextState.move(nextMove);
                int val = alphaBetaPrune(nextState, evalFunc, Integer.MIN_VALUE, Integer.MAX_VALUE, maxDepth, true);
                topLevelBranchesSearched++;
                if (val > LOW) {
                    LOW = val;
                    setMove(nextMove);
                    System.out.println("Move updated: " + nextMove.toPrettyString());
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

            int bestValue;

            // Base case at the end of the minimax tree.
            if (maxDepth == 0) {
                return f.compute(myID, currentState);
            }

            ArrayList<HusMove> l = currentState.getLegalMoves();
            int listSize = l.size();

            Iterator<HusMove> it = l.iterator();

            if (isMax) {
                bestValue = lowerBound;

                while (it.hasNext() && !this.interrupted()) {
                    HusMove nextMove = it.next();
                    HusBoardState newState = (HusBoardState) currentState.clone();
                    newState.move(nextMove);
                    int val = alphaBetaPrune(newState, f, bestValue, upperBound, maxDepth-1, false);
                    listSize--;

                    if (val > bestValue) {
                        bestValue = val;
                    }

                    if (val > upperBound) {
                        addPrunedBranches(listSize);
                        return upperBound;
                    }
                }
            } else { // isMin
                bestValue = upperBound;
                while (it.hasNext() && !this.interrupted()) {
                    HusMove nextMove = it.next();
                    HusBoardState newState = (HusBoardState) currentState.clone();
                    newState.move(nextMove);
                    int val = alphaBetaPrune(newState, f, lowerBound, bestValue, maxDepth-1, true);
                    listSize--;
                    if (val < bestValue) {
                        bestValue = val;
                    }
                    if (val < lowerBound) {
                        addPrunedBranches(listSize);
                        return lowerBound;
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

        synchronized void addPrunedBranches(int a) {
            branchesPruned += a;
        }
        
        public synchronized int getPrunedBranches() {
            return branchesPruned;
        }

        public synchronized int getSearchedBranches() {
            return topLevelBranchesSearched;
        }

        public synchronized int getBranchingFactor() {
            return branchingFactor;
        }

    }
}
