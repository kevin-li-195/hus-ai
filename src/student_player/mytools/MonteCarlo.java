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
        static Comparator<MonteCarloNode> statsComparator = new StatsOrder();

        private HusBoardState state;
        ArrayList<MonteCarloNode> children = new ArrayList<MonteCarloNode>();
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

                nodeMap.put(newS, m);
            }
        }

        public static class StatsOrder implements Comparator<MonteCarloNode> {
            public int compare(MonteCarloNode o1, MonteCarloNode o2) {
                if (o1.denominator == 0 && o2.denominator == 0) {
                    return 0;
                } else if (o1.denominator == 0) {
                    return 1;
                } else if (o2.denominator == 0) {
                    return -1;
                } else {
                   if ((o1.getStat() - o2.getStat()) < 0) {
                       return -1;
                   } else {
                       return 1;
                   }
                }
            }
        }

        // Rolls out one iteration.
        // Policies will order all states and then pick the first one.
        // Thus we can use evaluation functions and use comparators to sort.
        public void simulate(Comparator<HusBoardState> c) {
            // Returns a path to the best leaf node + 1, where the last element is the most promising 
            // child of that leaf node chosen by our comparator, and where we keep choosing the best move until we reach
            // a leaf node. (in terms of statistic). getFrontierPath also adds the newly chosen node to the tree.
            ArrayList<MonteCarloNode> path = getFrontierPath(c); 
            MonteCarloNode newChild = path.get(path.size()-1);

            // Play out from newChild and return win/loss stat.
            boolean isWin = heavyRollout(newChild, c);

            // Update statistics along the path.
            for (MonteCarloNode n : path) {
                if (isWin) {
                    n.numerator++;
                }
                n.denominator++;
            }

            // Update our best move.
        }

        static boolean heavyRollout(MonteCarloNode startNode, Comparator<HusBoardState> c) {
            HusBoardState startState = startNode.getState();

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

        // Builds most promising path down to leaf node using minimax on the tree part
        // and then adds one child to the tree using comparator and adds that child to the path.
        //
        // Returns the full path.
        ArrayList<MonteCarloNode> getFrontierPath(Comparator<HusBoardState> c) {

            ArrayList<MonteCarloNode> returnPath = new ArrayList<MonteCarloNode>();
            MonteCarloNode currentNode = this;
            while (!currentNode.children.isEmpty()) {
                returnPath.add(currentNode);

                if (currentNode.isMax) {
                    currentNode.children.sort(statsComparator.reversed()); // Potentially inefficient sort
                } else { // isMin
                    currentNode.children.sort(statsComparator);
                }
                currentNode = currentNode.children.get(0);
            }
            // currentNode now holds the last most promising node in the chain.

            HusBoardState currentState = currentNode.getState();

            ArrayList<HusMove> candidateMoves = currentState.getLegalMoves();
            HusBoardState[] candidateStates = new HusBoardState[candidateMoves.size()];

            // Returns the optimal next node that we should play out.
            for (int i = 0; i < candidateStates.length; i++) {
                HusBoardState newState = (HusBoardState) currentState.clone(); 
                newState.move(candidateMoves.get(i));
                candidateStates[i] = newState;
            }

            MonteCarloNode newNode;

            if (currentNode.isMax) {
                Arrays.sort(candidateStates, c.reversed());
                HusBoardState bestState = candidateStates[0];
                newNode = new MonteCarloNode(bestState, false);
            } else { // isMin
                Arrays.sort(candidateStates, c);
                HusBoardState bestState = candidateStates[0];
                newNode = new MonteCarloNode(bestState, true);
            }

            currentNode.children.add(newNode);

            returnPath.add(newNode);

            return returnPath;
        }

        public HusMove getBestMove() {
            children.sort(statsComparator.reversed());
            return nodeMap.get(children.get(0).getState());
        }

        double getStat() {
            return ((double) numerator / denominator);
        }

        HusBoardState getState() {
            return state;
        }
    }

    public static class MonteCarloSearchThread extends Thread {
        private MonteCarloNode rootNode;
        private HusBoardState rootState;
        private Comparator<HusBoardState> comparator;
        private int myID;

        private HusMove bestMove;

        MonteCarloSearchThread(HusBoardState s, Comparator<HusBoardState> c, int i) {
            rootState = s;
            comparator = c;
            myID = i;
        }

        public void run() {
            rootNode = new MonteCarloNode(rootState, true, myID);
            while (!this.isInterrupted()) {
                rootNode.simulate(comparator);
                setMove(rootNode.getBestMove());
            }
        }

        synchronized void setMove(HusMove m) {
            bestMove = m;
        }

        synchronized HusMove getMove() {
            return bestMove;
        }
    }
}
