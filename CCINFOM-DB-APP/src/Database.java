import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/?user=root";
    private static final String user = "root";
    private static final String password = "Just4Game$";

    public static Connection getConnection() throws SQLException, SQLException {

        return DriverManager.getConnection(url, user, password);
    }

}
