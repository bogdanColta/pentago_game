package game;

public enum Marble {
    EMPTY, BLACK, WHITE;

    /**
     * Returns the other mark.
     *
     * @return the other mark is this mark is not EMPTY or EMPTY
     */
    //@ ensures this == BLACK ==> \result == WHITE && this == WHITE ==> \result == BLACK;
    public Marble other() {
        if (this == BLACK) {
            return WHITE;
        } else if (this == WHITE) {
            return BLACK;
        } else {
            return EMPTY;
        }
    }
}
