import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainMenuGUI extends JFrame {

    private JButton clientBtn, serverBtn;

    public MainMenuGUI() {
        super("Chat Application");

        setLayout(new FlowLayout());

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
                try {
                    Client client = new Client("localhost", 8000);
                }
                catch (Exception a) {
                    a.printStackTrace();
                }
            }
        });

        add(serverBtn);
        add(clientBtn);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
