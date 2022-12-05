package ai;

import game.Board;
import game.Marble;

import java.util.Random;

public class NaiveStrategy implements Strategy {
    /**
     * Determines a random move by generating random numbers for the index and rotation
     * and then checking the validity.
     *
     * @param board  the board on which the game takes place
     * @param marble the marble for which we generate the move
     * @return a random generated move for the marble
     */
    @Override
    public Move determineMove(Board board, Marble marble) {
        int n1 = new Random().nextInt(36);
        int n2 = new Random().nextInt(8);
        while (!board.checkMove(n1, n2)) {
            if (!board.gameOver()) {
                n1 = new Random().nextInt(36);
                n2 = new Random().nextInt(8);
            } else {
                return null;
            }
        }
        return new Move(n1, n2);
    }

}
