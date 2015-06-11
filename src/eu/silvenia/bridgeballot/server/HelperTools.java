package eu.silvenia.bridgeballot.server;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Johnnie Ho on 6-6-2015.
 */
public final class HelperTools {
    public static String getCurrentTimeStamp(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss ");
        String formattedDate = sdf.format(date);

        return formattedDate;
    }
}
