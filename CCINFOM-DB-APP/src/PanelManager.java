import javax.swing.*;

public class PanelManager {
    private static JPanel currentPanel = null;
    private static final Theme defaultTheme = Theme.MONOCHROME;
    private static final JFrame frame = defaultTheme.createFrame();

    public static void initFrame() {
        LoginPanel loginPanel = new LoginPanel(defaultTheme);
        currentPanel = loginPanel;
        frame.add(loginPanel);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.repaint();
        frame.revalidate();
    }

    public static void updateCurrentPanel(JPanel newCurrentPanel) {
        if (currentPanel instanceof KitchenPanel) {
            ((KitchenPanel) currentPanel).stopRefreshTimer();
            System.out.println("KitchenPanel timer stopped.");
        }

        frame.remove(currentPanel);
        currentPanel = newCurrentPanel;
        frame.add(currentPanel);
        frame.repaint();
        frame.revalidate();
    }
}
