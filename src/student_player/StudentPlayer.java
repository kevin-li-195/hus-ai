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
        Functions.EvaluationFunctionFactory factory = new Functions.EvaluationFunctionFactory(player_id);
        Functions.EvaluationFunction func = factory.getEvaluationFunction("basic");

        MonteCarlo.SearchThread t = new MonteCarlo.SearchThread(board_state, func, player_id);

        long startTime = System.currentTimeMillis();

        // int STARTING_DEPTH = 3;

        // UNCOMMENT TO SWITCH TO ALPHA-BETA PRUNING.
        // AlphaBeta.SearchThread t = new AlphaBeta.SearchThread(board_state, func, player_id, STARTING_DEPTH);
        t.start();

        HusMove chosenMove;

        long makeTime = System.currentTimeMillis() - startTime;

        try {
            if (board_state.getTurnNumber() > 0) {
                Thread.sleep(1900 - makeTime);
                t.interrupt();
            } else {
                Thread.sleep(29000 - makeTime);
                t.interrupt();
            }
            chosenMove = t.getMove();
        } catch (InterruptedException e) {
            t.interrupt();
            chosenMove = t.getMove();
        }

        // int branchesPruned = t.getPrunedBranches();
        // int branchingFactor = t.getBranchingFactor();

        // System.out.println("Branches pruned: " + branchesPruned);
        // System.out.println("Total top level branching factor: " + branchingFactor);

        return chosenMove;
    }
}
