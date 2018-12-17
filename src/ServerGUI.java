import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class ServerGUI extends JFrame {

    private Server server;

    public ServerGUI(Server server) {
        super("Chat Server");
        this.server = server;

        JPanel rootPane = new JPanel(new BorderLayout());

        JPanel logPane = new JPanel(new BorderLayout());
        JPanel connectionsPane = new JPanel(new BorderLayout());

        JButton closeButton = new JButton("Close Server");
        closeButton.addActionListener(evt -> {
            server.close();
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        JTextArea chatLog = new JTextArea();
        chatLog.setEditable(false);
        JScrollPane scrollingChatLog = new JScrollPane(chatLog);

        logPane.add(closeButton, BorderLayout.NORTH);
        logPane.add(scrollingChatLog, BorderLayout.CENTER);

        JList connectionList = new JList();
        JButton kickButton = new JButton("Kick");
        kickButton.addActionListener(evt -> {
            //(ClientConnection)connectionList.getSelectedValue().disconnect();
        });

        connectionsPane.add(connectionList, BorderLayout.CENTER);
        connectionsPane.add(kickButton, BorderLayout.SOUTH);

        rootPane.add(logPane, BorderLayout.CENTER);
        rootPane.add(connectionsPane, BorderLayout.EAST);

        setSize(1000, 600);
        setVisible(true);
    }

}
