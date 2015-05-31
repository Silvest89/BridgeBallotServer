/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgeballotserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BridgeBallotServer {

    public static void main(String[] args) {
        int port = 21;

        Protocol server = new Protocol(port);
        server.startServer();

        // Automatically shutdown in 1 minute
        /*
         try {
         Thread.sleep(60000);
         } catch (Exception e) {
         e.printStackTrace();
         }
    
         server.stopServer();
         */
    }
}
