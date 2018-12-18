import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Client extends JFrame {

    private SSLSocket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String screenName;
    private String hash;
    private boolean authenticated = false;

    private JFrame window = this;
    private JTextArea chatLog;

    public Client(String ip, int port, String password, String screenName, String hash) throws Exception {
        super("Chat Client");

        this.hash = hash;
        this.screenName = screenName;

        //Load keystore from server certificate
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream fileIn = new FileInputStream("cert.pem");
        X509Certificate cert = (X509Certificate) CertificateFactory
                .getInstance("X.509")
                .generateCertificate(fileIn);
        keyStore.load(null);
        keyStore.setCertificateEntry("cert", cert);
        fileIn.close();

        //Initialize trust manager factory from keystore
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509");
        tmFactory.init(keyStore);

        //Initialize ssl context from trust manager factory
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(
                null,
                tmFactory.getTrustManagers(),
                new SecureRandom()
        );

        //Get ssl socket factory from ssl context
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        //Create ssl socket from ssl socket factory
        socket = (SSLSocket) socketFactory.createSocket();
        //Force connections to require TLSv1.2 because sslContext allows downgrading
        socket.setEnabledProtocols(new String[]{"TLSv1.2"});
        try {
            socket.connect(new InetSocketAddress(ip, port), 5000);
        } catch(SocketException e) {
            System.out.println("Could not establish connection.");
            return;
        }

        if(socket.isConnected()) {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            authenticate(password);

            Thread processMessages = new Thread() {
                @Override
                public void run() {
                    while (authenticated) {
                        try {
                            processMessage((Message) in.readObject());
                        } catch (SocketException | EOFException eof) {
                            authenticated = false;
                            chatLog.append("Disconnected.  Closing client...");
                            try {
                                sleep(2000);
                                dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                            } catch(Exception e) {
                                e.printStackTrace();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            processMessages.setDaemon(true);
            processMessages.start();
            if(authenticated) {
                createGUI();
            }
        }
    }

    private void authenticate(String password) {
        try {
            out.writeObject(new Message(
                    screenName,
                    String.format("%s/#/%s/#/%s", password, screenName, hash),
                    LocalTime.now())
            );
            Message authenticationResponse = (Message) in.readObject();
            if (authenticationResponse.getMessage().substring(0,2).equals("DC")) {
                socket.close();
            } else {
                authenticated = true;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Message message) {
        try {
            out.writeObject(message);
        }
        catch(SocketException | EOFException se) {
            dispatchEvent(new WindowEvent(
                    this,
                    WindowEvent.WINDOW_CLOSING
            ));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void processMessage(Message m) {
        chatLog.append(
                String.format(
                        "[%s] %s: %s\n",
                        m.getTimestamp()
                                .truncatedTo(ChronoUnit.SECONDS)
                                .toString(),
                        m.getScreenName(),
                        m.getMessage()
                        )
        );
    }

    private void disconnect() {
        try {
            send(new Message(
                    screenName,
                    "DC",
                    LocalTime.now()
            ));
            socket.close();
            dispatchEvent(new WindowEvent(
                    this,
                    WindowEvent.WINDOW_CLOSING
            ));
        }
        catch(SocketException | EOFException eof) {}
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void createGUI() {
        JPanel clientOnly = new JPanel();
        //JPanel allMessages = new JPanel();

        chatLog = new JTextArea();
        chatLog.setEditable(false);
        chatLog.setLineWrap(true);
        JScrollPane scrollingChatLog = new JScrollPane(chatLog);

        setLayout(new BorderLayout());
        clientOnly.setLayout(new FlowLayout());

        JTextField clientMessage = new JTextField(50);
        JButton clientSendBtn = new JButton("Send Message");
        clientSendBtn.addActionListener(evt -> {
            send(new Message(
                    screenName,
                    clientMessage.getText(),
                    LocalTime.now()
            ));
            clientMessage.setText("");
        });
        clientOnly.add(clientMessage);
        clientOnly.add(clientSendBtn);

        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(evt -> {
            disconnect();
        });

        add(disconnectButton, BorderLayout.NORTH);
        add(scrollingChatLog, BorderLayout.CENTER);
        add(clientOnly, BorderLayout.SOUTH);

        setSize(1000, 600);
        setVisible(true);
    }
}
