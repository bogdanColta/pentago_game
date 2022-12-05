package game;

public class Player {
    private final String name;
    private final Marble marble;

    /**
     * Initializes the fields name and marble.
     *
     * @param name   the value we want to set name to
     * @param marble the value we want to set marble to
     */
    public Player(String name, Marble marble) {
        this.name = name;
        this.marble = marble;
    }

    /**
     * Returns the field name.
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the field marble.
     *
     * @return the value of marble
     */
    public Marble getMarble() {
        return marble;
    }

}
