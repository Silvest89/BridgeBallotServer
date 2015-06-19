/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.silvenia.bridgeballot.server;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import java.util.ArrayList;


/**
 * @author KevinPC
 * 
 * Sends out the notification to the user.
 */
public class GCMRequest {
    public void sendPost(ArrayList<String> tokenList, String bridgeName) throws Exception {
        String NotificationMessage = "Bridge: " + bridgeName + " opened!"; 
        Message message = new Message.Builder()
               .collapseKey("bridgeName")
               .timeToLive(3600)
               .delayWhileIdle(false)
               .addData("message", NotificationMessage)
               .build();
       
        Sender sender = new Sender("AIzaSyA8MiGGJO1v1868Ku1odRXUZRui4Ru8GnE");
        ArrayList<String> tokens = tokenList;
        MulticastResult mcr = sender.send(message, tokens, 0); 
    }
}
