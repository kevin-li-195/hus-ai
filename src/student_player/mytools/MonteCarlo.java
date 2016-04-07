package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import student_player.mytools.Functions;

public class MonteCarlo {
    public static class MonteCarloNode {
        public int numerator = 0;
        public int denominator = 0; 
        static int myID = 0;

        static Comparator<MonteCarloNode> ucb = new UCBComparator();

        private HusBoardState state;
        ArrayList<MonteCarloNode> children = new ArrayList<MonteCarloNode>();
        public MonteCarloNode parent;
        private HashMap<HusBoardState, HusMove> nodeMap = new HashMap<HusBoardState, HusMove>();

        boolean isMax;

        public MonteCarloNode(HusBoardState s, boolean b) {
            state = s;
            isMax = b;
        }

        public MonteCarloNode(HusBoardState s, boolean b, int i) {
            state = s;
            isMax = b;
            myID = i;
            ArrayList<HusMove> arr = s.getLegalMoves();
            for (int a = 0; a < arr.size(); a++) {
                HusBoardState newS = (HusBoardState) s.clone();
                HusMove m = arr.get(a);
                newS.move(m);

                MonteCarloNode n = new MonteCarloNode(newS, false);
                n.parent = this;
                children.add(n);

                nodeMap.put(newS, m);
            }
        }

        public static class UCBComparator implements Comparator<MonteCarloNode> {
            static double c = 1.5;

            public int compare(MonteCarloNode o1, MonteCarloNode o2) {
                MonteCarloNode parent1 = o1.parent;
                MonteCarloNode parent2 = o2.parent;

                int parent1Visits = parent1.denominator;
                int parent2Visits = parent2.denominator;

                int o1Num = o1.numerator;
                int o2Num = o2.numerator;

                int o1Den = o1.denominator;
                int o2Den = o2.denominator;

                double o1Value;
                double o2Value;

                if (o1Den == 0) {
                    o1Value = 0;
                } else {
                    o1Value = (double) o1Num / o1Den;
                }

                if (o2Den == 0) {
                    o2Value = 0;
                } else {
                    o2Value = (double) o2Num / o2Den;
                }

                int o1Visits = o1.denominator;
                int o2Visits = o2.denominator;

                double o1UCB;
                double o2UCB;

                if (o1Visits == 0) {
                    o1UCB = Double.POSITIVE_INFINITY;
                } else {
                    o1UCB = o1Value + (c * Math.sqrt(Math.log(parent1Visits)/o1Visits));
                }

                if (o2Visits == 0) {
                    o2UCB = Double.POSITIVE_INFINITY;
                } else {
                    o2UCB = o2Value + (c * Math.sqrt(Math.log(parent2Visits)/o2Visits));
                }

                if (o1UCB == o2UCB) {
                    return 0;
                } else if (o1UCB < o2UCB) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }

        // Performs one iteration of MCTS.
        public static boolean simulate(MonteCarloNode currentNode, Comparator<HusBoardState> c) {
            if (currentNode.children.isEmpty()) {
                // Generate moves using c.
                
                HusBoardState startingState = currentNode.getState();
                ArrayList<HusMove> moveList = currentNode.getState().getLegalMoves();
                HusBoardState[] stateArray = new HusBoardState[moveList.size()];

                for (int i = 0; i < stateArray.length; i++) {
                    HusBoardState newState = (HusBoardState) startingState.clone();
                    HusMove m = moveList.get(i);
                    newState.move(m);

                    stateArray[i] = newState;
                }

                if (currentNode.isMax) {
                    Arrays.sort(stateArray, c.reversed());
                } else {
                    Arrays.sort(stateArray, c);
                }

                HusBoardState optState = stateArray[0];

                MonteCarloNode chosenNode;

                if (currentNode.isMax) {
                    if (optState.gameOver()) {
                        if (optState.getWinner() == myID) {
                            return true;
                        } else {
                            System.out.println("Something went wrong. Lost when you shouldn't have.");
                            return false;
                        }
                    }
                    chosenNode = new MonteCarloNode(optState, false);
                } else {
                    if (optState.gameOver()) {
                        if (optState.getWinner() != myID) {
                            return false;
                        } else {
                            System.out.println("Something went wrong. Won when you shouldn't have.");
                            return true;
                        }
                    }
                    chosenNode = new MonteCarloNode(optState, true);
                }

                chosenNode.parent = currentNode;
                boolean win = heavyRollout(chosenNode, c);
                currentNode.children.add(chosenNode);
                if (win) {
                    currentNode.numerator += 1;
                    currentNode.denominator += 1;
                    chosenNode.numerator += 1;
                    chosenNode.denominator += 1;
                    return true;
                } else {
                    currentNode.denominator += 1;
                    chosenNode.denominator += 1;
                    return false;
                }
            } else {
                // choose node descentNode using UCB comparator
                ArrayList<MonteCarloNode> nodeList = currentNode.children;
                if (currentNode.isMax) {
                    nodeList.sort(ucb.reversed());
                } else {
                    nodeList.sort(ucb);
                }
                MonteCarloNode optNode = nodeList.get(0);
                boolean win = simulate(optNode, c);
                if (win) {
                    currentNode.numerator += 1;
                    currentNode.denominator += 1;
                    return true;
                } else {
                    currentNode.denominator += 1;
                    return false;
                }
            }
        }

        static boolean heavyRollout(MonteCarloNode startNode, Comparator<HusBoardState> c) {
            HusBoardState startState = startNode.getState();

            // Check for victory/loss state.
            if (startState.gameOver()) {
                if (startState.getWinner() == myID) {
                    return true;
                } else {
                    return false;
                }
            }

            ArrayList<HusMove> moves = startState.getLegalMoves();
            HusBoardState[] candidateStates = new HusBoardState[moves.size()];

            for (int i = 0; i < moves.size(); i++) {
                HusBoardState nextState = (HusBoardState) startState.clone();
                HusMove candidateMove = moves.get(i);
                nextState.move(candidateMove);

                candidateStates[i] = nextState;
            }

            MonteCarloNode nextNode;

            if (startNode.isMax) {
                Arrays.sort(candidateStates, c.reversed());
                nextNode = new MonteCarloNode(candidateStates[0], false);
            } else { //isMin
                Arrays.sort(candidateStates, c);
                nextNode = new MonteCarloNode(candidateStates[0], true);
            }

            return heavyRollout(nextNode, c);
        }

        // Builds path down to a leaf node.
        ArrayList<MonteCarloNode> getFrontierPath(MonteCarloNode firstChild, Comparator<HusBoardState> c, boolean isExploration) {

            ArrayList<MonteCarloNode> returnPath = new ArrayList<MonteCarloNode>();
            MonteCarloNode currentNode = firstChild;

            // Tree policy.
            // Might want to probabilistically weight these because we might end up always choosing
            // the same path. Test this.
            if (isExploration) {
                // BFS to find shortest path and then return
            } else {
                while (!currentNode.children.isEmpty()) {
                    returnPath.add(currentNode);

                    if (currentNode.isMax) {
                    } else { // isMin
                    }
                    currentNode = currentNode.children.get(0);
                }
            }

            return returnPath;
        }


        public synchronized HusMove getBestMove() {
            children.sort(ucb.reversed());
            System.out.println("Got best move with score: " + children.get(0).numerator + "/" + children.get(0).denominator);
            return nodeMap.get(children.get(0).getState());
        }

        HusBoardState getState() {
            return state;
        }

        public void printRootStats() {
            String s = "";
            for (int a = 0; a < children.size(); a++) {
                s += " ";
                s += children.get(a).numerator + "/" + children.get(a).denominator;
            }

            System.out.println("Top level stats are: " + s);
        }
    }

    public static class SearchThread extends Thread {
        private MonteCarloNode rootNode;
        private HusBoardState rootState;
        private Comparator<HusBoardState> comparator;
        private int myID;

        private HusMove bestMove;

        public SearchThread(HusBoardState s, Comparator<HusBoardState> c, int i) {
            rootState = s;
            comparator = c;
            myID = i;
        }

        public void run() {
            rootNode = new MonteCarloNode(rootState, true, myID);
            while (!this.isInterrupted()) {
                MonteCarloNode.simulate(rootNode, comparator);
            }
            rootNode.printRootStats();
        }

        synchronized void setMove(HusMove m) {
            if (m != null && getMove() != m) {
                bestMove = m;
            } 
        }


        public synchronized HusMove getMove() {
            return rootNode.getBestMove();
        }
    }
}
