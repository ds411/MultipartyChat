import javafx.scene.layout.Border;
import javafx.scene.layout.StackPane;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientGUI extends JFrame {

    private JPanel allMessages, clientOnly;
    private JTextField clientMessage;
    private JButton clientSendBtn;

    public ClientGUI() {
        super("Client Messenger");

        clientOnly = new JPanel();
        allMessages = new JPanel();

        setLayout(new BorderLayout());
        clientOnly.setLayout(new FlowLayout());

        clientMessage = new JTextField(50);
        clientSendBtn = new JButton("Send Message");
        clientSendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = clientMessage.getText();
                System.out.println(message);

            }
        });
        clientOnly.add(clientMessage);
        clientOnly.add(clientSendBtn);

        add(allMessages);
        add(clientOnly, BorderLayout.SOUTH);

    }

}
