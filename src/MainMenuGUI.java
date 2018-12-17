import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;

public class MainMenuGUI extends JFrame {

    public MainMenuGUI() {
        super("Chat Application");

        setLayout(new FlowLayout());
        JPanel mainForm = new JPanel();

        JButton serverBtn = new JButton("Launch Server");
        serverBtn.setFocusPainted(false);
        serverBtn.addActionListener(evt -> {
            JTextField portField = new JTextField();
            JTextField passwordField = new JTextField();
            JTextField maxSize = new JTextField();
            Object[] fields = {
                    "Server Port: ", portField,
                    "Server Password: ", passwordField,
                    "Max Size: ", maxSize,
            };

            int option = JOptionPane.showConfirmDialog(null, fields, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
            if(option == JOptionPane.OK_OPTION) {
                try {
                    Server server = new Server(
                            Integer.parseInt(portField.getText()),
                            Integer.parseInt(maxSize.getText()),
                            passwordField.getText()
                    );
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });


        JButton clientBtn = new JButton("Launch Client");
        clientBtn.setFocusPainted(false);
        clientBtn.addActionListener(evt -> {
            JTextField ipField = new JTextField();
            JTextField portField = new JTextField();
            JTextField passwordField = new JTextField();
            JTextField screenNameField = new JTextField();
            JTextField hashField = new JTextField();
            Object[] fields = {
                    "IP: ", ipField,
                    "Port: ", portField,
                    "Server Password: ", passwordField,
                    "Screen Name: ", screenNameField,
                    "Hash String: ", hashField
            };

            int option = JOptionPane.showConfirmDialog(null, fields, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
            if(option == JOptionPane.OK_OPTION) {
                try {
                    Client client = new Client(
                            ipField.getText(),
                            Integer.parseInt(portField.getText()),
                            passwordField.getText(),
                            screenNameField.getText(),
                            hashField.getText()
                    );
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mainForm.add(serverBtn);
        mainForm.add(clientBtn);

        add(mainForm);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
