package eu.silvenia.bridgeballot.server;

/**
 * Created by Johnnie Ho on 10-6-2015.
 */

import bridgeballotserver.*;
import eu.silvenia.bridgeballot.network.Bridge;
import eu.silvenia.bridgeballot.network.ProtocolMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles both client-side and server-side handler depending on which
 * constructor was called.
 */
public class ClientHandler extends ChannelHandlerAdapter {

    ChannelHandlerContext ctx;
    Client client;

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
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
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

        int[] correctLogin = new Database().validateLogin(loginDetails[0], loginDetails[1], isGooglePlus, loginDetails[2]);
        Client client = new Database().getClient(loginDetails[0], ctx.channel());
        if(client != null) {
            Client.clientList.put(client.getId(), client);
            this.client = client;
            client.watchList = new Database().requestWatchlist(client.getId());
        }

        System.out.println(HelperTools.getCurrentTimeStamp() + "User: " + client.getUserName() + " logged in successfully.");

        ProtocolMessage returnMessage = new ProtocolMessage(MessageType.LOGIN);
        returnMessage.add(correctLogin);
        client.getChannel().writeAndFlush(returnMessage);
    }

    public void parseBridgeRequest(){
        ProtocolMessage message = new ProtocolMessage(MessageType.REQUEST_BRIDGE);

        ArrayList<String[]> bridgeList = new ArrayList<>();
        Iterator it = BridgeBallotServer.bridgeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Bridge bridge = (Bridge)pair.getValue();
            String[] bridge2 = new String[6];
            bridge2[0] = Integer.toString(bridge.getId());
            bridge2[1] = bridge.getName();
            bridge2[2] = bridge.getLocation();
            bridge2[3] = Double.toString(bridge.getLatitude());
            bridge2[4] = Double.toString(bridge.getLongitude());
            bridge2[5] = Boolean.toString(bridge.isOpen());

            bridgeList.add(bridge2);
        }
        message.add(bridgeList);

        client.getChannel().writeAndFlush(message);
    }

    public void parseWatchListRequest(){
        ProtocolMessage message = new ProtocolMessage(MessageType.REQUEST_WATCHLIST);

        ArrayList<String[]> bridgeList = new ArrayList<>();
        for (Object o : client.watchList.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Bridge bridge = (Bridge) pair.getValue();
            String[] bridge2 = new String[6];
            bridge2[0] = Integer.toString(bridge.getId());
            bridge2[1] = bridge.getName();
            bridge2[2] = bridge.getLocation();
            bridge2[3] = Double.toString(bridge.getLatitude());
            bridge2[4] = Double.toString(bridge.getLongitude());
            bridge2[5] = Boolean.toString(bridge.isOpen());
            bridgeList.add(bridge2);
        }
        message.add(bridgeList);

        client.getChannel().writeAndFlush(message);
    }
    public void parseAddToWatchList(ProtocolMessage message){
        int bridgeId = (int)message.getMessage().get(1);
        System.out.println(bridgeId);
        if(!client.watchList.containsKey(bridgeId)) {
            new Database().addBridgeToWatchlist(client.getId(), bridgeId);
            client.watchList.put(bridgeId, BridgeBallotServer.bridgeMap.get(bridgeId));
        }
    }

    public void parseRemoveFromWatchList(ProtocolMessage message){
        int bridgeId = (int)message.getMessage().get(1);
        System.out.println(bridgeId);
        if(client.watchList.containsKey(bridgeId)) {
            new Database().removeBridgeFromWatchlist(client.getId(), bridgeId);
            client.watchList.remove(bridgeId);
        }
    }
}
