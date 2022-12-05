package ai;

public class Move {
    private int index;
    private int rotation;

    /**
     * Constructor of the class that sets the index and the rotation.
     *
     * @param index    the index of the board
     * @param rotation the rotation that can be applied to a board
     */
    public Move(int index, int rotation) {
        this.index = index;
        this.rotation = rotation;
    }

    /**
     * Returns the index field.
     *
     * @return the index of the move
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the rotation field.
     *
     * @return the rotation of the move
     */
    public int getRotation() {
        return rotation;
    }
}
