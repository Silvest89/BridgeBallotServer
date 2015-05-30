package bridgeballotserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.SQLException;
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
    
    public boolean validateLogin(String userName, String password){
        try {
            preparedStatement = connect.prepareStatement("SELECT * FROM account WHERE user_name = ? AND password = ?");
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();
            
            return resultSet.next();
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return false;        
    }
}
