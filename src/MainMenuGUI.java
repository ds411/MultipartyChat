import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;

public class MainMenuGUI extends JFrame {

    private JPanel mainForm, loginForm;
    private JButton clientBtn, serverBtn;
    private JTextField portField, ipField, usernameField, passwordField;
    private JLabel portLabel, ipLabel, usernameLabel, passworldLabel;

    public MainMenuGUI() {
        super("Chat Application");

        setLayout(new FlowLayout());
        mainForm = new JPanel();

        serverBtn = new JButton("Launch Server");
        serverBtn.setFocusPainted(false);
        serverBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
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
                        //ServerGUI serverGUI = new ServerGUI(server);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                /*

                try {
                    Server server = new Server(8000, 2, "");
                }
                catch (Exception a) {
                    a.printStackTrace();
                }*/
            }
        });


        clientBtn = new JButton("Launch Client");
        clientBtn.setFocusPainted(false);
        clientBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                /*try {
                    Client client = new Client("localhost", 8000);
                }
                catch (Exception a) {
                    a.printStackTrace();
                } */
                /*loginForm = new JPanel(new GridLayout(5, 2));

                portLabel = new JLabel("Port Number");
                ipLabel = new JLabel("IP");
                usernameLabel = new JLabel("Username");
                passworldLabel = new JLabel("Password");

                portField = new JTextField(32);
                ipField = new JTextField(32);
                usernameField = new JTextField(32);
                passwordField = new JTextField(32);

                JButton loginBtn = new JButton("Login");


                loginForm.add(ipLabel);
                loginForm.add(ipField);
                loginForm.add(portLabel);
                loginForm.add(portField);
                loginForm.add(usernameLabel);
                loginForm.add(usernameField);
                loginForm.add(passworldLabel);
                loginForm.add(passwordField);
                loginForm.add(loginBtn);

                remove(mainForm);
                add(loginForm);
                validate();*/
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
                        ClientGUI clientGUI = new ClientGUI(client);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mainForm.add(serverBtn);
        mainForm.add(clientBtn);

        add(mainForm);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
