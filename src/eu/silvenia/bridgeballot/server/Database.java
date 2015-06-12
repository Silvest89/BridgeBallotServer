package eu.silvenia.bridgeballot.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import eu.silvenia.bridgeballot.network.Bridge;
import io.netty.channel.Channel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

    public Client getClient(String userName, Channel channel){
        try {
            preparedStatement = connect.prepareStatement("SELECT * FROM account WHERE email = ? ");
            preparedStatement.setString(1, userName);
            resultSet = preparedStatement.executeQuery();

            Client client = null;
            if(resultSet.next()){
                client = new Client(resultSet.getInt("id"),
                        resultSet.getString("email"),
                        resultSet.getString("token"),
                        resultSet.getInt("access_level"),
                        channel);

                preparedStatement = connect.prepareStatement("SELECT * FROM bridge_watchlist WHERE account_id = ? ");
                preparedStatement.setInt(1, client.getId());
                resultSet = preparedStatement.executeQuery();
                while(resultSet.next()) {
                    client.watchList.put(resultSet.getInt("bridge_id"), BridgeBallotServer.bridgeMap.get(resultSet.getInt("bridge_id")));
                }
            }
            return client;

        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
    public int[] validateLogin(String userName, String password, boolean isGooglePlus, String token){
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

            int[] result = new int[2];

            if(resultSet.next()) {
                result[0] = resultSet.getInt("id");
                result[1] = resultSet.getInt("access_level");
                return result;
            }


        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;
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
            preparedStatement = connect.prepareStatement("INSERT INTO account(email, password, access_level) VALUES (?, ?, ?)");
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, password);
            preparedStatement.setInt(3, 1);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void loadBridges(){
        try {
            preparedStatement = connect.prepareStatement("SELECT * FROM bridges");
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Bridge bridge = new Bridge(resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("location"),
                        Double.parseDouble(resultSet.getString("latitude")),
                        Double.parseDouble(resultSet.getString("longitude")),
                        false);
                BridgeBallotServer.bridgeMap.put(resultSet.getInt("id"), bridge);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Bridge> requestWatchlist(int userId){
        try {
            HashMap<Integer, Bridge> bridgeMap = new HashMap<>();
            preparedStatement = connect.prepareStatement("SELECT * FROM bridges, bridge_watchlist WHERE bridges.id = bridge_watchlist.bridge_id AND bridge_watchlist.account_id = ?");
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Bridge bridge = new Bridge(resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("location"),
                        Double.parseDouble(resultSet.getString("latitude")),
                        Double.parseDouble(resultSet.getString("longitude")),
                        false);
                bridgeMap.put(resultSet.getInt("id"), bridge);
            }
        return bridgeMap;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getUsers(){
        try {
            ArrayList<String> result = new ArrayList<>();
            preparedStatement = connect.prepareStatement("SELECT email FROM account");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                result.add(resultSet.getString("email"));
            }

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteUser(String username){
        try {
            preparedStatement = connect.prepareStatement("DELETE FROM account WHERE email = ?");
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBridgeToWatchlist(int userId, int bridgeId){
        try {
            preparedStatement = connect.prepareStatement("INSERT INTO bridge_watchlist (account_id, bridge_id) VALUES (?, ?)");
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, bridgeId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }

    }

    public void removeBridgeFromWatchlist(int userId, int bridgeId) {
        try {
            preparedStatement = connect.prepareStatement("DELETE FROM bridge_watchlist WHERE account_id = ? AND bridge_id = ?");
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, bridgeId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }

    }
}
