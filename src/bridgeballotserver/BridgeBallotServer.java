/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgeballotserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BridgeBallotServer extends Thread {

    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;

    public BridgeBallotServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        running = true;
        Database.getDataSource();
        while (running) {
            try {
                System.out.println("Listening for a connection");

                // Call accept() to receive the next connection
                Socket socket = serverSocket.accept();

                // Pass the socket to the RequestHandler thread for processing
                RequestHandler requestHandler = new RequestHandler(socket);
                requestHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        /*
         if (args.length == 0) {
         System.out.println("Usage: SimpleSocketServer <port>");
         System.exit(0);
         }
         */
        int port = 21;
        System.out.println("Start server on port: " + port);

        BridgeBallotServer server = new BridgeBallotServer(port);
        server.startServer();

        // Automatically shutdown in 1 minute
        /*
         try {
         Thread.sleep(60000);
         } catch (Exception e) {
         e.printStackTrace();
         }
    
         server.stopServer();
         */
    }
}

class RequestHandler extends Thread {

    private Socket socket;

    RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Received a connection");

            // Get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            // Write out our header to the client
            out.println("Echo Server 1.0");
            out.flush();

            String line = in.readLine();

            switch (line) {
                case "LOGIN":
                    String username = "";
                    String password = "";
                    int counter = 0;
                    while ((line = in.readLine()) != null) {
                        if (counter == 0) {
                            username = line;
                        } else if (counter == 1) {
                            password = line;
                        }
                        counter++;
                    }

                    boolean correctLogin = new Database().validateLogin(username, password);

                    if (correctLogin) {
                        System.out.println("Access granted");
                    } else {
                        System.out.println("Access refused");
                    }
                break;

            }

            // Close our connection
            in.close();
            out.close();
            socket.close();

            System.out.println("Connection closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
