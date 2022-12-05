package network.client;

import ai.Move;
import ai.NaiveStrategy;
import ai.SmartStrategy;
import ai.Strategy;
import exceptions.ConnectionFailed;
import exceptions.InvalidMessage;
import exceptions.InvalidUsername;
import game.Game;
import game.Marble;
import game.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements GameClient, Runnable {
    static Socket socket;
    static PrintWriter writer;
    static BufferedReader in;
    private static List<Listener> gameListeners;
    private String username;
    private Player playerClient;
    private Player playerOpponent;
    private Marble marble;
    private Game game;
    private int current;
    private boolean hasAi;
    private boolean strategy;
    private Strategy strategyEasy;
    private Strategy strategyHard;
    private boolean autoQueue;
    private boolean queue;

    /**
     * Add listener parameter to the gameListeners list.
     *
     * @param listener the listener added to the list
     */
    @Override
    public void addListener(Listener listener) {
        if (gameListeners == null) {
            gameListeners = new ArrayList<>();
        }
        gameListeners.add(listener);
    }

    /**
     * Remove listener parameter to the gameListeners list.
     *
     * @param listener the listener removed to the list
     */
    @Override
    public void removeListener(Listener listener) {
        gameListeners.remove(listener);
    }

    /**
     * Sends the username parameter to the server. Throws the InvalidUsername exception
     * in case the username is null, or it contains the character '~'.
     *
     * @param name the username sent to the server
     * @throws InvalidUsername when the username parameter is null, or has '~' in it
     */
    @Override
    public void sendUsername(String name) throws InvalidUsername {
        if (name == null || name.contains("~")) {
            throw new InvalidUsername("Incorrect username");
        }
        this.username = name;
        writer.println("LOGIN~" + name);
    }

    /**
     * Returns whether the num parameter can be changed from string to integer by catching
     * the NumberFormatException in case the string is not a number. Also returns false in
     * case the parameter is null.
     *
     * @param num the string number we want to check
     * @return whether the string is a number
     */
    public boolean isNumeric(String num) {
        if (num == null) {
            return false;
        }
        try {
            int number = Integer.parseInt(num);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Receives and handles commands that are coming from the TUI.
     * It uses a switch to check all possible incoming commands.
     *
     * @param message message containing the command
     * @throws InvalidMessage if the message is null
     */
    @Override
    public void sendMessage(String message) throws InvalidMessage {
        if (message == null) {
            throw new InvalidMessage("Null message");
        }

        String[] lines = message.split(" ");
        String command = lines[0];
        switch (command) {
            //for 'move' it will register the move in the game and send it to the server.
            case "move":
                //checks whether the client is in a game
                if (game != null) {
                    //checks if the strings are numbers
                    if (isNumeric(lines[1]) && isNumeric(lines[2])) {
                        //checks if the move is legal
                        if (lines.length != 3 ||
                                !game.getBoard().checkMove(Integer.parseInt(lines[1]),
                                        Integer.parseInt(lines[2]))) {
                            sendToListener("Invalid move! \n Write a new move");
                        } else {
                            //checks whether the client is the current player
                            if (game.getCurrent() != current) {
                                sendToListener("Opponent's turn now! \n Wait for his move");
                            } else {
                                //sends the move to the server
                                writer.println("MOVE~" + lines[1] + "~" + lines[2]);
                            }
                        }
                    } else {
                        //sends to the listeners in case the command is wrong
                        sendToListener("Wrong command!");
                    }
                } else {
                    //sends to the listeners if they are not in a game
                    sendToListener("You should be in a game to use this command");
                }
                break;

            case "hint":
                //checks whether the client is in a game
                if (game != null) {
                    //checks whether the command has two words
                    if (lines.length == 2) {
                        //checks whether the client is already using tha AI
                        if (!hasAi) {
                            //checks whether the difficulty
                            if (lines[1].equals("easy")) {
                                strategy = false;
                            } else if (lines[1].equals("hard")) {
                                strategy = true;
                            } else {
                                sendToListener("Wrong Command!");
                                break;
                            }
                            Move move;

                            //checks the strategy and determines the move
                            if (strategy) {
                                move = strategyHard.determineMove(getGame().getBoard(), marble);
                            } else {
                                move = strategyEasy.determineMove(getGame().getBoard(), marble);
                            }
                            //send the hinted move to the listeners
                            sendToListener("Suggested move by the ai is " + move.getIndex()
                                    + " " + move.getRotation());
                            strategy = false;
                        } else {
                            sendToListener("The ai is already playing the game");
                        }
                    } else {
                        sendToListener("Wrong Command!");
                    }
                } else {
                    sendToListener("You should be in a game to use this command");
                }
                break;

            //for 'play' it will turn on the AI for the next match
            case "play":
                //checks whether the client is not in a game and that the command has 3 words
                if (lines.length == 3 && game == null) {
                    //checks whether the client is already using the AI
                    if (!hasAi) {
                        //checks the difficulty of the AI that is in the command
                        if (lines[2].equals("easy") && lines[1].equals("ai")) {
                            strategy = false;
                            sendToListener("The ai (easy difficulty) " +
                                    "will play the next matches");
                        } else if (lines[2].equals("hard") && lines[1].equals("ai")) {
                            strategy = true;
                            sendToListener("The ai (hard difficulty) " +
                                    "will play the next matches");
                        } else {
                            sendToListener("Wrong Command!");
                            break;
                        }
                        hasAi = true;
                    }
                } else {
                    if (game != null) {
                        sendToListener("Cannot change auto-playing ai options during a game");
                    } else if (message.equals("play ai")) {
                        if (hasAi) {
                            sendToListener("The ai playing was deactivated");
                        } else {
                            sendToListener("The ai is already deactivated");
                        }
                        hasAi = false;
                        if (autoQueue) {
                            System.out.println("Exiting auto mode");
                        }
                    } else {
                        sendToListener("Wrong Command!");
                    }
                }
                break;
            //for 'queue' it sends to the server that it wants to join/quit the queue.
            case "queue":
                //checks whether the client is already in a game
                if (game == null) {
                    writer.println("QUEUE");
                    if (!queue) {
                        queue = true;
                        sendToListener("Entering queue");
                    } else {
                        queue = false;
                        sendToListener("Exiting queue");
                        if (autoQueue) {
                            sendToListener("Exiting auto-queue");
                            autoQueue = false;
                        }
                    }
                } else {
                    sendToListener("Cannot use this command while in game");
                }

                break;

            //for 'list' it sends the server the list keyword according to the protocol.
            case "list":
                writer.println("LIST");
                break;
            //for 'quit' it sends the server the quit keyword according to the protocol
            case "quit":
                writer.println("QUIT");
                break;

            //for 'rank' it sends the server the rank keyword according to the protocol.
            case "rank":
                writer.println("RANK");
                break;
            //for 'autoqueue' it changes the autoqueue boolean variable to true/false
            // to show that the auto-queue toggle is on/off.
            case "autoqueue":
                setAutoQueue(true);
                break;
            //for 'send' it checks whether the message is global or
            //private and sends it to the server according to the protocol.
            case "send":
                //checks whether the command has two words
                if (lines.length > 1 && lines[1].equals("everyone:")) {
                    String s = "CHAT~";
                    //add the whole message to the string
                    int length = lines[0].length() + lines[1].length() + 2;
                    for (int i = length; i < message.length(); i++) {
                        s += message.charAt(i);
                    }
                    writer.println(s);
                    sendToListener("Message sent");
                    //checks whether the command has to three words
                } else if (lines.length > 2 &&
                        lines[1].equals("to") && lines[2].length() >= 2) {
                    Character c = lines[2].charAt(lines[2].length() - 1);
                    if (c.equals(':')) {
                        String s = "WHISPER~" + lines[2];
                        s = s.substring(0, s.length() - 1);
                        s += "~";
                        //add the whole message to the string
                        int length = lines[0].length() +
                                lines[1].length() + lines[2].length() + 3;
                        for (int i = length; i < message.length(); i++) {
                            s += message.charAt(i);
                        }
                        writer.println(s);
                        sendToListener("Message sent");
                    } else {
                        sendToListener("Wrong Command!");
                    }
                } else {
                    sendToListener("Wrong Command!");
                }
                break;
            default:
                sendToListener("Wrong Command!");
        }
    }

    /**
     * Closes the socket and removes the TUI from the listeners.
     */
    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        removeListener(gameListeners.get(0));
    }

    /**
     * Send message to the listeners.
     *
     * @param line the message sent to the listeners
     */
    private void sendToListener(String line) {
        for (int i = 0; i < gameListeners.size(); i++) {
            gameListeners.get(i).messageReceived(line);
        }
    }

    /**
     * Returns the private field marble of the class.
     *
     * @return the field marble
     */
    public Marble getMarble() {
        return marble;
    }

    /**
     * Returns whether the AI is on.
     *
     * @return the field hasAi
     */
    public boolean isHasAi() {
        return hasAi;
    }

    /**
     * Sets the value of the field hasAi to the value of the parameter.
     *
     * @param hasAi the value we want to set the hasAi field to.
     */
    public void setHasAi(boolean hasAi) {
        this.hasAi = hasAi;
    }

    /**
     * Returns the value of the field autoqueue.
     *
     * @return the field autoqueue
     */
    public boolean getAutoQueue() {
        return autoQueue;
    }

    /**
     * Set the value of the autoqueue field to the value of the parameter.
     *
     * @param queue1 the value we want to set the autoqueue field to
     */
    public void setAutoQueue(boolean queue1) {
        this.autoQueue = queue1;
        if (autoQueue) {
            try {
                sendMessage("queue");
            } catch (InvalidMessage e) {
            }
        }
    }

    /**
     * Set the value of the strategy field to the value of the parameter.
     *
     * @param strategy the value we want to set the strategy field to
     */
    public void setStrategy(boolean strategy) {
        this.strategy = strategy;
    }

    /**
     * Connects to the socket of the server, saves the streams of the socket
     * and sends the server the hello message.
     *
     * @param address the address of the server socket
     * @param port    the port of the server socket
     * @throws ConnectionFailed if the connection failed
     */
    @Override
    public void connect(String address, int port) throws ConnectionFailed {
        game = null;
        boolean connection = true;
        try {
            socket = new Socket(address, port);
        } catch (IOException e) {
            connection = false;
        }

        if (!connection) {
            throw new ConnectionFailed("Could not connect! Invalid port or address.");
        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("HELLO~Client by bogdan & alex~RANK~CHAT");
        } catch (IOException e) {
            close();
        }
        new Thread(this).start();

    }

    /**
     * Returns the game of the client.
     *
     * @return the field game
     */
    public Game getGame() {
        return game;
    }

    /**
     * The run method of the Runnable Class Client
     * The main use is that it receives the messages from the server according
     * to the protocol and then acts according to them.
     */
    @Override
    public void run() {
        String line;

        try {
            while ((line = in.readLine()) != null) {
                String s;
                int length;
                String list;
                String[] lines = line.split("~");
                String command = lines[0];
                switch (command) {
                    //For the command 'MOVE' it registers the move of the player and
                    // then sends the listener whether it is his turn.
                    case "MOVE":
                        sendToListener("Player " + game.getPlayers()[game.getCurrent()].getName()
                                + " made the move " + lines[1] + " " + lines[2]);
                        game.registerMove(Integer.parseInt(lines[1]), Integer.parseInt(lines[2]));
                        sendToListener(game.update());
                        //checks whether the client is the current player
                        if (game.getCurrent() == current) {
                            sendToListener("It is your turn now!");
                            if (hasAi) {
                                if (strategy) {
                                    Move move = strategyHard.determineMove(
                                            getGame().getBoard(), marble);
                                    if (move != null) {
                                        sendMessage("move " + move.getIndex()
                                                + " " + move.getRotation());
                                    }
                                } else {
                                    Move move = strategyEasy.determineMove(
                                            getGame().getBoard(), marble);
                                    if (move != null) {
                                        sendMessage("move " + move.getIndex()
                                                + " " + move.getRotation());
                                    }
                                }
                            } else {
                                sendToListener("Enter a move using the format:" +
                                        " move index rotation");
                            }
                        } else {
                            sendToListener("It is opponent's turn now! ");
                        }
                        break;

                    //For the 'LOGIN' command it sends the listener a message
                    // that he has successfully logged in.
                    case "LOGIN":
                        sendToListener("Logged in");
                        strategyEasy = new NaiveStrategy();
                        strategyHard = new SmartStrategy();
                        break;

                    //For the 'ALREADYLOGGEDIN' command it sends the listener a message
                    // that the username is already used
                    case "ALREADYLOGGEDIN":
                        sendToListener("There is already a user with this username:");
                        break;

                    //For the 'NEWGAME' command it send the listener a message that
                    // there is a new game.
                    case "NEWGAME":
                        if (username.equals(lines[1])) {
                            playerClient = new Player(lines[1], Marble.BLACK);
                            marble = Marble.BLACK;
                            playerOpponent = new Player(lines[2], Marble.WHITE);
                            game = new Game(playerClient, playerOpponent);
                            current = 0;
                        } else {
                            playerOpponent = new Player(lines[1], Marble.BLACK);
                            playerClient = new Player(lines[2], Marble.WHITE);
                            marble = Marble.WHITE;
                            game = new Game(playerOpponent, playerClient);
                            current = 1;
                        }
                        sendToListener("New game created: " + lines[1] + " vs " + lines[2]);
                        sendToListener(game.update());
                        if (game.getCurrent() == current) {
                            sendToListener("It is your turn now! ");
                            //checks whether the AI is on
                            if (hasAi) {
                                //checks which strategy is used
                                if (strategy) {
                                    Move move = strategyHard.determineMove(
                                            getGame().getBoard(), marble);
                                    if (move != null) {
                                        sendMessage("move " + move.getIndex()
                                                + " " + move.getRotation());
                                    }
                                } else {
                                    Move move = strategyEasy.determineMove(
                                            getGame().getBoard(), marble);
                                    if (move != null) {
                                        sendMessage("move " + move.getIndex()
                                                + " " + move.getRotation());
                                    }
                                }
                            } else {
                                sendToListener("Enter a move using the format:" +
                                        " move index rotation");
                            }
                        } else {
                            sendToListener("It is opponent's turn now! ");
                        }
                        break;
                    //For the 'LIST' command it sends the list of players to the listeners
                    case "LIST":
                        list = "List of players: ";
                        for (int i = 1; i < lines.length; i++) {
                            if (i == lines.length - 1) {
                                list += lines[i] + ".";
                            } else {
                                list += lines[i] + ", ";
                            }
                        }
                        sendToListener(list);
                        break;

                    case "PONG":
                        break;

                    //For the 'PING' command it sends 'PONG' to the server
                    case "PING":
                        writer.println("PONG");
                        break;

                    //For the 'GAMEOVER' command it notifies the listeners that the game
                    //has the finished and the reason it finished.
                    case "GAMEOVER":
                        String reason = lines[1];
                        if (reason.equals("DRAW")) {
                            sendToListener("The game ended in a draw");
                        }
                        if (reason.equals("DISCONNECT")) {
                            sendToListener("Player " + lines[2]
                                    + " has won the game because the other player disconnected!");
                        }
                        if (reason.equals("VICTORY")) {
                            sendToListener("Player " + lines[2] + " has won the game!");
                        }
                        queue = false;
                        game = null;
                        if (autoQueue) {
                            sendMessage("queue");
                        }
                        break;
                    //For the 'RANK' it sends to the listeners a list containing
                    //all the ranks.
                    case "RANK":
                        list = "List of players' ranks: ";
                        for (int i = 1; i < lines.length; i++) {
                            if (i != lines.length - 1) {
                                if (i % 2 == 1) {
                                    list += lines[i] + " has ";
                                } else {
                                    list += lines[i] + " wins, ";
                                }
                            } else {
                                if (i % 2 == 1) {
                                    list += lines[i] + " has ";
                                } else {
                                    list += lines[i] + " wins.";
                                }
                            }
                        }
                        sendToListener(list);
                        break;

                    //For the 'CHAT' it sends to the listener the message received.
                    case "CHAT":
                        s = "User " + lines[1] + " sent the global message: ";
                        length = lines[0].length() + lines[1].length() + 2;
                        for (int i = length; i < line.length(); i++) {
                            s += line.charAt(i);
                        }
                        sendToListener(s);
                        break;

                    //For the 'WHISPER' it sends to the listener the private message received.
                    case "WHISPER":
                        s = "User " + lines[1] + " sent you the private message: ";
                        length = lines[0].length() + lines[1].length() + 2;
                        for (int i = length; i < line.length(); i++) {
                            s += line.charAt(i);
                        }
                        sendToListener(s);
                        break;

                    //For the 'CANNOTWHISPER' it sends to the listener
                    // that the message could not be sent
                    case "CANNOTWHISPER":
                        sendToListener("Could not send message to "
                                + lines[1] + ", the user does not exist!");
                        break;

                    case "QUIT":
                        close();
                        break;
                }
            }
            sendToListener("You were disconnected from the server!");
            close();
            System.exit(0);
        } catch (IOException e) {
            sendToListener("You were disconnected from the server!");
            close();
            System.exit(0);
        } catch (InvalidMessage e) {
        }
    }
}
