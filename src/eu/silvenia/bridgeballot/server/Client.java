package eu.silvenia.bridgeballot.server;

import eu.silvenia.bridgeballot.network.Bridge;
import eu.silvenia.bridgeballot.network.ProtocolMessage;
import io.netty.channel.Channel;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Sends out all the user information to the client. 
 */
public class Client {

    public static HashMap<Integer, Client> clientList = new HashMap<>();
    private int id;
    private String userName;
    private String gcmToken;
    private int accessLevel;
    private Channel channel;
    private int reputation;

    public HashMap<Integer, Bridge> watchList = new HashMap<>();

    public Client(int id, String userName, String gcmToken, int accessLevel, Channel channel, int reputation){
        this.id = id;
        this.userName = userName;
        this.gcmToken = gcmToken;
        this.accessLevel = accessLevel;
        this.channel = channel;
        this.reputation = reputation;
    }
    


    public static HashMap<Integer, Client> getClientList() {
        return clientList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    
    /**
     * Sends the WatchList to the client.
     */
    public void sendWatchList(){
        ProtocolMessage message = new ProtocolMessage(ClientHandler.MessageType.REQUEST_WATCHLIST);

        ArrayList<String[]> bridgeList = new ArrayList<>();
        Iterator it = watchList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
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
        getChannel().writeAndFlush(message);
    }
    
    /**
     * Sends the BridgeList to the client.
     */
    public void sendBridgeList(){
        ProtocolMessage message = new ProtocolMessage(ClientHandler.MessageType.REQUEST_BRIDGE);

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

        getChannel().writeAndFlush(message);
    }
    
    /**
     * Sends the updated BridgeStatus to the client.
     * 
     * @param bridgeId - The id of the bridge
     * @param status - The status of the bridge.
     */
    public void updateBridgeStatus(int bridgeId, boolean status){
        ProtocolMessage message = new ProtocolMessage(ClientHandler.MessageType.BRIDGE_STATUS_UPDATE);
        
        message.add(bridgeId);
        message.add(status);
        getChannel().writeAndFlush(message);        
    }
    
    /**
     * Sends out the reputationList per bridge to the client.
     * 
     * @param bridgeId - The id of the bridge.
     */
    public void sendReputationList(int bridgeId){
        ArrayList<String[]> list = new Database().getReputation(bridgeId);
        if(list != null && !list.isEmpty()){
        ProtocolMessage message = new ProtocolMessage((ClientHandler.MessageType.REPUTATION));
        
        message.add(list);
        getChannel().writeAndFlush(message);
        }
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }
}
