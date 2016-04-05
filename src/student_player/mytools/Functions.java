package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.Comparator;

public class Functions {
    
    // Abstract class for evaluation functions on HusBoardState states.
    // Eval funcs return a normalized cost from 0 to 100.
    public abstract static class EvaluationFunction implements Comparator<HusBoardState> {
        abstract int compute(HusBoardState s);
    }

    // Evaluation function factory to produce different heuristics for generic states
    // for the purpose of quickly testing different heuristics.
    public static class EvaluationFunctionFactory {
        int id;

        public EvaluationFunctionFactory(int i) {
            id = i;
        }
        public EvaluationFunction getEvaluationFunction(String s) {
            switch (s) {
                case "basic":
                    return new BasicEvaluationFunction(id);
                case "improved":
                    return new ImprovedEvaluationFunction(id);
                case "capture":
                    return new CaptureFocusedEvaluationFunction(id);
                default:
                    return new BasicEvaluationFunction(id);
            }
        }
    }

    // Super basic testing evaluation function that gives a higher score
    // if we have more pits than the opponent.
    // Normalized from 0 to 100.
    static class BasicEvaluationFunction extends EvaluationFunction {

        int id = 0;

        public BasicEvaluationFunction(int i) {
            id = i;
        }

        public int compare(HusBoardState o1, HusBoardState o2) {
            int[][] lhs = o1.getPits();
            int[][] rhs = o2.getPits();

            int[] myPitsL = lhs[id];
            int[] myPitsR = rhs[id];
            int[] oppPitsL = lhs[(id + 1) % 2];
            int[] oppPitsR = rhs[(id + 1) % 2];

            int left = 0;
            int right = 0;

            for (int i = 0; i < myPitsL.length; i++) {
                left += myPitsL[i];
                left -= oppPitsL[i];

                right += myPitsR[i];
                right -= oppPitsR[i];
            }

            return (left - right);
        }

        public int compute(HusBoardState s) {
            int[][] pits = s.getPits();
            int[] my_pits = pits[id];
            int[] opp_pits = pits[(id + 1) % 2];
            int a = 0;
            int b = 0;
            for (int i = 0; i < my_pits.length; i++) {
                a += my_pits[i];
                b += opp_pits[i];
            }
            return (a - b);
        }
    }

    // Improved evaluation function that incorporates more aspects of the game.
    // Higher weighting for outer loop pits than inner loop pits.
    static class ImprovedEvaluationFunction extends EvaluationFunction {
        int id;

        public ImprovedEvaluationFunction(int i) {
            id = i;
        }

        public int compare(HusBoardState o1, HusBoardState o2) {
            return 0;
        }

        public int compute(HusBoardState s) {
            float interiorMultiplier = 0.9f;
            float exteriorMultiplier = 1.1f;

            // Exterior pits are 0-16.
            // Interior pits are 17-32.
            
            int myScore = 0;
            int oppScore = 0;

            int[][] pits = s.getPits();
            int[] my_pits = pits[id];
            int[] opp_pits = pits[(id + 1) % 2];

            for (int i = 0; i < 17; i++) {
                myScore += my_pits[i]*exteriorMultiplier;
                oppScore += opp_pits[i]*exteriorMultiplier;
            }
            for (int j = 17; j < 32; j++) {
                myScore += my_pits[j]*exteriorMultiplier;
                oppScore += opp_pits[j]*exteriorMultiplier;
            }
            return Math.round(myScore-oppScore);
        }
    }

    // Evaluation function that favors board states in which we've just captured pieces.
    // Prioritizes difference between player and opp pieces.
    static class CaptureFocusedEvaluationFunction extends EvaluationFunction {
        int id;

        public CaptureFocusedEvaluationFunction(int i) {
            id = i;
        }

        public int compare(HusBoardState o1, HusBoardState o2) {
            return 0;
        }

        public int compute(HusBoardState s) {
            int myScore = 0;
            int oppScore = 0;

            int[][] pits = s.getPits();
            int[] my_pits = pits[id];
            int[] opp_pits = pits[(id + 1) % 2];

            return 0; // placeholder
        }
    }
}
