package eu.silvenia.bridgeballot.network;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Johnnie Ho on 11-6-2015.
 */
public class ProtocolMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Object> protocolMessage = new ArrayList<>();

    public ProtocolMessage(int messageType){
        protocolMessage.add(messageType);
    }

    public void add(Object object){
        protocolMessage.add(object);
    }

    public ArrayList getMessage(){
        return protocolMessage;
    }
}
