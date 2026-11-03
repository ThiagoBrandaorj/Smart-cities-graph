package src;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // usa o padrao
        }
        SwingUtilities.invokeLater(() -> new Janela().setVisible(true));
    }
}