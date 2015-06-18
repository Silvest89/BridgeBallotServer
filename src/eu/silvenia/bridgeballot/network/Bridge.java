package eu.silvenia.bridgeballot.network;


import eu.silvenia.bridgeballot.server.BridgeBallotServer;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Johnnie Ho on 6-6-2015.
 */
public class Bridge implements Serializable {

    private static final long serialVersionUID = 5950169519310163575L;

    private int id;
    private String name;
    private String location;
    private double latitude, longitude;
    private int distance;
    private boolean isOpen;

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Bridge(int id, String name, String location, double latitude, double longitude, boolean isOpen){
        this.id = id;
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = 0;
        this.isOpen = isOpen;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getDistance(){
        return distance;
    }

    public boolean isOpen(){ return isOpen; }

    public synchronized boolean setOpen(boolean isOpen) {
        if (isOpen && this.isOpen()){
            return false;
        }
        this.isOpen = isOpen;
        if (isOpen){
            Timer timer = new Timer();
            timer.schedule(new BridgeTimer(), 15 * 60 * 1000);
        }
        return true;
    }

    public class BridgeTimer extends TimerTask {
        @Override
        public void run() {
            setOpen(false);
            BridgeBallotServer.sendBridgeUpdate(id, false);
        }
    }
}
