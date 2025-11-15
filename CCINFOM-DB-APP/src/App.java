import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JFrame;

public class App {
    public static final String schemaName = "korean_bbq_pos";

    public static void main(String[] args) {
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + schemaName);
            System.out.println("Successfully created " + schemaName + " or it already exists!");
            stmt.execute("USE " + schemaName);
            System.out.println("Successfully using " + schemaName + "!");

            System.out.println("Creating tables...");
            for (TableCreationSQLCodes sql : TableCreationSQLCodes.values()) {
                stmt.execute(sql.getSQL());
                System.out.println("Successfully executed " + sql.name() + "!");
            }
            for (InsertInitialDataSQLCodes sql : InsertInitialDataSQLCodes.values()) {
                stmt.execute(sql.getSQL());
                System.out.println("Successfully executed " + sql.name() + "!");
            }

            System.out.println("The tables have been created!");
        } catch (SQLException f) {
            throw new RuntimeException(f);
        }

        PanelManager.initFrame();
    }
}
