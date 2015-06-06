package bridgeballotserver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Johnnie Ho on 6-6-2015.
 */
public class Bridge implements Serializable {

    private static final long serialVersionUID = 5950169519310163575L;

    private int id;
    private String name;
    private String location;
    private double latitude, longitude;


    public Bridge(int id, String name, String location, double latitude, double longitude){
        this.id = id;
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
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
}
