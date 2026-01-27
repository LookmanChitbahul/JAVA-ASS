package test;

import ui.SalesUI;
import javax.swing.*;

public class TestSalesUIOnly {
    public static void main(String[] args) {
        System.out.println("=== SALES UI TEST ===");
        System.out.println("1. Creating JFrame...");
        System.out.println("2. Loading SalesUI...");

        // Force database to use mock data (optional)
        System.setProperty("test.mode", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Sales UI - Standalone Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 700);
                frame.setLocationRelativeTo(null);

                SalesUI salesUI = new SalesUI();
                frame.add(salesUI);
                frame.setVisible(true);

                System.out.println("✅ SUCCESS: SalesUI is running!");
                System.out.println("📊 Note: Using test mode with sample data");

            } catch (Exception e) {
                System.err.println("❌ FAILED: " + e.getMessage());
                e.printStackTrace();

                // Show error in dialog
                JOptionPane.showMessageDialog(null,
                        "SalesUI Test Failed:\n" + e.getMessage() +
                                "\n\nCheck console for details.",
                        "Test Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}