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

        int STARTING_DEPTH = 4;

        long startTime = System.currentTimeMillis();
        Functions.EvaluationFunctionFactory factory = new Functions.EvaluationFunctionFactory(player_id);
        Functions.EvaluationFunction func = factory.getEvaluationFunction("capture");

        AlphaBeta.SearchThread t = new AlphaBeta.SearchThread(board_state, func, player_id, STARTING_DEPTH);
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

        //print move pretty move when it's chosen.
        //System.out.println(chosenMove.toPrettyString());
        int branchesPruned = t.getPrunedBranches();
        int branchingFactor = t.getBranchingFactor();

        System.out.println("Branches pruned: " + branchesPruned);
        System.out.println("Total top level branching factor: " + branchingFactor);

        return chosenMove;
    }
}
