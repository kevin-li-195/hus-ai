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
        private int startingDepth;
        private int branchesPruned = 0;
        private int branchingFactor = 0;
        
        public SearchThread(HusBoardState s, Functions.EvaluationFunction f, int id, int d) {
            startingState = s;
            evalFunc = f;
            myID = id;
            startingDepth = d;
        }

        public void run() {
            int MAX_DEPTH = 50;

            for (int depth = startingDepth; depth < MAX_DEPTH; depth++) {
                if (this.isInterrupted()) {
                    System.out.println("Thread is interrupted. Killing now.");
                    break;
                }
                int LOW = Integer.MIN_VALUE;
                // We have no upper bound because we assume that the root node is a max node.

                HusMove[] l = startingState.getLegalMoves().toArray(new HusMove[startingState.getLegalMoves().size()]);
                branchingFactor = l.length;
                int i = 0;

                while (!this.isInterrupted() && i < l.length) {
                    HusMove nextMove = l[i];
                    HusBoardState nextState = (HusBoardState) startingState.clone();
                    nextState.move(nextMove);

                    int val = alphaBetaPrune(nextState, evalFunc, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, true);

                    // Everytime we get a value, we check if this value is both better than
                    // the current best value for that move.
                    //
                    // Or we could compare only moves at the same depth with each other.
                    //
                    // Tradeoff between the two: if we just check if this value
                    // is the best value for the move, we may incorrectly reject a move
                    // as our evaluation function might overestimate the value of a move
                    // due to not searching deep enough.
                    //
                    // If we only compare moves at the same depth with each other, then
                    // we implicitly don't trust our evaluation function as a move
                    // with a lower score, searched from a deeper search, would be chosen
                    // over another higher scoring that was searched from a shallower search.
                    // This would only occur in a case where a search is interrupted prematurely
                    // due to time constraints.
                    //
                    // Here we will test the performance of both approaches.
                    //
                    // My intuition is that a deeper search will win more often, even
                    // if we are removing all solutions that have been found from shallower searches.
                    //
                    // We implement this by resetting the low bound with every new search depth.
                    //
                    // This should also serve to address the null pointer issues when the agent
                    // does not find a move quickly enough with a specified depth.
                    if (val > LOW) {
                        LOW = val;
                        setMove(nextMove);
                        System.out.println("Move updated: " + nextMove.toPrettyString() + " at depth " + depth);
                    }
                    i++;
                }

            }
            if (!this.isInterrupted()) { System.out.println("Thread terminated."); }
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

                while (it.hasNext() && !this.isInterrupted()) {
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
                while (it.hasNext() && !this.isInterrupted()) {
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

        public synchronized int getBranchingFactor() {
            return branchingFactor;
        }

    }
}
