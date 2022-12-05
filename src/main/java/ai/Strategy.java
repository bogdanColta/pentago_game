package ai;

import game.Board;
import game.Marble;

public interface Strategy {
    /**
     * Determines the move for the board and marble parameters.
     *
     * @param board  the board that the game takes place on
     * @param marble the marble for which we generate the move
     * @return a generated move
     */
    Move determineMove(Board board, Marble marble);

}
