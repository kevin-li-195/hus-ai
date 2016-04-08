package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

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

            HusMove[] l = startingState.getLegalMoves().toArray(new HusMove[startingState.getLegalMoves().size()]); // All legal moves from starting state.
            HusBoardState[] topLevelStates = new HusBoardState[l.length];
            HashMap<HusBoardState, HusMove> stateMap = new HashMap<HusBoardState, HusMove>();

            for (int a = 0; a < l.length; a++) {
                HusMove nextMove = l[a];
                HusBoardState newState = (HusBoardState) startingState.clone();
                newState.move(nextMove);

                stateMap.put(newState, nextMove);

                topLevelStates[a] = newState;
            }

            Arrays.sort(topLevelStates, evalFunc.reversed());

            System.out.println("Sorted top level states.");

            branchingFactor = l.length; // Record branching factor for debugging.


            for (int depth = startingDepth; depth < MAX_DEPTH; depth++) {
                if (this.isInterrupted()) { break; }

                int LOW = Integer.MIN_VALUE;
                // We have no upper bound because we assume that the root node is a max node.
                
                int i = 0;

                while (!this.isInterrupted() && i < topLevelStates.length) {
                    
                    HusBoardState nextState = topLevelStates[i];

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
                    // My intuition is that a deeper search will win more often, even
                    // if we are removing all solutions that have been found from shallower searches.
                    //
                    // We implement this by resetting the low bound with every new search depth.
                    //
                    // This should also serve to address the null pointer issues when the agent
                    // does not find a move quickly enough with a specified depth.
                    //
                    // Note: We are attempting to solve the problem of selecting a subpar solution
                    // and having the thread terminate early by properly ordering the states before calling
                    // alphaBetaPrune on them.
                    //
                    // We also do this ordering within the pruning function itself.
                    

                    // todo: If the int value from this path corresponds to a victory value, then we stop searching
                    // because this path guarantees victory. We might not have to program this if we always search for
                    // the best path even from the previous depth though.
                    if (val > LOW) {
                        LOW = val;
                        HusMove n = stateMap.get(nextState);
                        shiftToFront(topLevelStates, i);
                        setMove(n);
                        System.out.println("Move updated: " + n.toPrettyString() + " at depth " + depth);
                    }
                    i++;
                }
            }
            if (!this.isInterrupted()) { System.out.println("Thread terminated naturally."); }
        }

        void shiftToFront(HusBoardState[] arr, int i) {
            HusBoardState temp = arr[i];
            for (int a = i; a > 0; a--) {
                arr[a] = arr[a-1];
            }
            arr[0] = temp;
        }

        static boolean stateEquals(HusBoardState a, HusBoardState b) {
            int[][] aBoard = a.getPits();
            int[][] bBoard = b.getPits();
            boolean f = true;

            for (int i = 0; i < 32; i++) {
                if (aBoard[0][i] != bBoard[0][i] || aBoard[1][i] != bBoard[1][i]) {
                    f = false;
                }
            }

            return (a.getTurnPlayer() == b.getTurnPlayer() && f);
        }

        // Recursively expand and prune nodes. Returns the value of the node that this is called on
        // to its optimal value based on its node type, the evaluation function used, and alpha-beta pruning.
        int alphaBetaPrune(
                HusBoardState currentState,
                Functions.EvaluationFunction f, 
                int lowerBound, 
                int upperBound,
                int depth,
                boolean isMax) {

            int bestValue;

            // Depth weighted victories: shallower victories are better victories.
            if (currentState.gameOver()) {
                if (currentState.getWinner() == myID) {
                    return depth*100 + 100;
                } else {
                    return -(depth*100) - 100;
                }
            }

            // Base case at the end of the minimax tree.
            if (depth == 0) {
                return f.compute(currentState);
            }

            HusMove[] l = currentState.getLegalMoves().toArray(new HusMove[currentState.getLegalMoves().size()]);

            HusBoardState[] allStates = new HusBoardState[l.length];

            // Make array of all possible states from this current state for later sorting.
            for (int a = 0; a < l.length; a++) {
                HusMove nextMove = l[a];
                HusBoardState newState = (HusBoardState) currentState.clone();
                newState.move(nextMove);
                allStates[a] = newState;
            }

            int branchesRemaining = l.length;

            // Sort allStates using EvaluationFunction comparator, depending on min or max.
            if (isMax) {
                bestValue = lowerBound;

                // If max, sort in descending order (i.e., first we want to look
                // at states where we are at an advantage).
                Arrays.sort(allStates, f.reversed());

                int i = 0;
                // Try ceiling of log? or square root?
                // or searching a smaller "width" as the game progresses? (gotten from turnnumber)
                int cap = allStates.length;
                while (i < cap && !this.isInterrupted()) {
                    HusBoardState newState = allStates[i];

                    int val = alphaBetaPrune(newState, f, bestValue, upperBound, depth-1, false);
                    branchesRemaining--;

                    if (val > bestValue) {
                        bestValue = val;
                    }

                    if (val > upperBound) {
                        addPrunedBranches(branchesRemaining);
                        return upperBound;
                    }
                    i++;
                }

            } else { // isMin
                bestValue = upperBound;

                // If min, sort in ascending order (i.e., first we want to look
                // at states where we are at a disadvantage).
                Arrays.sort(allStates, f);

                int cap = allStates.length;
                int i = 0;
                while (i < cap && !this.isInterrupted()) {
                    HusBoardState newState = allStates[i];

                    int val = alphaBetaPrune(newState, f, lowerBound, bestValue, depth-1, true);
                    branchesRemaining--;
                    if (val < bestValue) {
                        bestValue = val;
                    }
                    if (val < lowerBound) {
                        addPrunedBranches(branchesRemaining);
                        return lowerBound;
                    }
                    i++;
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
