import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalTime;

public class Client extends JFrame {

    private SSLSocket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String screenName;
    private String hash;
    private boolean authenticated = false;

    private JTextArea chatLog;

    public Client(String ip, int port, String password, String screenName, String hash) throws Exception {
        super("Chat Client");

        //Load keystore from server certificate
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream fileIn = new FileInputStream("cert.pem");
        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fileIn);
        keyStore.load(null);
        keyStore.setCertificateEntry("cert", cert);
        fileIn.close();

        //Initialize key manager factory from keystore
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509");
        tmFactory.init(keyStore);

        //Initialize ssl context from key manager factory
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, tmFactory.getTrustManagers(), new SecureRandom());

        //Get ssl socket factory from ssl context
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        //Create ssl socket from ssl socket factory
        socket = (SSLSocket) socketFactory.createSocket(ip, port);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        this.hash = hash;
        this.screenName = screenName;

        authenticate(password);

        Thread processMessages = new Thread() {
            @Override
            public void run() {
                while(authenticated) {
                    try {
                        processMessage((Message)in.readObject());
                    }
                    catch(EOFException eof) {
                        authenticated = false;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        processMessages.setDaemon(true);
        processMessages.start();

        JPanel clientOnly = new JPanel();
        //JPanel allMessages = new JPanel();

        chatLog = new JTextArea();
        chatLog.setEditable(false);
        JScrollPane scrollingChatLog = new JScrollPane(chatLog);

        setLayout(new BorderLayout());
        clientOnly.setLayout(new FlowLayout());

        JTextField clientMessage = new JTextField(50);
        JButton clientSendBtn = new JButton("Send Message");
        clientSendBtn.addActionListener(evt -> {
            send(new Message(screenName, clientMessage.getText(), LocalTime.now()));
            clientMessage.setText("");
        });
        clientOnly.add(clientMessage);
        clientOnly.add(clientSendBtn);

        add(scrollingChatLog, BorderLayout.CENTER);
        add(clientOnly, BorderLayout.SOUTH);

        setSize(1000,600);
        setVisible(true);
    }

    private void authenticate(String password) throws Exception {
        out.writeObject(new Message(
                screenName,
                String.format("%s/#/%s/#/%s", password, screenName, hash),
                LocalTime.now())
        );
        Message authenticationResponse = (Message)in.readObject();
        if(authenticationResponse.getMessage().equals("Authentication failed.  Disconnecting.")) {
            socket.close();
        }
        else {
            authenticated = true;
        }
    }

    public void send(Message message) {
        try {
            out.writeObject(message);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void processMessage(Message m) {
        chatLog.append(
                String.format(
                        "[%s] %s: %s\n",
                        m.getTimestamp().toString(),
                        m.getScreenName(),
                        m.getMessage()
                        )
        );
    }
}
