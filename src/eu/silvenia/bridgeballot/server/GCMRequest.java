/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.silvenia.bridgeballot.server;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 *
 * @author KevinPC
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
        
        String gcmToken = "fTDKl9HLvBc:APA91bH98_C2xrQ5SdCbaSi3IADfBKZq5TWyMjWpvGAT-H8AroyUfYTei6tWClwWM3b8S42Bk8ymq-wTYA7KVJtNs4d4W0BfHXdXqYQQ5goJHMunTFhkOKpyGgMu9jupKtHgxG-00s3s";
        Sender sender = new Sender("AIzaSyA8MiGGJO1v1868Ku1odRXUZRui4Ru8GnE");
        ArrayList<String> tokens = tokenList;
        MulticastResult mcr = sender.send(message, tokens, 0);
        System.out.println("Message Result: "+mcr.toString());
                
        
        //tokens.add(gcmToken);
        
        
        
        
        //Result result  = sender.send(message, gcmToken, 1);
        
                
        
        
                
 
		/*String url = "https://gcm-http.googleapis.com/gcm/send";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "key=AIzaSyA8MiGGJO1v1868Ku1odRXUZRui4Ru8GnE");
                con.setRequestProperty("Content-Type", "application/json");
                
                con.setDoOutput(true);
                
                JSONObject json = new JSONObject();
                json.put("to", "fTDKl9HLvBc:APA91bH98_C2xrQ5SdCbaSi3IADfBKZq5TWyMjWpvGAT-H8AroyUfYTei6tWClwWM3b8S42Bk8ymq-wTYA7KVJtNs4d4W0BfHXdXqYQQ5goJHMunTFhkOKpyGgMu9jupKtHgxG-00s3s");
		//json.put("data", "");
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(json);
                System.out.println(jsonArray);
                // Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeUTF(jsonArray.toString());
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
	
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());*/
 
	}
    
}
