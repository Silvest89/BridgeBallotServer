package eu.silvenia.bridgeballot.network;


import java.io.Serializable;

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

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
}
