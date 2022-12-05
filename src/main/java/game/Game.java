package game;

public class Game {
    private static final int NUMBER_PLAYERS = 2;
    private final Board board;
    private Player[] players;
    private int current;

    /**
     * Creates a new board for the game and stores the values of both players in an array.
     *
     * @param p1 first player
     * @param p2 second player
     */
    public Game(Player p1, Player p2) {
        board = new Board();
        players = new Player[NUMBER_PLAYERS];
        players[0] = p1;
        players[1] = p2;
        current = 0;
    }

    public void reset() {
        current = 0;
        board.reset();
    }

    /**
     * Processes the move on the board.
     *
     * @param index  index of the field we want to change
     * @param rotate value of the rotation corresponding to the server's protocol
     */
    public void registerMove(int index, int rotate) {
        board.processMove(index, rotate, players[current].getMarble());
        current++;
        current %= 2;
    }

    /**
     * Returns the index in the players array of the current player.
     *
     * @return the index of the current player
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Prints the current situation of the game.
     *
     * @return a string containing the board's representation
     */
    public String update() {
        return "\ncurrent game situation: \n\n" + board.toString()
                + "\n";
    }

    /**
     * Prints the result of the game, in case the game has finished.
     *
     * @return a string containing the result of the game
     */
    public String printResult() {
        if (board.gameOver()) {
            if (board.hasWinner()) {
                Player winner = board.isWinner(players[0].getMarble()) ? players[0]
                        : players[1];
                return "Player " + winner.getName() + " ("
                        + winner.getMarble().toString() + ") has won!";
            } else {
                if (board.isFull()) {
                    return "Draw. There is no winner!";
                }
            }
        }
        return null;
    }

    /**
     * Returns the board of the game.
     *
     * @return board object
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Returns the players array of the game that contains both players.
     *
     * @return an array players which has both players
     */
    public Player[] getPlayers() {
        return players;
    }
}
