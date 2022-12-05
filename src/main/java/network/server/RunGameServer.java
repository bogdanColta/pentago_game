package network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RunGameServer {
    public static void main(String[] args) {

        try {
            System.out.println("The local host address is: "
                    + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            System.out.println("Could not get local host address");
            System.exit(0);
        }
        System.out.println("Introduce a valid port number: ");
        int port;
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(System.in));
        try {
            port = Integer.parseInt(reader1.readLine());
        } catch (NumberFormatException | IOException e) {
            port = -1;
        }
        //checks whether the port is a valid number
        while (port <= 0 || port > 65536) {
            System.out.println("The port number is not valid. Introduce a valid port number: ");
            try {
                port = Integer.parseInt(reader1.readLine());
            } catch (NumberFormatException | IOException e) {
                port = -1;
            }
        }

        GameServer server = new GameServer(port);
        System.out.println("Server started at port " + server.getPort());
        server.start();

        try {
            while (true) {
                if (reader1.readLine().equals("quit")) {
                    server.stop();
                    System.exit(-1);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Could not connect!");
            System.exit(0);
        }

    }
}
