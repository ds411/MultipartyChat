import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
import java.util.Base64;

/**
 * Client class.
 * This class creates the client object and client GUI.
 */
public class Client extends JFrame {

    private SSLSocket socket;   //client ssl socket
    private ObjectOutputStream out; //client output
    private ObjectInputStream in;   //client input
    private String screenName;  //client screen name
    private Mac hmac;    //hmac algorithm
    private boolean authenticated = false;  //boolean to check if the client is authenticated
    private Base64.Encoder encoder;

    private JFrame window = this;   //frame for the client GUI
    private JTextArea chatLog;  //textarea for the chat log of sent messages

    /**
     * Client Constructor.
     * Sets the client to allow them to connect to the server, creates the GUI.
     * @param ip for the IP of the server to connect to.
     * @param port for the port of the server.
     * @param password for the password of the server.
     * @param screenName for the clients screen name
     * @param keyString for the hmac to authenticate to other clients
     * @throws Exception
     */
    public Client(String ip, int port, String password, String screenName, String keyString) throws Exception {
        super("Chat Client");   //set the title of the client gui

        SecretKeySpec keySpec = new SecretKeySpec(keyString.getBytes(), "HmacSHA256");
        hmac = Mac.getInstance("HmacSHA256");
        hmac.init(keySpec);
        encoder = Base64.getEncoder();

        this.screenName = screenName;   //set the client screen name

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
            //try and connect to the server based on the ip and port input
            socket.connect(new InetSocketAddress(ip, port), 5000);
        }
        //catch socket exception
        catch(SocketException e) {
            //close the socket and output a failed to connect message
            socket.close();
            JOptionPane.showMessageDialog(null, "Could not establish connection.", "Connection Failure", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //if the socket is connected
        if(socket.isConnected()) {
            //set the input and output objects
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            authenticate(password); //authenticate the password entered

            /**
             * Proccess messages thread override for run.
             * While the client is authenticated, allow them to send messages.
             * Otherwise disconnect the client.
             */
            Thread processMessages = new Thread() {
                @Override
                public void run() {
                    //while the client has been authenticated
                    while (authenticated) {
                        try {
                            //try to allow the client to send messages
                            processMessage((Message) in.readObject());
                        }
                        //catch socket or eof exception
                        catch (SocketException | EOFException eof) {
                            authenticated = false;  //deauth the user
                            chatLog.append("Disconnected.  Closing client..."); //let the client know they are disconnect
                            try {
                                //try and wait, close the GUI
                                sleep(2000);
                                dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                            }
                            //catch exception and print
                            catch(Exception e) {
                                e.printStackTrace();
                            }

                        }
                        //catch exception and print
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            //set as a daemon thread and start
            processMessages.setDaemon(true);
            processMessages.start();

            //if the client is authenticated, create a gui
            if(authenticated) {
                createGUI();
            }
        }
    }

    /**
     * Authenticate method.
     * This method authenticates the client to the server based on the password.
     * @param password is the password the client inputs to be authenticated.
     */
    private void authenticate(String password) {
        try {
            //try to send to the server the clients inputs
            out.writeObject(new Message(
                    screenName,
                    password,
                    null,
                    LocalTime.now())
            );
            //set the message authentication response to the output
            Message authenticationResponse = (Message) in.readObject();
            //if the response is not valid
            if (authenticationResponse.getMessage().substring(0,2).equals("DC")) {
                //close the socket
                socket.close();
            } else {
                //else the client can authenticate
                authenticated = true;
            }
        }
        //catch exception and print
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send method.
     * This method sends a message over the server.
     * @param message is a message tro be sent.
     */
    public void send(Message message) {
        try {
            //try to output the message
            out.writeObject(message);
        }
        //catch socket exception or eof exception and close the gui
        catch(SocketException | EOFException se) {
            dispatchEvent(new WindowEvent(
                    this,
                    WindowEvent.WINDOW_CLOSING
            ));
        }
        //catch exception and print
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process message method.
     * Adds a message to the chat log.
     * @param m is a message to be added to the chat log.
     */
    public void processMessage(Message m) {
        //set the HMAC of the message
        String messageHmac = m.getHmac();
        //if its null set it to be an empty string
        if(messageHmac == null) {
            messageHmac = "";
        }
        //else set the hmac to the one the message uses
        else {
            messageHmac = String.format("(%s)", messageHmac);
        }
        //append the message in the correct format to the chat log text area
        chatLog.append(
                String.format(
                        "[%s] %s: %s %s\n",
                        m.getTimestamp()
                                .truncatedTo(ChronoUnit.SECONDS)
                                .toString(),
                        m.getScreenName(),
                        m.getMessage(),
                        messageHmac
                        )
        );
    }

    /**
     * Disconnect method.
     * Allows a client to disconnect from the server.
     */
    private void disconnect() {
        try {
            //try and send a message saying the client has disconnected
            send(new Message(
                    screenName,
                    "DC",
                    null,
                    LocalTime.now()
            ));
            //close the socket
            socket.close();
            //close the GUI
            dispatchEvent(new WindowEvent(
                    this,
                    WindowEvent.WINDOW_CLOSING
            ));
        }
        //catch socket exception and eof exception and disconnect
        catch(SocketException | EOFException eof) {
            disconnect();
        }
        //catch exception and print
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * hmac method.
     * This method hashes the messages and adds it to a byte array.
     * @param message to be hashes using the hmac.
     * @return the encoded bytes of the message.
     */
    private String hmac(String message) {
        byte[] macBytes = hmac.doFinal(message.getBytes());
        return encoder.encodeToString(macBytes);
    }

    /**
     * Create GUI method.
     * This creates the client GUI to display.
     */
    private void createGUI() {
        JPanel clientOnly = new JPanel();   //Panel for the client text box

        chatLog = new JTextArea();  //text area for the chat log
        chatLog.setEditable(false); //make the text area not editable
        chatLog.setLineWrap(true);
        JScrollPane scrollingChatLog = new JScrollPane(chatLog);    //scrolling pane for the chat log

        setLayout(new BorderLayout());   //set the layout of the gui
        clientOnly.setLayout(new FlowLayout()); //set the layout for the textbox and send button

        JTextField clientMessage = new JTextField(50);  //client message field
        JButton clientSendBtn = new JButton("Send Message");    //button to send the message
        /**
         * Action listener for the send message btn.
         * If the btn is clicked, the message from the text field is sent along
         * with the screen name and timestamp to create a message.
         */
        clientSendBtn.addActionListener(evt -> {
            //get message text from text field
            String messageText = clientMessage.getText();
            //send a new message
            send(new Message(
                    screenName,
                    messageText,
                    hmac(messageText),
                    LocalTime.now()
            ));
            //reset the text field
            clientMessage.setText("");
        });
        //add the field and btn to the panel
        clientOnly.add(clientMessage);
        clientOnly.add(clientSendBtn);

        JButton disconnectButton = new JButton("Disconnect");   //btn to allow for disconnecting
        /**
         * Action listener for disconnect btn.
         * If clicked, the client will disconnect from the server.
         */
        disconnectButton.addActionListener(evt -> {
            disconnect();
        });

        add(disconnectButton, BorderLayout.NORTH);  //add the button to the frame
        add(scrollingChatLog, BorderLayout.CENTER); //add the chat log to the frame
        add(clientOnly, BorderLayout.SOUTH);    //add the field and send btn to the frame

        //set the size and make the gui visible
        setSize(1000, 600);
        setVisible(true);
    }
}
