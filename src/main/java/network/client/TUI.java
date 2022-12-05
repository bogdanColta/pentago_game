package network.client;

import exceptions.ConnectionFailed;
import exceptions.InvalidMessage;
import exceptions.InvalidUsername;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TUI implements Listener {
    private static boolean login;

    public static void main(String[] args) {
        String help = "Commands" +
                "1.  Join the queue : queue \n" +
                "2.  Toggle on/off the auto-queue functionality:" +
                " autoqueue \n" +
                "3.  Toggle off the automatic ai play: play ai \n" +
                "4.  Toggle on the automatic ai play, easy difficulty:" +
                " play ai easy\n" +
                "5.  Toggle on the automatic ai play, hard difficulty:" +
                " play ai hard\n" +
                "6.  Suggest move by the ai, easy difficulty:" +
                " hint easy\n" +
                "7.  Suggest move by the ai, hard difficulty:" +
                " hint hard\n" +
                "8.  Make move on the board (cannot use while not in a game):" +
                " move integer integer\n" +
                "9.  List all the usernames of the players in the game:" +
                " list\n" +
                "10. List the rank of the players: rank\n" +
                "11. Send a global message to all the players with the chat extension:" +
                " send everyone: String\n" +
                "12. Send a private message to a player who has the chat extension:" +
                " send to username: String\n" +
                "13. Quit the TUI: quit\n" +
                "14. Print all the commands: help\n";


        System.out.println("Hi!\n" +
                "1.  Join the queue : queue \n" +
                "2.  Toggle on/off the auto-queue functionality:" +
                " autoqueue \n" +
                "3.  Toggle off the automatic ai play: play ai \n" +
                "4.  Toggle on the automatic ai play, easy difficulty:" +
                " play ai easy\n" +
                "5.  Toggle on the automatic ai play, hard difficulty:" +
                " play ai hard\n" +
                "6.  Suggest move by the ai, easy difficulty:" +
                " hint easy\n" +
                "7.  Suggest move by the ai, hard difficulty:" +
                " hint hard\n" +
                "8.  Make move on the board (cannot use while not in a game):" +
                " move integer integer\n" +
                "9.  List all the usernames of the players in the game:" +
                " list\n" +
                "10. List the rank of the players: rank\n" +
                "11. Send a global message to all the players with the chat extension:" +
                " send everyone: String\n" +
                "12. Send a private message to a player who has the chat extension:" +
                " send to username: String\n" +
                "13. Quit the TUI: quit\n" +
                "14. Print all the commands: help\n");
        login = false;

        TUI play = new TUI();

        BufferedReader reader1 = new BufferedReader(new InputStreamReader(System.in));

        int port;

        try {
            System.out.println("Introduce an address: ");
            String address = reader1.readLine();

            //checks whether entered a valid number
            System.out.println("Introduce a valid port: ");
            try {
                port = Integer.parseInt(reader1.readLine());
            } catch (NumberFormatException | IOException e) {
                port = -1;
            }

            //ask again if the port is invalid
            while (port <= 0 || port > 65536) {
                System.out.println("The port number is not valid. Introduce a valid port number: ");
                try {
                    port = Integer.parseInt(reader1.readLine());
                } catch (NumberFormatException | IOException e) {
                    port = -1;
                }
            }

            String line;
            GameClient client = new Client();
            //if the connection fails the user gets a message
            //and the TUI closes
            try {
                client.connect(address, port);
            } catch (ConnectionFailed e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }

            client.addListener(play);

            System.out.println("Enter a username: ");
            String username = reader1.readLine();
            while (!login) {
                try {
                    client.sendUsername(username);
                } catch (InvalidUsername e) {
                    System.out.println(e.getMessage());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //if the username is used it asks again
                if (!login) {
                    System.out.println("Enter a new username: ");
                    username = reader1.readLine();
                }
            }

            System.out.println("Do you want to toggle on the auto playing ai? y/n \n" +
                    "You can change the option later");
            line = reader1.readLine();
            while (!line.equals("y") && !line.equals("n")) {
                System.out.println("You have to write y (for yes) or n (for no)");
                line = reader1.readLine();
            }
            if (line.equals("y")) {
                ((Client) client).setHasAi(true);
                System.out.println("Which difficulty of the ai do you want? hard/easy");
                line = reader1.readLine();
                while (!line.equals("hard") && !line.equals("easy")) {
                    System.out.println("You have to write hard or easy");
                    line = reader1.readLine();
                }
                if (line.equals("hard")) {
                    ((Client) client).setStrategy(true);
                } else {
                    ((Client) client).setStrategy(false);
                }
            } else {
                ((Client) client).setHasAi(false);
            }

            System.out.println("Do you want to toggle on the auto-queue option? y/n");
            System.out.println("Auto mode consists of the ai playing and the auto queue");
            System.out.println("You can stop the auto-queue while in the auto mode by " +
                    "pressing Enter");
            System.out.println("If you choose no you will join the queue " +
                    "automatically only the first time");
            line = reader1.readLine();
            while (!line.equals("y") && !line.equals("n")) {
                System.out.println("You have to write y (for yes) or n (for no)");
                line = reader1.readLine();
            }

            if (line.equals("y")) {
                ((Client) client).setAutoQueue(true);
            } else {
                ((Client) client).setAutoQueue(false);
                try {
                    client.sendMessage("queue");
                } catch (InvalidMessage e) {
                }
            }

            //if the user enters an empty string, the automatic mode closes
            //else the command are sent to the client
            while (true) {
                line = reader1.readLine();
                if (line.equals("") && ((Client) client).isHasAi() &&
                        ((Client) client).getAutoQueue()) {
                    if (((Client) client).getGame() != null) {
                        System.out.println("Exiting auto mode");
                        ((Client) client).setAutoQueue(false);
                    } else {
                        System.out.println("Exiting auto mode");
                        ((Client) client).setAutoQueue(false);
                        try {
                            client.sendMessage("queue");
                        } catch (InvalidMessage e) {
                        }
                    }
                } else if (line.equals("help")) {
                    System.out.println(help);
                } else {
                    try {
                        client.sendMessage(line);
                    } catch (InvalidMessage e) {
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
            System.exit(0);
        }

    }

    /**
     * Prints on the display the messages incoming from the server.
     *
     * @param message the message received
     */
    @Override
    public void messageReceived(String message) {
        if (message.equals("Logged in")) {
            login = true;
        }

        System.out.println(message);
    }
}
