package bridgeballotserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.DataSource;

/**
 * Created by Jesse on 30-5-2015.
 */
public class Database {

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private static MysqlDataSource mysql = null;
    
    public Database(){
        try {
            connect = mysql.getConnection();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static DataSource getDataSource(){
        mysql = new MysqlDataSource();
        mysql.setURL("jdbc:mysql://localhost/appflow");
        mysql.setUser("root");
        mysql.setPassword("appflow1");
        
        return mysql;
    }
    
    public int validateLogin(String userName, String password, boolean isGooglePlus, String token){
        try {
            if(!isGooglePlus) {
                preparedStatement = connect.prepareStatement("SELECT * FROM account WHERE email = ? AND password = ?");
                preparedStatement.setString(1, userName);
                preparedStatement.setString(2, password);
            }else{
                preparedStatement = connect.prepareStatement("SELECT * FROM account WHERE email = ?");
                preparedStatement.setString(1, userName);
            }
            resultSet = preparedStatement.executeQuery();
            
            if(!isGooglePlus) {
                preparedStatement = connect.prepareStatement("UPDATE account SET token = ? WHERE email = ? AND password = ?");
                preparedStatement.setString(1, token);
                preparedStatement.setString(2, userName);
                preparedStatement.setString(3, password);
            }else{
                preparedStatement = connect.prepareStatement("UPDATE account SET token = ? WHERE email = ?");
                preparedStatement.setString(1, token);
                preparedStatement.setString(2, userName);
            }
            preparedStatement.executeUpdate();

            if(resultSet.next())
                return resultSet.getInt("id");

        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return 0;
    }

    public void updateRegToken(int id, String token){
        try {
            preparedStatement = connect.prepareStatement("UPDATE account SET token = ? WHERE id = ?");
            preparedStatement.setString(1, token);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean checkUserName(String userName){
        try {
            preparedStatement = connect.prepareStatement("SELECT email FROM account WHERE email = ?");
            preparedStatement.setString(1, userName);
            resultSet = preparedStatement.executeQuery();
            
            return resultSet.next();
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
        
    }
    
    public void createAccount(String userName, String password){
        try {
            preparedStatement = connect.prepareStatement("INSERT INTO account(email, password) VALUES (?, ?)");
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public ArrayList<String[]> requestBridgeList(){
    	try {
            preparedStatement = connect.prepareStatement("SELECT * FROM bridges");
            resultSet = preparedStatement.executeQuery();
            ArrayList<String[]> bridgeList = new ArrayList();
            while(resultSet.next()){
            	String[] bridge = new String[5];
            	bridge[0] = Integer.toString(resultSet.getInt("id"));
            	bridge[1] = resultSet.getString("name");
            	bridge[2] = resultSet.getString("location");
            	bridge[3] = resultSet.getString("latitude");
            	bridge[4] = resultSet.getString("longitude");

            	bridgeList.add(bridge);
            	            
            }
            return bridgeList;

        } catch (SQLException e){
            e.printStackTrace();
        }   
        return null;
    }

    public void loadBridges(){
        try {
            preparedStatement = connect.prepareStatement("SELECT * FROM bridges");
            resultSet = preparedStatement.executeQuery();
            ArrayList<String[]> bridgeList = new ArrayList();
            while(resultSet.next()){
                Bridge bridge = new Bridge(resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("location"),
                        Double.parseDouble(resultSet.getString("latitude")),
                        Double.parseDouble(resultSet.getString("longitude")));
                BridgeBallotServer.bridgeMap.put(resultSet.getInt("id"), bridge);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public void addBridgeToWatchlist(int bridge_id, int username_id){
    	 try {
             preparedStatement = connect.prepareStatement("INSERT INTO `bridge_watchlist` (account_id, bridge_id) VALUES ('?', '?')");
             preparedStatement.setString(1, username_id);
             preparedStatement.setString(2, bridge_id);
             preparedStatement.executeUpdate();
             }
         } catch (SQLException e){
             e.printStackTrace();
         }
    	
    }
}
