import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import java.awt.*;
import java.awt.event.*;
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

        serverBtn = new JButton("Connect As Server");
        serverBtn.setFocusPainted(false);
        serverBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Server server = new Server(8000, 2);
                }
                catch (Exception a) {
                    a.printStackTrace();
                }
            }
        });


        clientBtn = new JButton("Connect As Client");
        clientBtn.setFocusPainted(false);
        clientBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*try {
                    Client client = new Client("localhost", 8000);
                }
                catch (Exception a) {
                    a.printStackTrace();
                } */
                loginForm = new JPanel(new GridLayout(5, 2));

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
                validate();
            }
        });

        mainForm.add(serverBtn);
        mainForm.add(clientBtn);

        add(mainForm);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
