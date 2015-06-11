package eu.silvenia.bridgeballot.server;

import io.netty.channel.Channel;

import java.util.HashMap;

/**
 * Created by Johnnie Ho on 10-6-2015.
 */
public class Client {

    public static HashMap<Integer, Client> clientList = new HashMap<>();
    private int id;
    private String userName;
    private String gcmToken;
    private int accessLevel;
    private Channel channel;

    public Client(int id, String userName, String gcmToken, int accessLevel, Channel channel){
        this.id = id;
        this.userName = userName;
        this.gcmToken = gcmToken;
        this.accessLevel = accessLevel;
        this.channel = channel;
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
}
