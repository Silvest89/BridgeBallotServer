package eu.silvenia.bridgeballot.server;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import eu.silvenia.bridgeballot.network.Bridge;
import io.netty.channel.Channel;
import sun.reflect.generics.tree.ReturnType;

import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.util.Base64.Encoder;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public synchronized void saveClient(Client client){
        try{
            preparedStatement = connect.prepareStatement("UPDATE account SET email = ?, access_level = ?, reputation = ? WHERE id = ?");
            preparedStatement.setString(1, client.getUserName());
            preparedStatement.setInt(2, client.getAccessLevel());
            preparedStatement.setInt(3, client.getReputation());
            preparedStatement.setInt(4, client.getId());
            preparedStatement.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
        }
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
                        channel,
                        resultSet.getInt("reputation"));

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
    public int[] validateLogin(String userName, String password, boolean isGooglePlus){
        try {
            String newPass = password.replaceAll("\\s", "");
            if(isGooglePlus) {
                if(!userExist(userName)){
                    createAccount(userName, newPass);
                }
                preparedStatement = connect.prepareStatement("SELECT * FROM account WHERE email = ?");
                preparedStatement.setString(1, userName);
            }
            else {
                preparedStatement = connect.prepareStatement("SELECT * FROM account WHERE email = ? AND password = ?");
                preparedStatement.setString(1, userName);
                preparedStatement.setString(2, newPass);
            }
            resultSet = preparedStatement.executeQuery();

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
    
    public ArrayList checkWatchListUser(int bridgeId){
        try{
            preparedStatement = connect.prepareStatement("SELECT token, account_id FROM bridge_watchlist b, account a WHERE b.bridge_id = ? AND b.account_id = a.id");
            preparedStatement.setInt(1, bridgeId);
            resultSet = preparedStatement.executeQuery();
            ArrayList<ArrayList> aggregated = new ArrayList<>();
            ArrayList<String> token = new ArrayList<>();
            ArrayList<Integer> user = new ArrayList<>();
            while(resultSet.next()){
                token.add(resultSet.getString("token"));
                user.add(resultSet.getInt("account_id"));
            } 
            aggregated.add(token);
            aggregated.add(user);
            return aggregated;
            
        }catch(SQLException e){
            
        }return null;
    }

    public boolean userExist(String userName){
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

    public Integer createAccount(String userName, String password){
        try {
            if (!userExist(userName)) {
                preparedStatement = connect.prepareStatement("INSERT INTO account(email, password, access_level) VALUES (?, ?, ?)");
                preparedStatement.setString(1, userName);
                preparedStatement.setString(2, password);
                preparedStatement.setInt(3, 1);
                preparedStatement.executeUpdate();
                return 0;
            }
            else {
                return 2;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return 1;
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
                Bridge bridge = bridgeMap.get(resultSet.getInt("id"));
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

    public boolean createBridge(ArrayList<String> newBridge) {
        try {
            preparedStatement = connect.prepareStatement("INSERT INTO bridges (name, location, latitude, longitude) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, newBridge.get(2));
            preparedStatement.setString(2, newBridge.get(3));
            preparedStatement.setString(3, newBridge.get(4));
            preparedStatement.setString(4, newBridge.get(5));
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                int last_inserted_id = rs.getInt(1);
                Bridge bridge = new Bridge(last_inserted_id,
                        newBridge.get(2),
                        newBridge.get(3),
                        Double.parseDouble(newBridge.get(4)),
                        Double.parseDouble(newBridge.get(5)),
                        false);
                BridgeBallotServer.bridgeMap.put(last_inserted_id, bridge);
                return true;
            }

        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBridge(ArrayList<String> deleteBridge) {
        try {
            preparedStatement = connect.prepareStatement("DELETE FROM bridges WHERE id=?");
            preparedStatement.setString(1, deleteBridge.get(0));
            preparedStatement.executeUpdate();


            BridgeBallotServer.bridgeMap.values().remove(deleteBridge.get(0));
            return true;

        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return false;

    }

    public boolean updateBridge(ArrayList<String> updateBridge) {
        try {
            preparedStatement = connect.prepareStatement("UPDATE bridges SET name=?, location=?, latitude=?, longitude=? WHERE id=?");
            preparedStatement.setString(1, updateBridge.get(2));
            preparedStatement.setString(2, updateBridge.get(3));
            preparedStatement.setString(3, updateBridge.get(4));
            preparedStatement.setString(4, updateBridge.get(5));
            preparedStatement.setString(5, updateBridge.get(1));
            preparedStatement.executeUpdate();


            int id = Integer.parseInt(updateBridge.get(1));
            Bridge bridge = BridgeBallotServer.bridgeMap.get(id);
            bridge.setName(updateBridge.get(2));
            bridge.setLocation(updateBridge.get(3));
            bridge.setLatitude(Double.parseDouble(updateBridge.get(4)));
            bridge.setLongitude(Double.parseDouble(updateBridge.get(5)));
            BridgeBallotServer.bridgeMap.put(id, bridge);

            return true;

        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return false;

    }
    
    public ArrayList getReputation(int bridgeId){
        try {
            ArrayList<String[]> reputationList = new ArrayList<>();
            preparedStatement = connect.prepareStatement("SELECT ur.id, ur.bridge_id, a.id, a.email, a.reputation, ur.time, ur.status FROM account a, bridge_vote ur WHERE a.id = ur.account_id AND ur.bridge_id = ? ORDER BY ur.time DESC LIMIT 5");
            preparedStatement.setInt(1, bridgeId);
            
            resultSet = preparedStatement.executeQuery(); 
            while(resultSet.next()){
                String[] client = new String[7];
                client[0] = Integer.toString(resultSet.getInt("ur.id"));
                client[1] = Integer.toString(resultSet.getInt("a.id"));
                client[2] = resultSet.getString("a.email");
                client[3] = Integer.toString(resultSet.getInt("a.reputation"));
                client[4] = Integer.toString(resultSet.getInt("ur.time"));
                client[5] = Integer.toString(resultSet.getInt("ur.status"));
                client[6] = Integer.toString(resultSet.getInt("ur.bridge_id"));

                reputationList.add(client);
            }return reputationList;
     
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }return null;
    }
    
    public boolean saveDislike(int voteId, int accountId){
        try {
            System.out.println(voteId + " " + accountId);
            
            preparedStatement = connect.prepareStatement("SELECT * FROM vote_dislike WHERE vote_id = ? AND account_id = ?");
            preparedStatement.setInt(1, voteId);
            preparedStatement.setInt(2, accountId);
            
            resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()){
            
            
            preparedStatement = connect.prepareStatement("INSERT INTO vote_dislike VALUES(?, ?)");
            
            preparedStatement.setInt(1, voteId);
            preparedStatement.setInt(2, accountId);
            
            preparedStatement.executeUpdate();
            return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void setReputation(int targetId){
        try {
            preparedStatement = connect.prepareStatement("SELECT reputation FROM account WHERE id = ?");
            preparedStatement.setInt(1, targetId);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                int reputation = resultSet.getInt("reputation");
                reputation -= 2;
            preparedStatement = connect.prepareStatement("UPDATE account SET reputation = ? WHERE id = ?");
            preparedStatement.setInt(1, reputation);
            preparedStatement.setInt(2, targetId);
            
            preparedStatement.executeUpdate();
                    }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
