package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;
public class Functions {
    
    // Abstract class for evaluation functions on HusBoardState states.
    // Eval funcs return a normalized cost from 0 to 100.
    public abstract static class EvaluationFunction {
        abstract int compute(int id, HusBoardState s);
    }

    // Evaluation function factory to produce different heuristics for generic states
    // for the purpose of quickly testing different heuristics.
    public static class EvaluationFunctionFactory {
        public EvaluationFunction getEvaluationFunction(String s) {
            switch (s) {
                case "basic":
                    return new BasicEvaluationFunction();
                default:
                    return new BasicEvaluationFunction();
            }
        }
    }

    // Super basic testing evaluation function that gives a higher score
    // if we have more pits than the opponent.
    // Normalized from 0 to 100.
    static class BasicEvaluationFunction extends EvaluationFunction {
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
            return (((a - b)/48)*100);
        }
    }

    // Improved evaluation function that incorporates more aspects of the game.
    // 1. Higher weighting wrt score for outer loop pits than inner loop pits.
    // 2. We favor states where we have more pits that are available.
    static class ImprovedEvaluationFunction extends EvaluationFunction {
        public int compute(int id, HusBoardState s) {
            float interiorMultiplier = 0.5f;
            float exteriorMultiplier = 1.5f;
            // Exterior pits are 0-16.
            // Interior pits are 17-32.
            


            int[][] pits = s.getPits();
            int[] my_pits = pits[id];
            int[] opp_pits = pits[(id + 1) % 2];
            return 0;
        }
    }
}
