package network.server;

import game.Game;
import game.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameClientHandler implements Runnable {
    private final Object object;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Socket socket;
    private final GameServer server;
    private String username;
    private Game game;
    private Player player;
    private boolean accepted;
    private boolean logged;
    private boolean inGame;
    private Set<String> extensions;
    private boolean hasRank;
    private boolean hasChat;

    /**
     * Initializes the fields of the class.
     *
     * @param socket the socket we want to use for the client
     * @param server the server that the client connects to
     * @param object the object used for synchronization between each thread
     * @throws IOException if it cannot connect to the input stream of the socket
     */
    public GameClientHandler(Socket socket, GameServer server, Object object) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.object = object;
        extensions = new HashSet<>();
    }

    /**
     * Sends message to the client.
     *
     * @param message the message sent to the client
     */
    public void sendGame(String message) {
        synchronized (object) {
            out.println(message);
        }
    }

    /**
     * Returns username field.
     *
     * @return the value of the username field
     */
    public String getUsername() {
        synchronized (object) {
            return username;
        }
    }

    /**
     * Sets username to the value of the parameter.
     *
     * @param username the value we want to set username to
     */
    public void setUsername(String username) {
        synchronized (object) {
            this.username = username;
        }
    }

    /**
     * Returns the field accepted.
     *
     * @return the value of accepted field
     */
    public boolean isAccepted() {
        synchronized (object) {
            return accepted;
        }
    }

    /**
     * Returns the value of logged.
     *
     * @return the value of logged field
     */
    public boolean isLogged() {
        synchronized (object) {
            return logged;
        }
    }

    /**
     * Returns the value of inGame.
     *
     * @return the value of inGame field
     */
    public boolean getInGame() {
        synchronized (object) {
            return inGame;
        }
    }

    /**
     * Sets the inGame to the value of the parameter.
     *
     * @param k the value we want to set k to
     */
    public void setInGame(boolean k) {
        synchronized (object) {
            inGame = k;
        }
    }

    /**
     * Returns the player.
     *
     * @return the player field
     */
    public Player getPlayer() {
        synchronized (object) {
            return player;
        }
    }

    /**
     * Sets the player to the value of the parameter.
     *
     * @param player the value we want to set player to
     */
    public void setPlayer(Player player) {
        synchronized (object) {
            this.player = player;
        }
    }

    /**
     * Returns the server field.
     *
     * @return the server field
     */
    public GameServer getServer() {
        synchronized (object) {
            return server;
        }
    }

    /**
     * Returns the game field.
     *
     * @return the game field
     */
    public Game getGame() {
        synchronized (object) {
            return game;
        }
    }

    /**
     * Sets the game to the value of the parameter.
     *
     * @param game the value we want to set game to
     */
    public void setGame(Game game) {
        synchronized (object) {
            this.game = game;
            inGame = true;
        }
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
        synchronized (object) {
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
    }

    /**
     * Removes the ClientHandler from the server's lists and then closes the socket.
     */
    public void close() {
        synchronized (object) {
            try {
                getServer().removeClient(this);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Receives the messages coming from the client according to the protocol.
     * If the command is 'PING' it calls server's method handlePing. If it is
     * 'MOVE' it calls the method handleMove. If it is 'QUEUE' it calls the
     * method handleQueue. If it is 'HELLO' it sends a hello message back
     * and gets the extensions from the message. It is 'LOGIN' it checks
     * whether there is already a user with the same name, if yes, it sends
     * 'ALREADYLOGGEDIN' to the client, if no. it sends 'LOGIN'. If it is
     * 'LIST' it calls the method handleList. If it is 'QUIT' it closes the
     * socket. If it is 'RANK' it calls the method handleRank. If it is
     * 'CHAT' it calls the method handleGlobalChat. If it is 'WHISPER' it
     * calls the method handlePrivateChat.
     *
     * @param line the string received from the client
     */
    public void clientSwitch(String line) {
        synchronized (object) {
            String[] lines = line.split("~");
            String command = lines[0];
            switch (command) {
                case "PING":
                    if (isAccepted()) {
                        getServer().handlePing(this);
                    }
                    break;

                case "MOVE":
                    if (lines.length == 3 && isAccepted() && isLogged() && getInGame() &&
                            inGame && isNumeric(lines[1]) && isNumeric(lines[2])) {
                        getServer().handleMove(game, player,
                                Integer.parseInt(lines[1]), Integer.parseInt(lines[2]));
                    }
                    break;

                case "QUEUE":
                    if (isAccepted() && isLogged() && !inGame) {
                        getServer().handleQueue(this);
                    }
                    break;

                case "HELLO":
                    accepted = true;
                    sendGame("HELLO~server by bogdan & alex");
                    if (lines.length > 2) {
                        for (int i = 2; i < 7; i++) {
                            if (lines.length > i && lines[i].equals("RANK")) {
                                extensions.add("RANK");
                                hasRank = true;
                            }

                            if (lines.length > i && lines[i].equals("CHAT")) {
                                extensions.add("CHAT");
                                getServer().getChatClients().add(this);
                                hasChat = true;
                            }
                        }
                    }
                    break;

                case "LOGIN":
                    if (isAccepted() && lines.length == 2) {
                        setUsername(lines[1]);
                        if (getServer().checkLogged(getUsername())) {
                            logged = true;
                            getServer().addLogged(this);
                            sendGame("LOGIN");
                        } else {
                            sendGame("ALREADYLOGGEDIN");
                        }
                    }
                    break;

                case "LIST":
                    if (isAccepted() && isLogged()) {
                        getServer().handleList(this);
                    }
                    break;

                case "PONG":
                    break;

                case "QUIT":
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "RANK":
                    if (isAccepted() && isLogged() && hasRank) {
                        getServer().handleRank(this);
                    }
                    break;

                case "CHAT":
                    if (isAccepted() && isLogged() && hasChat) {
                        getServer().handleGlobalChat(line, this);
                    }
                    break;

                case "WHISPER":
                    if (isLogged() && isLogged() && hasChat) {
                        getServer().handlePrivateChat(line, this);
                    }
                    break;
            }
        }
    }

    /**
     * The run method of the Runnable class GameClientHandler. It receives
     * messages from the client according to the protocol. It sends each
     * message to the clientSwitch method. If connection is closed from the
     * server then commands from the catch will be executed, if it is closed
     * by the client, then the instructions after the while loop will be executed.
     */
    @Override
    public void run() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                clientSwitch(line);
            }
            List<GameClientHandler> list = server.getPlayers().get(game);
            close();
            //checks which player disconnected in order to send the GAMEOVER message
            //to the client and the winner to the rank list.
            if (list != null && (!server.getClients().contains(list.get(0)) ||
                    !server.getClients().contains(list.get(1)))) {
                if (!server.getClients().contains(list.get(0))) {
                    list.get(1).sendGame("GAMEOVER~" + "DISCONNECT~" + list.get(1).getUsername());
                    list.get(1).setInGame(false);
                    if (server.getRanks().containsKey(list.get(1))) {
                        server.getRanks().put(list.get(1), server.getRanks().get(list.get(1)) + 1);
                    } else {
                        server.getRanks().put(list.get(1), 1);
                    }
                } else {
                    list.get(0).sendGame("GAMEOVER~" + "DISCONNECT~" + list.get(0).getUsername());
                    list.get(0).setInGame(false);
                    if (server.getRanks().containsKey(list.get(0))) {
                        server.getRanks().put(list.get(0), server.getRanks().get(list.get(0)) + 1);
                    } else {
                        server.getRanks().put(list.get(0), 1);
                    }
                }
            }
            return;

        } catch (IOException e) {
            List<GameClientHandler> list = server.getPlayers().get(game);
            close();
            if (list != null && (!server.getClients().contains(list.get(0)) ||
                    !server.getClients().contains(list.get(1)))) {
                if (!server.getClients().contains(list.get(0))) {
                    list.get(1).sendGame("GAMEOVER~" + "DISCONNECT~" + list.get(1).getUsername());
                    list.get(1).setInGame(false);
                    if (server.getRanks().containsKey(list.get(1))) {
                        server.getRanks().put(list.get(1), server.getRanks().get(list.get(1)) + 1);
                    } else {
                        server.getRanks().put(list.get(1), 1);
                    }
                } else {
                    list.get(0).sendGame("GAMEOVER~" + "DISCONNECT~" + list.get(0).getUsername());
                    list.get(0).setInGame(false);
                    if (server.getRanks().containsKey(list.get(0))) {
                        server.getRanks().put(list.get(0), server.getRanks().get(list.get(0)) + 1);
                    } else {
                        server.getRanks().put(list.get(0), 1);
                    }
                }
            }
            return;
        }
    }

}
