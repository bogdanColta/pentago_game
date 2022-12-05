package network.server;


import game.Game;
import game.Marble;
import game.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GameServer implements Server, Runnable {
    private static final Object OBJECT = new Object();
    private static int port;
    private static GameServer server;
    private Thread s1;
    private List<GameClientHandler> clients;
    private List<GameClientHandler> clientsQueue;
    private List<GameClientHandler> clientsLogged;
    private Map<Game, List<GameClientHandler>> players;
    private Map<GameClientHandler, Integer> rankings;
    private Set<GameClientHandler> chatClients;
    private ServerSocket serverSocket;

    /**
     * Initializes the port field of the class.
     *
     * @param port the value we want to set port to
     */
    public GameServer(int port) {
        server = this;
        GameServer.port = port;
    }

    /**
     * Creates a new thread of server and starts it.
     */
    @Override
    public void start() {
        s1 = new Thread(this);
        s1.start();
    }

    /**
     * Closes the server socket.
     */
    @Override
    public void stop() {
        try {
            server.serverSocket.close();
        } catch (IOException e) {
            System.out.println("error");
        }

        try {
            s1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the field port.
     *
     * @return returns the value of the port field
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Returns the map of all players in the game.
     *
     * @return returns the field players
     */
    synchronized public Map<Game, List<GameClientHandler>> getPlayers() {
        return players;
    }

    /**
     * Handles the move received.
     *
     * @param game   the game we want to apply the move to
     * @param player the player that did the move
     * @param n1     the index of the move
     * @param n2     the rotation of the move
     */
    synchronized public void handleMove(Game game, Player player, int n1, int n2) {
        List<GameClientHandler> list = players.get(game);
        List<Player> playersList = Arrays.asList(game.getPlayers());
        //checks whether the move is valid
        if (game.getBoard().checkMove(n1, n2)) {
            //checks whether the player is the current player
            if ((playersList.get(game.getCurrent()).getMarble()).equals(player.getMarble())) {
                game.registerMove(n1, n2);
                for (GameClientHandler pl : list) {
                    //sends the move to both clients
                    pl.sendGame("MOVE~" + n1 + "~" + n2);
                }
            } else {
                //sends error to the player sending the move
                for (GameClientHandler pl : list) {
                    if (pl.getPlayer() == player) {
                        pl.sendGame("ERROR~Invalid move");
                    }
                }
            }
        } else {
            //sends error to the player sending the move
            for (GameClientHandler pl : list) {
                if (pl.getPlayer() == player) {
                    pl.sendGame("ERROR~Not your turn");
                }
            }
        }
        //checks whether the game has finished
        if (game.getBoard().gameOver()) {
            //check winner of the game and send message to both clients
            if (game.getBoard().isWinner(playersList.get(0).getMarble())) {
                for (GameClientHandler pl : list) {
                    pl.setInGame(false);
                    pl.sendGame("GAMEOVER~" + "VICTORY~" + playersList.get(0).getName());
                    if (pl.getPlayer().equals(playersList.get(0))) {
                        if (rankings.containsKey(pl)) {
                            rankings.put(pl, rankings.get(pl) + 1);
                        } else {
                            rankings.put(pl, 1);
                        }
                    }
                }
                //delete the game from the map which stores the current games
                players.remove(game);
            } else if (game.getBoard().isWinner(playersList.get(1).getMarble())) {
                for (GameClientHandler pl : list) {
                    pl.setInGame(false);
                    pl.sendGame("GAMEOVER~" + "VICTORY~" + playersList.get(1).getName());
                    if (pl.getPlayer().equals(playersList.get(1))) {
                        if (rankings.containsKey(pl)) {
                            rankings.put(pl, rankings.get(pl) + 1);
                        } else {
                            rankings.put(pl, 1);
                        }
                    }
                }
                players.remove(game);
            } else {
                for (GameClientHandler pl : list) {
                    pl.setInGame(false);
                    pl.sendGame("GAMEOVER~" + "DRAW");
                }
                players.remove(game);
            }
        }
    }

    /**
     * Returns the 'PONG' to the client.
     *
     * @param clientHandler handler of the client which sent 'PING'
     */
    synchronized public void handlePing(GameClientHandler clientHandler) {
        clientHandler.sendGame("PONG");
    }

    /**
     * Adds handler to the list of logged.
     *
     * @param clientHandler the handler added to the list
     */
    synchronized public void addLogged(GameClientHandler clientHandler) {
        clientsLogged.add(clientHandler);
    }

    /**
     * Sends the string containing the list to the client.
     *
     * @param clientHandler the handler through which the list is sent
     */
    synchronized public void handleList(GameClientHandler clientHandler) {
        String s = "LIST";
        for (GameClientHandler client : clientsLogged) {
            s += "~" + client.getUsername();
        }
        clientHandler.sendGame(s);
    }

    /**
     * Sends the string containing the ranks to the client.
     *
     * @param clientHandler the handler through which the list of ranks is sent
     */
    synchronized public void handleRank(GameClientHandler clientHandler) {
        String s = "RANK";
        for (GameClientHandler client : rankings.keySet()) {
            s += "~" + client.getUsername() + "~" + rankings.get(client);
        }
        clientHandler.sendGame(s);
    }

    /**
     * Checks whether the client is logged in.
     *
     * @param username username of the client we want to check
     * @return if the client is logged
     */
    synchronized public boolean checkLogged(String username) {
        for (GameClientHandler client : clientsLogged) {
            if (client.getUsername().equals(username)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sends the global message to each client with the chat extension.
     *
     * @param message the message sent
     * @param sender  the sender of the message
     */
    synchronized public void handleGlobalChat(String message, GameClientHandler sender) {
        String s = "CHAT~" + sender.getUsername() + "~";
        int index = message.indexOf('~');
        for (int i = index + 1; i < message.length(); i++) {
            s += message.charAt(i);
        }
        for (GameClientHandler client : chatClients) {
            client.sendGame(s);
        }
    }

    /**
     * Sends the private message to the client with the specified username in the message.
     *
     * @param message the message sent
     * @param sender  the sender of the message
     */
    synchronized public void handlePrivateChat(String message, GameClientHandler sender) {

        int c = 0;
        int i;
        for (i = 0; i < message.length(); i++) {
            Character chr = message.charAt(i);
            if (chr.equals('~')) {
                c++;
            }
            if (c == 2) {
                break;
            }
        }

        String username = "";
        //finds the username of the user we want to send the message to
        for (int j = message.indexOf('~') + 1; j < i; j++) {
            username += message.charAt(j);
        }

        String s = "WHISPER~" + sender.getUsername() + "~";
        //adds the message to the string
        for (int a = i + 1; a < message.length(); a++) {
            s += message.charAt(a);
        }

        for (GameClientHandler client : chatClients) {
            if (client.getUsername().equals(username)) {
                client.sendGame(s);
                return;
            }
        }
        //if the user was not found
        sender.sendGame("CANNOTWHISPER~" + username);

    }

    /**
     * Adds the handler to the queue. If there are more than two handlers
     * in the queue, a game is created.
     *
     * @param clientHandler the user we want to add to the queue
     */
    synchronized public void handleQueue(GameClientHandler clientHandler) {
        if (getClientsQueue().contains(clientHandler)) {
            getClientsQueue().remove(clientHandler);
        } else {
            getClientsQueue().add(clientHandler);
        }

        if (server.getClientsQueue().size() >= 2) {
            List<GameClientHandler> gameList = new ArrayList<>();
            gameList.add(server.getClientsQueue().get(0));
            gameList.add(server.getClientsQueue().get(1));
            Player player1 = new Player(gameList.get(0).getUsername(), Marble.BLACK);
            Player player2 = new Player(gameList.get(1).getUsername(), Marble.WHITE);
            Game game = new Game(player1, player2);
            gameList.get(0).setGame(game);
            gameList.get(1).setGame(game);

            gameList.get(0).setPlayer(player1);
            gameList.get(1).setPlayer(player2);

            server.getPlayers().put(game, gameList);
            server.newGame(game);

            server.getClientsQueue().remove(0);
            server.getClientsQueue().remove(0);
        }
    }

    /**
     * Returns the field clientsQueue containing the users in the queue.
     *
     * @return the field clientsQueue
     */
    synchronized public List<GameClientHandler> getClientsQueue() {
        return clientsQueue;
    }

    /**
     * Sends a message to the client that a new game was created.
     *
     * @param game the new game that was created
     */
    synchronized public void newGame(Game game) {
        List<GameClientHandler> list = players.get(game);
        for (GameClientHandler pl : list) {
            pl.sendGame("NEWGAME" + "~" + list.get(0).getPlayer().getName()
                    + "~" + list.get(1).getPlayer().getName());
        }
    }

    /**
     * Adds a client to the list of connected clients.
     *
     * @param client the client we want to add to the list
     */
    synchronized public void addClient(GameClientHandler client) {
        clients.add(client);

    }

    /**
     * Removes the client from all the lists.
     *
     * @param client the client we want to remove
     */
    synchronized public void removeClient(GameClientHandler client) {
        clients.remove(client);
        clientsLogged.remove(client);
        clientsQueue.remove(client);
        players.remove(client.getGame());
    }

    /**
     * Returns the field clients which stores connected clients.
     *
     * @return the clients list
     */
    synchronized public List<GameClientHandler> getClients() {
        return clients;
    }

    /**
     * Returns the field rankings which stores the number of wins for each player.
     *
     * @return the rankings map
     */
    synchronized public Map<GameClientHandler, Integer> getRanks() {
        return rankings;
    }

    /**
     * Returns the field chatClients which stores the clients with chat extension.
     *
     * @return the chatClients set
     */
    synchronized public Set<GameClientHandler> getChatClients() {
        return chatClients;
    }

    /**
     * The run method of the Runnable class GameServer. Initializes all the list fields.
     * Creates the server socket. Then starts a loop in which it creates client handlers
     * for the clients connecting and then starts a thread for each of them.
     */
    @Override
    public void run() {
        this.clients = new ArrayList<>();
        this.clientsQueue = new ArrayList<>();
        this.players = new HashMap<>();
        this.clientsLogged = new ArrayList<>();
        this.rankings = new HashMap<>();
        this.chatClients = new HashSet<>();

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Could not start server at port " + port);
            System.out.println("The port is probably already being used");
            System.exit(0);
        }

        boolean run = true;

        while (run) {
            try {
                Socket socket = serverSocket.accept();
                GameClientHandler clientHandler = new GameClientHandler(socket, server, OBJECT);
                addClient(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();

            } catch (IOException e) {
                run = false;
            }
        }
    }
}



