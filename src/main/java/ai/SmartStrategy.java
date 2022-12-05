package ai;

import game.Board;
import game.Marble;

import java.util.Random;

public class SmartStrategy implements Strategy {
    /**
     * Determines a move by using a strategy.
     *
     * @param board  the board that the game takes place on
     * @param marble the marble for which we generate the move
     * @return a move determined by a strategy
     */
    @Override
    public Move determineMove(Board board, Marble marble) {
        Move move;
        //first the method check whether the field 7, 10, 25 and 28
        // are empty, if yes a move containing this index is returned.
        if (!board.gameOver()) {
            if (board.getField(7).equals(Marble.EMPTY)) {
                return new Move(7, 0);
            }

            if (board.getField(10).equals(Marble.EMPTY)) {
                return new Move(10, 0);
            }

            if (board.getField(25).equals(Marble.EMPTY)) {
                return new Move(25, 0);
            }

            if (board.getField(28).equals(Marble.EMPTY)) {
                return new Move(28, 0);
            }
            //the method checks whether there is a winning move for the
            // marble parameter and in case there is, it is returned
            move = check(board, marble);
            if (move != null) {
                return move;
            }

            move = check(board, marble.other());

            if (move != null) {
                return move;
            }
            //if all the cases were false, in the end
            // the method generates a random move

            //registers in an array all the indexes of empty fields.
            int[] ar = new int[36];
            int c = 0;
            for (int i = 0; i < 36; i++) {
                if (board.getField(i).equals(Marble.EMPTY)) {
                    ar[c] = i;
                    c++;
                }
            }
            //choose random rotation and random index from the empty indexes
            int index = new Random().nextInt(c);
            int rotation = new Random().nextInt(8);

            return new Move(ar[index], rotation);
        } else {
            return null;
        }

    }

    /**
     * Checks whether there is a winning move for the marble parameter. If yes, it returns the move.
     * Otherwise, it returns null.
     *
     * @param board  the board that the game takes place on
     * @param marble the marble for which we generate the move
     * @return the winning move or null in case there is no winning move
     */
    private Move check(Board board, Marble marble) {
        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.checkMove(i, j)) {
                    Board copy = board.copy();
                    copy.processMove(i, j, marble);
                    if (copy.isWinner(marble)) {
                        return new Move(i, j);
                    }
                }
            }
        }
        return null;
    }
}
