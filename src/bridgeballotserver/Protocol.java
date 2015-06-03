package bridgeballotserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Johnnie Ho on 31-5-2015.
 */
public class Protocol extends Thread {

    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;

    public Protocol(int port){
        this.port = port;
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

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Starting server on port: " + port);
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        this.interrupt();
    }
}

class RequestHandler extends Thread {

    public final static class MessageType {
        public static final int LOGIN = 0;
        public static final int DISCONNECT = 1;
        public static final int SEND_TOKEN = 2;
        public static final int BRIDGE_REQUEST = 3;
        public static final int BRIDGE_ADD = 4;
        public static final int BRIDGE_DELETE = 5;
    }

    public final static class ReturnType {
        public static final int SUCCESS = 0;
        public static final int FAILURE = 1;
    }

    private Socket socket;

    RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
            try {
                System.out.println("Received a connection");
                // Get input and output streams
                //ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                int line = in.readInt();
                System.out.println("Test1:" + line);
                switch (line) {
                    case MessageType.LOGIN: {
                        parseLogin(in, out);
                        break;
                    }
                    case MessageType.DISCONNECT:{
                        socket.close();
                        break;
                    }
                    case MessageType.SEND_TOKEN:{
                        handleToken(in, out);
                        socket.close();  
                    }
                    case MessageType.BRIDGE_REQUEST: {
                    	parseBridgeRequest(in,out);
                    	socket.close();
                    	break;
                    }
                    default: {
                        in.close();
                        out.close();
                        socket.close();
                        break;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void parseLogin(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        String[] loginDetails = (String[]) in.readObject();
        System.out.println(loginDetails[0] + " " + loginDetails[1]);
        boolean correctLogin = new Database().validateLogin(loginDetails[0], loginDetails[1]);

        if (correctLogin) {
            out.writeInt(ReturnType.SUCCESS);
        } else {
            out.writeInt(ReturnType.FAILURE);
        }
        out.flush();

    }
    
    public void handleToken(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        String token = (String) in.readUTF();
        System.out.println(token);
    }
    
    public void parseBridgeRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        ArrayList<String[]> bridgeList = new Database().requestBridgeList();
        out.writeObject(bridgeList);
        out.flush();

    }
}
