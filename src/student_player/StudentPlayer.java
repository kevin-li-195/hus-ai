package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;

import student_player.mytools.*;

/** A Hus player submitted by a student. */
public class StudentPlayer extends HusPlayer {

    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() { super("260565522"); }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state)
    {
        // Have available:
        //  - player_id
        //  - opponent_id

        int MAX_DEPTH = 5;

        long startTime = System.currentTimeMillis();
        Functions.EvaluationFunctionFactory factory = new Functions.EvaluationFunctionFactory();
        Functions.EvaluationFunction func = factory.getEvaluationFunction("improved");

        AlphaBeta.SearchThread t = new AlphaBeta.SearchThread(board_state, func, player_id, MAX_DEPTH);
        t.start();

        HusMove chosenMove;

        long makeTime = System.currentTimeMillis() - startTime;

        try {
            if (board_state.getTurnNumber() > 0) {
                Thread.sleep(1900 - makeTime);
            } else {
                Thread.sleep(29000 - makeTime);
            }
            chosenMove = t.getMove();
        } catch (InterruptedException e) {
            chosenMove = t.getMove();
        }
        t.interrupt();
        int branchesPruned = t.getPrunedBranches();
        int topLevelBranchesSearched = t.getSearchedBranches();
        int branchingFactor = t.getBranchingFactor();

        System.out.println("Branches pruned: " + branchesPruned);
        System.out.println("Top level branches searched: " + topLevelBranchesSearched);
        System.out.println("Total top level branching factor: " + branchingFactor);

        return chosenMove;
    }
}
