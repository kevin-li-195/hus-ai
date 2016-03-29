package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

public class MyTools {

    // Basic, non-admissible heuristic function that returns 
    // 24 - (id - opponent_id) as the heuristic.
    //
    // Make sure to pass the ID of the person for whom we would like 
    // to compute this heuristic.
   
    // Interface for heuristics on generic states.
    abstract class Heuristic {
        abstract int compute(int id, HusBoardState s);
    }

    // Heuristic factory to produce different heuristics for generic states
    // for the purpose of quickly testing different heuristics.
    public class HusHeuristicFactory {
        public Heuristic getHeuristic(String s) {
            switch (s) {
                case default: 
            }
        }
    }

    class BasicHeuristic extends Heuristic {
        public int compute(
        int[][] pits = s.getPits();
        int[] my_pits = pits[id];
        int[] opp_pits = pits[(id + 1) % 2];
        int a = 0;
        int b = 0;
        for (int i = 0; i < my_pits.length; i++) {
            a += my_pits[i];
            b += opp_pits[i];
        }
        return (24 - (a - b));
    }
}
