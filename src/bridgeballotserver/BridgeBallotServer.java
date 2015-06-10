package bridgeballotserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BridgeBallotServer implements Runnable{

    public final static class MessageType {
        public static final int LOGIN = 0;
        public static final int DISCONNECT = 1;
        public static final int SEND_TOKEN = 2;

        public static final int CREATE_ACCOUNT = 5;
        public static final int REQUEST_USERS = 6;
        public static final int DELETE_USER = 7;

        public static final int BRIDGE_WATCHLIST_ADD = 10;
        public static final int BRIDGE_ON_WATCHLIST = 11;
        public static final int BRIDGE_REQUEST = 12;
        public static final int BRIDGE_ADD = 13;
        public static final int BRIDGE_DELETE = 14;
        public static final int BRIDGE_UPDATE = 15;
    }

    public final static class ReturnType {
        public static final int SUCCESS = 0;
        public static final int FAILURE = 1;
        public static final int FAILURE_NAME_NOT_UNIQUE = 2;
    }

    public static HashMap<Integer, Bridge> bridgeMap = new HashMap<>();

    private Socket serverSocket;

    public BridgeBallotServer(Socket serverSocket){
        this.serverSocket = serverSocket;
    }

    public static HashMap<Integer, Bridge> getBridgeMap(){
        return bridgeMap;
    }

    /*public void sendNotification() throws Exception {
        new Thread();

       String gcmToken = "c4rV3k7JJ9k:APA91bE1g7rr5abmT9LHlhxS66Ss7n6MPN9VEdEYjaSP_Gah2S2oN8ehRXxcPTCQg7UB-7oLU9KUZbbTwaJ-d-DroHDN2GHGo4W9g1PU_VlxUlNIJyJ7GX7W4X09X2RsBeFVVvDIIh6e";
       String NotificationMessage = "Test Notification";

       Message message = new Message.Builder()
               .collapseKey("message")
               .timeToLive(3)
               .delayWhileIdle(true)
               .addData("message", NotificationMessage)
               .build();


                       Sender sender = new Sender("AIzaSyArlED8QXpO6XjvRUv_0JNis8fkWHHvd_k");

       Result result  = sender.send(message, gcmToken, 1);

       System.out.println("Message Result: "+result.toString());
   }*/

    public static void main(String[] args) throws Exception{
        System.out.println(HelperTools.getCurrentTimeStamp() + "Bridge Ballot Server Build 6");
        Database.getDataSource();

        new Database().loadBridges();
        //bridgeMap = getBridgeMap();
        /*Iterator it = bridgeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Bridge bridge = (Bridge) pair.getValue();
            System.out.println(HelperTools.getCurrentTimeStamp() + pair.getKey() + " = " + bridge.getName());
            //it.remove(); // avoids a ConcurrentModificationException
        }*/
        System.out.println(HelperTools.getCurrentTimeStamp() + "Loaded " + bridgeMap.size() + " bridges.");

        int port = 21;

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println(HelperTools.getCurrentTimeStamp() + "Server listening on port 21");
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new BridgeBallotServer(socket)).start();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println(HelperTools.getCurrentTimeStamp() + "Connection from: "
                    + serverSocket.getRemoteSocketAddress());

            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(serverSocket.getInputStream()));
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));
            out.flush();

            int line = in.readInt();
            switch (line) {
                case MessageType.LOGIN: {
                    parseLogin(in, out);
                    break;
                }
                case MessageType.DISCONNECT: {
                    serverSocket.close();
                    break;
                }
                case MessageType.SEND_TOKEN: {
                    //parseUpdateToken(in, out);
                    break;
                }
                case MessageType.BRIDGE_REQUEST: {
                    parseBridgeRequest(in, out);
                    break;
                }
                case MessageType.CREATE_ACCOUNT: {
                    createAccount(in, out);
                    break;
                }
                case MessageType.BRIDGE_WATCHLIST_ADD: {
                	addBridgeToWatchlist(in, out);
                	break;
                }
                case MessageType.BRIDGE_ON_WATCHLIST: {
                    requestWatchlist(in, out);
                    break;
                }
                case MessageType.REQUEST_USERS: {
                    requestUsers(in, out);
                    break;
                }
                case MessageType.DELETE_USER: {
                    deleteUser(in, out);
                    break;
                }
                case MessageType.BRIDGE_UPDATE: {
                    updateBridgeList(in, out);
                    break;
                }
            }
            in.close();
            out.close();
            serverSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseLogin(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        boolean isGooglePlus = in.readBoolean();
        String[] loginDetails = (String[]) in.readObject();

        System.out.println(loginDetails[0] + " " + loginDetails[1] + " " + loginDetails[2]);

        int[] correctLogin = new Database().validateLogin(loginDetails[0], loginDetails[1], isGooglePlus, loginDetails[2]);

        out.writeObject(correctLogin);
        out.flush();
        out.reset();
    }

    public void createAccount(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        Database d = new Database();
        String[] loginDetails = (String[]) in.readObject();

        System.out.println(loginDetails[0] + " " + loginDetails[1]);

        boolean exists = d.checkUserName(loginDetails[0]);

        if (!exists) {
            d.createAccount(loginDetails[0], loginDetails[1]);
            out.writeInt(ReturnType.SUCCESS);
        }

        else {
            out.writeInt(ReturnType.FAILURE_NAME_NOT_UNIQUE);
        }

        out.flush();
    }

    public void requestUsers(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        ArrayList<String> users = new Database().getUsers();

        out.writeObject(users);
        out.flush();
    }

    public void deleteUser(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        new Database().deleteUser(in.readUTF());
        out.writeInt(ReturnType.SUCCESS);
        out.flush();
    }

    public void parseUpdateToken(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        int id = in.readInt();
        String token = in.readUTF();

        new Database().updateRegToken(id, token);
        System.out.println(id + " " + token);
    }

    public void parseBridgeRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        HashMap<Integer, Bridge> bridgeMap = getBridgeMap();
        out.writeObject(bridgeMap);
        out.flush();
    }
    public void addBridgeToWatchlist(ObjectInputStream in, ObjectOutputStream out) throws Exception{
    	Database d = new Database();
    	int[] watchlistDetails = (int[]) in.readObject();
    	d.addBridgeToWatchlist(watchlistDetails[0], watchlistDetails[1]);
    	out.writeInt(ReturnType.SUCCESS);
    	out.flush();
    }
    public void requestWatchlist(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Database d = new Database();
        int username_id = in.readInt();
        HashMap<Integer, Bridge> watchMap = statusIterator(d.requestWatchlist(username_id));
        out.writeObject(watchMap);
        out.flush();
    }
    public HashMap<Integer, Bridge> statusIterator(HashMap<Integer, Bridge> input){
        Iterator it = input.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Bridge bridge = (Bridge) pair.getValue();
            bridge.setOpen(getBridgeStatusById(bridge.getId()));
        }
        return input;
    }
    public boolean getBridgeStatusById(int id){
        Bridge bridge = bridgeMap.get(id);
        return bridge.isOpen();
    }

    public void updateBridgeList(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Bridge bridge = bridgeMap.get(in.readInt());
        bridge.setOpen(true);

    }
}
