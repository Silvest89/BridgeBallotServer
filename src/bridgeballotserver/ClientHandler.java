package bridgeballotserver;

/**
 * Created by Johnnie Ho on 10-6-2015.
 */
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;

/**
 * Handles both client-side and server-side handler depending on which
 * constructor was called.
 */
public class ClientHandler extends ChannelHandlerAdapter {

    ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        this.ctx = ctx;
    }

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
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Echo back the received object to the client.
        ArrayList<Object> incomingMessage = (ArrayList)msg;

        switch((int)incomingMessage.get(0)){
            case MessageType.LOGIN:{
                incomingMessage.remove(0);

                try {
                    parseLogin(incomingMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
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

    public void parseLogin(ArrayList<Object> incomingMessage) throws Exception{
        String[] loginDetails = (String[])incomingMessage.get(0);

        boolean isGooglePlus = (boolean)incomingMessage.get(1);

        System.out.println(loginDetails[0] + " " + loginDetails[1] + " " + loginDetails[2]);

        int[] correctLogin = new Database().validateLogin(loginDetails[0], loginDetails[1], isGooglePlus, loginDetails[2]);

        System.out.println(correctLogin);
    }
}
