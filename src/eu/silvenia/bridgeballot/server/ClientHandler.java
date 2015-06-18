package eu.silvenia.bridgeballot.server;

/**
 * Created by Johnnie Ho on 10-6-2015.
 */

import eu.silvenia.bridgeballot.network.Bridge;
import eu.silvenia.bridgeballot.network.ProtocolMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles both client-side and server-side handler depending on which
 * constructor was called.
 */
public class ClientHandler extends ChannelHandlerAdapter {

    ChannelHandlerContext ctx;
    Client clientConnection;

    public final static class MessageType {
        public static final int LOGIN = 0;
        public static final int DISCONNECT = 1;
        public static final int SEND_TOKEN = 2;

        public static final int CREATE_ACCOUNT = 5;
        public static final int REQUEST_USERS = 6;
        public static final int DELETE_USER = 7;        
        
        public static final int REQUEST_BRIDGE = 10;
        public static final int REQUEST_WATCHLIST = 11;

        public static final int WATCHLIST_ADD = 12;
        public static final int WATCHLIST_DELETE= 13;

        public static final int BRIDGE_STATUS_UPDATE = 14;
        public static final int BRIDGE_CREATE = 15;
        public static final int BRIDGE_UPDATE = 16;
        public static final int BRIDGE_DELETE = 17;
        
        public static final int REPUTATION = 18;
        public static final int REPUTATION_CHANGE = 19;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        this.ctx = ctx;
        System.out.println(HelperTools.getCurrentTimeStamp() + "Connection: " + ctx.channel().remoteAddress() + " Channel ID: " + ctx.channel().id());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Echo back the received object to the client.
        ProtocolMessage message = (ProtocolMessage)msg;
        try {
            switch ((int)message.getMessage().get(0)) {
                case MessageType.LOGIN: {
                    parseLogin(message);
                    break;
                }
                case MessageType.CREATE_ACCOUNT:{
                    parseCreateAccount(ctx, message);
                    break;
                }
                case MessageType.REQUEST_USERS:{
                    parseRequestUsers(message);
                    break;
                }
                case MessageType.DELETE_USER:{
                    parseDeleteUser(message);
                    break;
                }
                case MessageType.DISCONNECT: {
                    ctx.close();
                    break;
                }
                case MessageType.REQUEST_BRIDGE: {
                    parseBridgeRequest();
                    break;
                }
                case MessageType.REQUEST_WATCHLIST: {
                    parseWatchListRequest();
                    break;
                }
                case MessageType.WATCHLIST_ADD: {
                    parseAddToWatchList(message);
                    break;
                }
                case MessageType.WATCHLIST_DELETE: {
                    parseRemoveFromWatchList(message);
                    break;
                }
                case MessageType.BRIDGE_STATUS_UPDATE:{
                    parseBridgeUpdateStatus(message);
                    break;
                }
                case MessageType.SEND_TOKEN:{
                    parseGcmToken(message);
                    break;   
                }
                case MessageType.BRIDGE_CREATE: {
                    parseCreateBridge(message);
                    break;
                }
                case MessageType.BRIDGE_UPDATE: {
                    parseUpdateBridge(message);
                    break;
                }
                case MessageType.BRIDGE_DELETE: {
                    parseDeleteBridge(message);
                    break;
                }
                case MessageType.REPUTATION: {
                    parseReputation(message);
                    break;
                }
                case MessageType.REPUTATION_CHANGE: {
                    parseReputationUpdate(message);
                    break;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseUpdateBridge(ProtocolMessage message) {
        ArrayList<String> updateBridge = (ArrayList) message.getMessage().get(1);
        new Database().updateBridge(updateBridge);
    }

    private void parseDeleteBridge(ProtocolMessage message) {
        ArrayList<String> deleteBridge = (ArrayList) message.getMessage().get(1);
        new Database().deleteBridge(deleteBridge);
    }


    private void parseCreateBridge(ProtocolMessage message) {
        ArrayList<String> newBridge = (ArrayList) message.getMessage().get(1);
        new Database().createBridge(newBridge);
    }
   

    @Override
    public void channelInactive (ChannelHandlerContext ctx){
        if(clientConnection != null)
            Client.clientList.remove(clientConnection.getId());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void parseLogin(ProtocolMessage message) throws Exception{
        String[] loginDetails = (String[])message.getMessage().get(1);

        boolean isGooglePlus = (boolean)message.getMessage().get(2);

        int[] correctLogin = new Database().validateLogin(loginDetails[0], loginDetails[1], isGooglePlus);
        

        if(correctLogin != null) {
            Client client = new Database().getClient(loginDetails[0], ctx.channel());
            if (client != null) {
                Client.clientList.put(client.getId(), client);
                this.clientConnection = client;
                //client.watchList = new Database().requestWatchlist(client.getId());
            }

            System.out.println(HelperTools.getCurrentTimeStamp() + "User: " + client.getUserName() + " logged in successfully.");

        }
        ProtocolMessage returnMessage = new ProtocolMessage(MessageType.LOGIN);
        returnMessage.add(correctLogin);
        clientConnection.getChannel().writeAndFlush(returnMessage);
    }

    public void parseCreateAccount(ChannelHandlerContext ctx, ProtocolMessage message){
        String[] accountDetails = (String[]) message.getMessage().get(1);

        Integer result = new Database().createAccount(accountDetails[0], accountDetails[1]);
        ProtocolMessage returnMessage = new ProtocolMessage(MessageType.CREATE_ACCOUNT);
        returnMessage.add(result);
        ctx.writeAndFlush(returnMessage);
    }

    public void parseRequestUsers(ProtocolMessage message){
        ArrayList<String> result = new Database().getUsers();
        ProtocolMessage returnMessage = new ProtocolMessage(MessageType.REQUEST_USERS);
        returnMessage.add(result);
        clientConnection.getChannel().writeAndFlush(returnMessage);
    }

    public void parseDeleteUser(ProtocolMessage message){
        String userToDelete = (String) message.getMessage().get(1);
        new Database().deleteUser(userToDelete);
        ProtocolMessage returnMessage = new ProtocolMessage(MessageType.DELETE_USER);
        returnMessage.add(0);
        clientConnection.getChannel().writeAndFlush(returnMessage);
    }

    public void parseBridgeRequest(){
        clientConnection.sendBridgeList();
    }

    private void parseBridgeUpdateStatus(ProtocolMessage message) {
        int bridgeId = (int) message.getMessage().get(1);
        boolean bridgeStatus = (boolean) message.getMessage().get(2);
        if (BridgeBallotServer.bridgeMap.get(bridgeId).setOpen(bridgeStatus)){
            clientConnection.setReputation(clientConnection.getReputation() + 1);
            new Database().saveClient(clientConnection);
            BridgeBallotServer.sendBridgeUpdate(bridgeId, bridgeStatus);
        }
    }
    
    private void parseReputationUpdate(ProtocolMessage message) { 
        int voteId = (int) message.getMessage().get(1);
        int userId = (int) message.getMessage().get(2);
        int targetId = (int) message.getMessage().get(3);
        int bridgeId = (int) message.getMessage().get(4);
        
        if(new Database().saveDislike(voteId, userId)){
            if(Client.clientList.containsKey(targetId)){
                Client.clientList.get(targetId).setReputation(Client.clientList.get(targetId).getReputation() - 2) ;
                new Database().saveClient(Client.clientList.get(targetId));
            }else{
                new Database().setReputation(targetId);   
            } 
            clientConnection.sendReputationList(bridgeId);
        }
    }

    public void parseWatchListRequest(){
        clientConnection.sendWatchList();
    }
    public void parseAddToWatchList(ProtocolMessage message){
        int bridgeId = (int)message.getMessage().get(1);
        if(!clientConnection.watchList.containsKey(bridgeId)) {
            new Database().addBridgeToWatchlist(clientConnection.getId(), bridgeId);
            clientConnection.watchList.put(bridgeId, BridgeBallotServer.bridgeMap.get(bridgeId));
        }
    }

    public void parseRemoveFromWatchList(ProtocolMessage message){
        int bridgeId = (int)message.getMessage().get(1);
        if(clientConnection.watchList.containsKey(bridgeId)) {
            new Database().removeBridgeFromWatchlist(clientConnection.getId(), bridgeId);
            clientConnection.watchList.remove(bridgeId);
        }
    }
    public void parseGcmToken(ProtocolMessage message){
        String token = (String)message.getMessage().get(1);
        if(token != null && !token.equals("")){
            clientConnection.setGcmToken(token);
            new Database().updateRegToken(clientConnection.getId(), token);
        }
    }
    public void parseReputation(ProtocolMessage message){
        int bridgeId = (int)message.getMessage().get(1);
            clientConnection.sendReputationList(bridgeId);
    }
}
