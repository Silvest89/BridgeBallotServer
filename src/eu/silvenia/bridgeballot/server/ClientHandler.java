package eu.silvenia.bridgeballot.server;

/**
 * Created by Johnnie Ho on 10-6-2015.
 */

import eu.silvenia.bridgeballot.network.Bridge;
import eu.silvenia.bridgeballot.network.ProtocolMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
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

        public static final int BRIDGE_WATCHLIST_ADD = 10;
        public static final int BRIDGE_ON_WATCHLIST = 11;
        public static final int BRIDGE_REQUEST = 12;
        public static final int BRIDGE_ADD = 13;
        public static final int BRIDGE_DELETE = 14;
        public static final int BRIDGE_UPDATE = 15;
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
                case MessageType.BRIDGE_REQUEST: {
                    parseBridgeRequest();
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
        }

        System.out.println(HelperTools.getCurrentTimeStamp() + "User: " + client.getUserName() + " logged in successfully.");

        ProtocolMessage returnMessage = new ProtocolMessage(MessageType.LOGIN);
        returnMessage.add(correctLogin);
        client.getChannel().writeAndFlush(returnMessage);
    }

    public void parseBridgeRequest(){
        ProtocolMessage message = new ProtocolMessage(MessageType.BRIDGE_REQUEST);
        ArrayList<String[]> bridgeList = new Database().requestBridgeList();
        message.add(bridgeList);

        client.getChannel().writeAndFlush(message);
    }
}
