import java.sql.*;

public class App {
    public static final String schemaName = "korean_bbq_pos";

    public static void main(String[] args) {
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + schemaName);
            System.out.println("Successfully created " + schemaName + " or it already exists!");
            stmt.execute("USE " + schemaName);
            System.out.println("Successfully using " + schemaName + "!");
            //

            System.out.println("Creating tables...");
            for (TableCreationSQLCodes sql : TableCreationSQLCodes.values()) {
                stmt.execute(sql.getSQL());
                System.out.println("Successfully executed " + sql.name() + "!");
            }

            System.out.println("Inserting data...");
            String query = "SELECT COUNT(*) FROM users";
            try (PreparedStatement stmt2 = connection.prepareStatement(query);
                 ResultSet rs = stmt2.executeQuery()) {

                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        for (InsertInitialDataSQLCodes sql : InsertInitialDataSQLCodes.values()) {
                            stmt.execute(sql.getSQL());
                            System.out.println("Successfully inserted " + sql.name() + " data!");
                        }
                    } else {
                        System.out.println("Table has data.");
                    }
                }
            }

            System.out.println("The tables have been created!");
        } catch (SQLException f) {
            throw new RuntimeException(f);
        }

        Theme.loadFont("CCINFOM-DB-APP/assets/fonts/DMSans.ttf");
        PanelManager.initFrame();
    }
}
