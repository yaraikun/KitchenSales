import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;

public class CashierMenuPanel extends JPanel {
    public CashierMenuPanel(ArrayList<ItemRecord> currentItems, CashierCurrentItemsPanel itemPanel) {
        setLayout(null);
        setOpaque(true);

        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new GridLayout(2, 12, 10, 10));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(menuContainer);
        scrollPane.setBounds(0, 0, 1200, 330);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane);

        ArrayList<ItemRecord> items = loadItemsFromDB();
        populateMenu(menuContainer, items, currentItems, itemPanel);
    }

    private ArrayList<ItemRecord> loadItemsFromDB() {
        ArrayList<ItemRecord> list = new ArrayList<>();
        String query = "SELECT item_id, item_name, unit_price FROM items";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("USE " + App.schemaName);
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                list.add(new ItemRecord(
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getDouble("unit_price")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void populateMenu(JPanel menuContainer, ArrayList<ItemRecord> items, ArrayList<ItemRecord> currentItems, CashierCurrentItemsPanel itemPanel) {
        int fontSize = 20;
        String fontName = "Arial";
        Color fontColor = Color.WHITE;

        for (ItemRecord item : items) {
            JPanel card = new JPanel();
            card.setPreferredSize(new Dimension(200, 120));
            card.setBackground(new Color(80, 82, 84));
            card.setOpaque(true);
            card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            card.setLayout(new BorderLayout());

            JLabel idLabel = new JLabel(String.valueOf(item.getId()), SwingConstants.CENTER);
            idLabel.setBounds(0,5,50,20);
            idLabel.setFont(new Font(fontName, Font.BOLD, fontSize));
            idLabel.setForeground(fontColor);

            JLabel nameLabel = new JLabel(item.getName(), SwingConstants.CENTER);
            nameLabel.setFont(new Font(fontName, Font.BOLD, fontSize));
            nameLabel.setForeground(fontColor);

            JLabel priceLabel = new JLabel("₱" + item.getPrice(), SwingConstants.CENTER);
            priceLabel.setFont(new Font(fontName, Font.PLAIN, fontSize - 4));
            priceLabel.setForeground(fontColor);

            card.add(idLabel);
            card.add(nameLabel, BorderLayout.CENTER);
            card.add(priceLabel, BorderLayout.SOUTH);

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    currentItems.add(item);
                    itemPanel.refresh(itemPanel.itemContainer, currentItems);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(new Color(255, 255, 174));
                    card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    card.setBackground(new Color(80, 82, 84));
                }
            });

            menuContainer.add(card);
        }
    }
}
