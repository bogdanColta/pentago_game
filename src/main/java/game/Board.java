package game;

public class Board {
    /*@ private invariant fields.length == DIM*DIM;
        private invariant (\forall int i; (i >= 0 && i < DIM*DIM); fields[i] ==
        Marble.EMPTY || fields[i] == Marble.BLACK || fields[i] == Marble.WHITE);
    @*/

    private static final int DIM = 6;
    private static final String DELIM = "              ";
    private static final String LINE = "----+----+----+    +----+----+----";
    private static final String[] NUMBERING = {" 0  | 1  | 2  |    | 3  | 4  | 5" +
            "                Rotations: ", LINE,
        " 6  | 7  | 8  |    | 9  | 10 | 11        " +
                    "0: Top left counter-clockwise          1: Top left clockwise", LINE,
        " 12 | 13 | 14 |    | 15 | 16 | 17        " +
                    "2: Top right counter-clockwise         3: Top right clockwise ", LINE,
        " 18 | 19 | 20 |    | 21 | 22 | 23        " +
                    "4: Bottom left counter-clockwise       5: Bottom left clockwise", LINE,
        " 24 | 25 | 26 |    | 27 | 28 | 29        " +
                    "6: Bottom right counter-clockwise      7: Bottom right clockwise", LINE,
        " 30 | 31 | 32 |    | 33 | 34 | 35 "};


    private final Marble[] fields;

    /**
     * Initializes the fields array and setting each one to the empty value.
     */
    public Board() {
        fields = new Marble[36];
        for (int a = 0; a < 36; a++) {
            fields[a] = Marble.EMPTY;
        }
    }

    // -- Constructors -----------------------------------------------

    /**
     * Creates a copy of the current board.
     *
     * @return a board that is a copy of the current board
     */
    /*@ ensures \result != this;
        ensures (\forall int i; (i >= 0 && i < DIM*DIM); \result.fields[i] == this.fields[i]);
     @*/
    public Board copy() {
        Board newBoard = new Board();
        for (int a = 0; a < DIM * DIM; a++) {
            newBoard.fields[a] = fields[a];
        }
        return newBoard;
    }

    /*@ requires row >= 0 && row < DIM;
        requires col >= 0 && row < DIM;
     @*/
    /*@ pure */
    private int index(int row, int col) {
        return DIM * row + col;
    }

    //@ ensures index >= 0 && index < DIM*DIM ==> \result == true;
    /*@ pure */
    private boolean isField(int index) {
        return index < DIM * DIM && index >= 0;
    }

    /**
     * Checks if the row and col are of a valid field.
     *
     * @param row row of the field we want to check
     * @param col column of the field we want to check
     * @return whether the field is valid or not
     */
    //@ ensures row >= 0 && row < DIM && col >= 0 && col < DIM ==> \result == true;
    /*@ pure */
    private boolean isField(int row, int col) {
        return row >= 0 && row <= 6 && col >= 0 && col < 6;
    }

    /**
     * Returns the value of the field at the corresponding index.
     * Returns null if the index is not valid.
     *
     * @param index index of the field
     * @return the value of the field at the given index
     */
    /*@ requires isField(index);
        ensures \result == Marble.EMPTY || \result == Marble.BLACK || \result == Marble.WHITE;
     @*/
    /*@ pure */
    public Marble getField(int index) {
        if (isField(index)) {
            return fields[index];
        }
        return null;
    }

    /**
     * Returns the value of the field at the corresponding row and column.
     * Returns null if the index of row and column is not valid.
     *
     * @param row row of the field we want to check
     * @param col column of the field we want to check
     * @return the value of the field at the given row and column
     */
    /*@ requires isField(row, col);
        ensures \result == Marble.EMPTY || \result == Marble.WHITE || \result == Marble.BLACK;
     @*/
    /*@ pure */
    public Marble getField(int row, int col) {
        if (isField(row, col)) {
            return fields[index(row, col)];
        }
        return null;
    }

    /**
     * Checks if the field at the corresponding index is empty.
     *
     * @param index index of the field we want to check
     * @return whether the field is empty or not
     */
    /*@ requires isField(index);
        ensures getField(index) == Marble.EMPTY ==> \result == true;
     @*/
    /*@ pure */
    private boolean isEmptyField(int index) {
        return isField(index) && fields[index].equals(Marble.EMPTY);
    }

    /**
     * Check if the board is full.
     *
     * @return whether the board has at least one empty field or not.
     */
    /*@ ensures (\forall int i; (i >= 0 && i < DIM*DIM);
        fields[i] == Marble.BLACK || fields[i] == Marble.BLACK);
    @*/
    /*@ pure */public boolean isFull() {
        for (int i = 0; i < 36; i++) {
            if (fields[i].equals(Marble.EMPTY)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the game is finished.
     *
     * @return whether the board is full or a player has won the game.
     */
    //@ ensures isFull() || hasWinner() ==> \result == true;
    /*@ pure */
    public boolean gameOver() {
        return isFull() || hasWinner();
    }

    /**
     * Checks whether the board has 5 marbles consecutive on a major diagonal.
     *
     * @param marble marble we want to check if it has a winning major diagonal
     * @return whether the marble has a major diagonal
     */
    /*@ requires marble != null;
        ensures (\forall int i; (i >= 0 && i < DIM);
        this.fields[index(i,i)] == marble) ==> \result == true;
    @*/
    /*@ pure */private boolean hasMajorDiagonal(Marble marble) {
        boolean[] checker = new boolean[4];

        for (int a = 0; a < 5; a++) {
            if (!fields[index(a, a)].equals(marble)) {
                checker[0] = true;
            }
            if (!fields[index(a + 1, a + 1)].equals(marble)) {
                checker[1] = true;
            }
        }

        for (int a = 5; a >= 1; a--) {
            if (!fields[index(5 - a, a)].equals(marble)) {
                checker[2] = true;
            }
            if (!fields[index(6 - a, a - 1)].equals(marble)) {
                checker[3] = true;
            }
        }
        return !checker[0] || !checker[1] || !checker[2] || !checker[3];
    }

    /**
     * Checks whether the board has 5 marbles consecutive on a minor diagonal.
     *
     * @param marble marble we want to check if it has a winning minor diagonal
     * @return whether the marble has a minor diagonal
     */
    /*@ requires marble != null;
        ensures (\forall int i; (i >= 0 && i < DIM);
        this.fields[index(i,i + 1)] == marble) ==> \result == true;
    @*/
    /*@ pure */private boolean hasMinorDiagonal(Marble marble) {
        boolean[] checker = new boolean[4];

        for (int a = 0; a < 5; a++) {
            if (!fields[index(a, a + 1)].equals(marble)) {
                checker[0] = true;
            }
            if (!fields[index(a + 1, a)].equals(marble)) {
                checker[1] = true;
            }
        }

        for (int a = 0; a < 5; a++) {
            if (!fields[index(a, 4 - a)].equals(marble)) {
                checker[2] = true;
            }
            if (!fields[index(a + 1, 5 - a)].equals(marble)) {
                checker[3] = true;
            }
        }

        return !checker[0] || !checker[1] || !checker[2] || !checker[3];
    }

    /**
     * Checks whether the board has 5 marbles consecutive on a row.
     *
     * @param marble marble we want to check if it has a winning row
     * @return whether the marble has a winning row
     */
    /*@
        requires marble != null;
        ensures (\forall int i; (i >= 0 && i < DIM);
        this.fields[index(0, i)] == marble) ==> \result == true;
    @*/
    /*@ pure */private boolean hasHorizontalLine(Marble marble) {
        boolean[] checker = new boolean[2];

        for (int a = 0; a < 6; a++) {
            checker = new boolean[2];
            for (int j = 0; j < 5; j++) {
                if (!fields[index(a, j)].equals(marble)) {
                    checker[0] = true;
                    break;
                }
            }
            if (!checker[0]) {
                break;
            }

            for (int j = 1; j < 6; j++) {
                if (!fields[index(a, j)].equals(marble)) {
                    checker[1] = true;
                    break;
                }
            }
            if (!checker[1]) {
                break;
            }
        }
        return !checker[0] || !checker[1];
    }

    /**
     * Checks whether the board has 5 marbles consecutive on a column.
     *
     * @param marble marble we want to check if it has a winning column
     * @return whether the marble has a winning column
     */
    /*@
        requires marble != null;
        ensures (\forall int i; (i >= 0 && i < DIM);
        this.fields[index(i, 0)] == marble) ==> \result == true;
    @*/
    /*@ pure */private boolean hasVerticalLine(Marble marble) {
        boolean[] checker = new boolean[2];

        for (int a = 0; a < 6; a++) {
            checker = new boolean[2];
            for (int j = 0; j < 5; j++) {
                if (!fields[index(j, a)].equals(marble)) {
                    checker[0] = true;
                    break;
                }
            }
            if (!checker[0]) {
                break;
            }

            for (int j = 1; j < 6; j++) {
                if (!fields[index(j, a)].equals(marble)) {
                    checker[1] = true;
                    break;
                }
            }
            if (!checker[1]) {
                break;
            }

        }
        return !checker[0] || !checker[1];
    }

    /**
     * Checks if the given marble is the winner by checking each winning case.
     *
     * @param marble marble we want to check if it is a winner
     * @return whether the given marble is the winner
     */
    /*@
        requires marble == Marble.BLACK || marble == Marble.WHITE;
        ensures hasHorizontalLine(marble) || hasVerticalLine(marble) ||
        hasMajorDiagonal(marble) || hasMinorDiagonal(marble)==> \result == true;
     @*/
    /*@ pure */public boolean isWinner(Marble marble) {
        return marble != Marble.EMPTY &&
                (hasVerticalLine(marble) ||
                        hasMajorDiagonal(marble) ||
                        hasMinorDiagonal(marble) || hasHorizontalLine(marble));
    }


    /**
     * Checks if the board has a winner.
     *
     * @return if the board has a winner or not
     */
    //@ ensures isWinner(Marble.BLACK) || isWinner(Marble.WHITE) ==> \result == true;
    /*@ pure */public boolean hasWinner() {
        return isWinner(Marble.BLACK) || isWinner(Marble.WHITE);
    }


    /**
     * Changes the subboard into its matrix transpose corresponding to the given row and column.
     *
     * @param row row of the starting index for the subboard
     * @param col col of the starting index for the subboard
     */
    /*@ requires row > 0 && row < 6 && col > 0 && col < 6;
        ensures (\forall int i; (i >= 0 && i < row + 3);(\forall int j; (j >= 0 && j < col + 3);
        fields[index(i, j)] == \old(fields[index(j - col + row, i + col - row)])));
     @*/
    private void calculateTranspose(int row, int col) {
        Marble[] copy = fields.clone();
        for (int i = row; i < (row + 3); i++) {
            for (int j = col; j < (col + 3); j++) {
                fields[index(i, j)] = copy[index(j - col + row, i + col - row)];
            }
        }
    }

    //The method rotates the subboard to the left by replacing the
    // first column of the transpose matrix with the third column of
    // the subboard. The transpose is calulated with another method.

    /**
     * Rotates to the left the subboard corresponding to the given row and column.
     *
     * @param row row of the starting index for the subboard
     * @param col col of the starting index for the subboard
     */
    /*@ requires row > 0 && row < 6 && col > 0 && col < 6;
        ensures (\forall int i; (i >= 0 && i < col + 3);
        fields[index(row, i)] == \old(fields[index(row + 2, i)]) &&
        fields[index(row + 2, i)] == \old(fields[index(row, i)]));
     @*/
    public void rotateLeft(int row, int col) {
        calculateTranspose(row, col);
        for (int i = col; i < (col + 3); i++) {
            Marble temp = fields[index(row, i)];
            fields[index(row, i)] = fields[index(row + 2, i)];
            fields[index(row + 2, i)] = temp;
        }
    }

    /**
     * Rotates to the right the subboard corresponding to the given row and column.
     *
     * @param row row of the starting index for the subboard
     * @param col col of the starting index for the subboard
     */
    /*@ requires row > 0 && row < 6 && col > 0 && col < 6;
        ensures (\forall int i; (i >= 0 && i < row + 3);
        fields[index(i, col)] == \old(fields[index(i, col + 2)]) &&
        fields[index(i, col + 2)] == \old(fields[index(i, col)]));
     @*/
    public void rotateRight(int row, int col) {
        calculateTranspose(row, col);
        for (int i = row; i < (row + 3); i++) {
            Marble temp = fields[index(i, col)];
            fields[index(i, col)] = fields[index(i, col + 2)];
            fields[index(i, col + 2)] = temp;
        }

    }

    /**
     * Resets the board by setting each field to empty.
     */
    //@ ensures (\forall int i; (i >= 0 && i < DIM*DIM); fields[i] == Marble.EMPTY);
    public void reset() {
        for (int i = 0; i < 36; i++) {
            fields[i] = Marble.EMPTY;
        }
    }

    /**
     * Setts the value of the field at the given index to the given value.
     *
     * @param index  index of the field
     * @param marble marble that we want to change the field to
     */
    /*@ requires isField(index);
    ensures getField(index) == marble;
     @*/
    public void setField(int index, Marble marble) {
        if (isField(index)) {
            fields[index] = marble;
        }
    }

    //The method applies the move by setting the specified field and making the rotation.
    //We use a switch to make all possible rotations.
    //The row and column as the parameters show which subboard we are working with.
    //It takes the index of row and column and uses it as the top left cell of the 3x3 subboard.
    //So 0, 0 is for the top left subboard
    //0, 3 for the top right
    //3, 0 for the bottom left
    //3, 3 for bottom right

    /**
     * Setts the value of the field at the given row and column to the given value.
     *
     * @param row    row of the field
     * @param col    column of the field
     * @param marble marble that we want to change the field to
     */
    /*@ requires isField(row, col);
        ensures getField(row, col) == marble;
     @*/
    public void setField(int row, int col, Marble marble) {
        if (isField(row, col)) {
            fields[index(row, col)] = marble;
        }
    }

    /**
     * Processes the move by setting the marble on the field at
     * the corresponding field and then making the rotation.
     *
     * @param field  index of the field we want to change
     * @param rotate value of the rotation corresponding to the server's protocol
     * @param marble marble that we want to change the field to
     */
    /*@ requires field >= 0 && field < 36 && rotate >=0 && rotate < 8;
        ensures getField(field) != marble.EMPTY;
     @*/
    public void processMove(int field, int rotate, Marble marble) {
        setField(field, marble);
        switch (rotate) {
            case 0:
                rotateLeft(0, 0);
                break;
            case 1:
                rotateRight(0, 0);
                break;
            case 2:
                rotateLeft(0, 3);
                break;
            case 3:
                rotateRight(0, 3);
                break;
            case 4:
                rotateLeft(3, 0);
                break;
            case 5:
                rotateRight(3, 0);
                break;
            case 6:
                rotateLeft(3, 3);
                break;
            case 7:
                rotateRight(3, 3);
                break;
        }
    }

    /**
     * Checks if the move is valid.
     *
     * @param index  index of the field
     * @param rotate value of the rotation corresponding to the server's protocol
     * @return whether the move is valid or not
     */
    /*@ requires index >= 0 && index < 36 && rotate >=0 && rotate < 8;
        ensures isEmptyField(index) ==> \result == true;
     @*/
    /*@ pure */public boolean checkMove(int index, int rotate) {
        return rotate >= 0 && rotate <= 7 && isEmptyField(index);
    }

    /**
     * Adds the board representation to a string.
     *
     * @return a string that contains the board's representation
     */
    /*@ pure */public String toString() {
        String s = "";
        for (int i = 0; i < DIM; i++) {
            String row = "";
            for (int j = 0; j < DIM; j++) {
                row = row + " "
                        + getField(i, j).toString().substring(0, 1).replace("E", " ") + "  ";
                if (j < DIM - 1 && j != 2) {
                    row = row + "|";
                }
                if (j == 2) {
                    row = row + "|    |";
                }
            }
            if (i == 3) {
                s = s + "\n" + LINE + DELIM + LINE + "\n";
            }
            s = s + row + DELIM + NUMBERING[i * 2];
            if (i < DIM - 1) {
                s = s + "\n" + LINE + DELIM + NUMBERING[i * 2 + 1] + "\n";
            }
        }
        return s;
    }
}
