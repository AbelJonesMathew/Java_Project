package com.campusvoice.ui.login;

import com.campusvoice.config.DatabaseConfig;
import javax.swing.*;
import java.awt.*;

final class SharedDatabaseSetupDialog {
    private SharedDatabaseSetupDialog() {}

    static void show(JFrame parent, Runnable connected) {
        JDialog dialog = new JDialog(parent, "Connect shared database", true);
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        form.add(new JLabel("Use the same database on every student and HoD computer."));
        form.add(new JLabel("JDBC URL (from your database administrator)"));
        JTextField url = new JTextField("jdbc:postgresql://", 45);
        form.add(url);
        form.add(new JLabel("Database username"));
        JTextField user = new JTextField("postgres");
        form.add(user);
        form.add(new JLabel("Database password"));
        JPasswordField password = new JPasswordField();
        form.add(password);
        JLabel status = new JLabel("Connection is encrypted and remembered for this Windows account.");
        form.add(status);
        JButton connect = new JButton("Test and connect");
        form.add(connect);
        connect.addActionListener(event -> {
            String connectionUrl = url.getText().trim();
            String username = user.getText().trim();
            char[] secret = password.getPassword();
            String value = new String(secret);
            java.util.Arrays.fill(secret, '\0');
            connect.setEnabled(false);
            url.setEnabled(false);
            user.setEnabled(false);
            password.setEnabled(false);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            status.setText("Testing the shared database connection...");
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    DatabaseConfig.connectSharedDatabase(connectionUrl, username, value, true);
                    return null;
                }
                @Override protected void done() {
                    connect.setEnabled(true);
                    url.setEnabled(true);
                    user.setEnabled(true);
                    password.setEnabled(true);
                    password.setText("");
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    try {
                        get();
                        connected.run();
                        JOptionPane.showMessageDialog(dialog,
                                "Connected and remembered securely. Future launches connect automatically.\n"
                                + "Set up the same database once on each other computer. Existing local complaints stay local.");
                        dialog.dispose();
                    } catch (Exception error) {
                        Throwable cause = error.getCause() != null ? error.getCause() : error;
                        String message = cause instanceof IllegalArgumentException ? cause.getMessage()
                                : "Could not connect. Check the URL, credentials, network access, and CampusVoice database schema.";
                        status.setText("Connection unchanged. Check the settings and retry.");
                        JOptionPane.showMessageDialog(dialog, message, "Connection failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });
        dialog.setContentPane(form);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
