import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CashierCurrentItemsPanel extends JPanel {
    public JPanel itemContainer = new JPanel();

    public CashierCurrentItemsPanel(ArrayList<ItemRecord> currentItems) {
        setLayout(null);
        setOpaque(false);

        itemContainer.setLayout(new FlowLayout(FlowLayout.LEFT));
        itemContainer.setOpaque(false);
        itemContainer.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane scrollPane = new JScrollPane(itemContainer);
        scrollPane.setBounds(0, 0, 1200, 70);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane);

        refresh(itemContainer, currentItems);
    }

    private void populateMenu(JPanel itemContainer, ArrayList<ItemRecord> currentItems) {
        int fontSize = 14;
        String fontName = "Arial";
        Color fontColor = Color.BLACK;

        for (ItemRecord item : currentItems) {
            JPanel card = new JPanel();
            card.setPreferredSize(new Dimension(52, 52));
            card.setBackground(new Color(205, 255, 186));
            card.setOpaque(true);
            card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

            JLabel idLabel = new JLabel(String.valueOf(item.getId()), SwingConstants.CENTER);
            idLabel.setFont(new Font(fontName, Font.BOLD, fontSize));
            idLabel.setForeground(fontColor);

            JLabel nameLabel = new JLabel(item.getName(), SwingConstants.CENTER);
            nameLabel.setFont(new Font(fontName, Font.PLAIN, fontSize));
            nameLabel.setForeground(fontColor);

            idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(idLabel);
            card.add(nameLabel);

            itemContainer.add(card);
        }
    }

    public void refresh(JPanel itemContainer, ArrayList<ItemRecord> currentItems) {
        if (currentItems != null) {
            itemContainer.removeAll();
            populateMenu(itemContainer, currentItems);
            itemContainer.revalidate();
            itemContainer.repaint();
        }
    }

}
