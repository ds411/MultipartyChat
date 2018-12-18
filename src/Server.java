import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Server Class.
 * This creates a server object and server GUI.
 */
public class Server extends JFrame {

    boolean running = true; //boolean for running to see if the server is running
    private final String SERVER_PASSWORD;   //final string for the server password

    private SSLServerSocket ssocket;    //ssl server socket
    private LinkedBlockingQueue<Message> messageQueue;  //queue for the messages
    private ArrayBlockingQueue<ClientConnection> clientConneections;    //queue for the clients
    private ThreadPoolExecutor connectionPool;  //thread pool for connecting

    private JTextArea chatLog;  //text area for the chat log
    private JList connectionList;   //list for the current connections

    /**
     * Server Constructor to create the server.
     * @param port the port number of the server.
     * @param clientPoolSize the number of clients for the server.
     * @param password the password of the server.
     * @throws Exception
     */
    public Server(int port, int clientPoolSize, String password) throws Exception {
        super("Chat Server");   //title for the server GUI

        messageQueue = new LinkedBlockingQueue<>(); //new message queue for each server
        SERVER_PASSWORD = password; //set the password for the server

        //Load keystore from server certificate
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream in = new FileInputStream("cert.p12");
        keyStore.load(in, "projectCertificate".toCharArray());
        in.close();

        //Initialize key manager factory from keystore
        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance("SunX509");
        kmFactory.init(keyStore, "projectCertificate".toCharArray());

        //Initialize ssl context from key manager factory
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmFactory.getKeyManagers(), null, new SecureRandom());

        //Get ssl server socket factory from ssl context
        SSLServerSocketFactory ssocketFactory = sslContext.getServerSocketFactory();

        //Create ssl server socket from ssl server socket factory
        ssocket = (SSLServerSocket) ssocketFactory.createServerSocket(port);

        clientConneections = new ArrayBlockingQueue<>(clientPoolSize); //set the size of the connections
        connectionPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(clientPoolSize); //create threads for the size of the client pool

        //While server is running, accept clients and run new threads
        Thread acceptConnections = new Thread() {
            /**
             * Override for run acceptConnections.
             * While running is still true, allow the clients to be accepted
             */
            @Override
            public void run() {
                while(running) {
                    try {
                        //show the server is listening
                        System.out.println("Listening...");
                        //set the client to accept
                        SSLSocket client = (SSLSocket) ssocket.accept();
                        //if the pool is not full allow the connection
                        if(clientConneections.size() < clientPoolSize) {
                            //Create a new connection
                            System.out.println("Connection accepted");
                            ClientConnection clientConnection = new ClientConnection(client);
                        }
                    }
                    //catch socket exception and print
                    catch(SocketException se) {
                        System.out.println("Server socket closed.");
                    }
                    //catch exception and print
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //set as a daemon thread
        acceptConnections.setDaemon(true);
        acceptConnections.start();

        //While server is running, process every message added to the queue
        Thread processMessages = new Thread() {
            /**
             * Override run for processMessages.
             * While the server is still running, allow messages to be placed in the queue.
             */
            @Override
            public void run() {
                while(running) {
                    try {
                        //create the message and add it to the queue
                        Message m = messageQueue.take();
                        //append the messaged to the chat log wit the format
                        chatLog.append(String.format(
                                "[%s] %s: %s\n",
                                m.getTimestamp().truncatedTo(ChronoUnit.SECONDS).toString(),
                                m.getScreenName(),
                                m.getMessage()
                        ));
                        //for the client connections send the message
                        for(ClientConnection conn : clientConneections) {
                            conn.send(m);
                        }
                    }
                    //catch exception and print
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //set as a daemon thread
        processMessages.setDaemon(true);
        processMessages.start();

        createGUI();    //create the server GUI
    }

    /**
     * Close method.
     * Allows the connection to the server to be stopped.
     * Closes the socket, disconnects all clients and, sets running to false.
     */
    public void close() {
        //try to stop
        try {
            running = false;    //set running to false
            //for each connection, disconnect
            for(ClientConnection conn : clientConneections) {
                conn.disconnect();
            }
            ssocket.close();    //close the ssl socket
        }
        //catch exception and print
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * passwordHash method.
     * Allows a password to be hashed in SHA-256.
     * @param password takes in a plaintext string password
     * @return  the password as a hash
     * @throws Exception
     */
    private String passwordHash(String password) throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();   //create base64 encoder
        MessageDigest hashFunction = MessageDigest.getInstance("SHA-256");  //set the hash function
        return encoder.encodeToString(hashFunction.digest(password.getBytes()));    //return the hash
    }

    /**
     * CreateGUI method.
     * Creates the server GUI to display.
     * Keeps a log of messages and server controls.
     */
    private void createGUI() {
        JPanel rootPane = new JPanel(new BorderLayout());   //root panel for the GUI

        JPanel logPane = new JPanel(new BorderLayout());    //log panel for the messages
        JPanel connectionsPane = new JPanel(new BorderLayout());    //connection panel for the users

        JButton closeButton = new JButton("Close Server");  //button to close the server
        /**
         * closeBtn action listener.
         * Allows the server gui to be closed on the button click.
         */
        closeButton.addActionListener(evt -> {
            close();
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        chatLog = new JTextArea();  //text area for the chatlog
        chatLog.setEditable(false); //dont allow the text area to be edited
        chatLog.setLineWrap(true);
        JScrollPane scrollingChatLog = new JScrollPane(chatLog);    //scrolling panel for the chat log
        scrollingChatLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);  //dont allow horizontal scrolling

        logPane.add(closeButton, BorderLayout.NORTH);   //add the button
        logPane.add(scrollingChatLog, BorderLayout.CENTER); //add the chat log

        connectionList = new JList(clientConneections.toArray());   //create list of connectioins
        JButton kickButton = new JButton("Kick");   //button to kick users
        /**
         * Action listener for kickBtn.
         * Allows server to kick clients that are connected.
         */
        kickButton.addActionListener(evt -> {
            ((ClientConnection) connectionList.getSelectedValue()).disconnect(); //disconnects the selected client
        });

        connectionsPane.add(connectionList, BorderLayout.CENTER);   //add the connection list
        connectionsPane.add(kickButton, BorderLayout.SOUTH);    //add the kick button

        rootPane.add(logPane, BorderLayout.CENTER); //add the log panel
        rootPane.add(connectionsPane, BorderLayout.EAST);   //add the connection panel

        add(rootPane);  //add the panel

        //set the gui size and allow it to be visible
        setSize(1000, 600);
        setVisible(true);
    }

    /**
     * Client Connection class.
     * Allows a client to connect to the server.
     */
    private class ClientConnection implements Runnable {

        private SSLSocket client;   //ssl client object
        private ObjectInputStream in;   //object input
        private ObjectOutputStream out; //object output
        private boolean authenticated = false;  //authentication of the user false
        private String screenName;  //client connection screen name
        private String hash;    //client connection hash

        /**
         * Override for run in ClientConnection.
         * Checks if the server is running and the client is authenticated.
         */
        @Override
        public void run() {
            //if client is authenticated and server is running
            while(authenticated && running) {
                try {
                    //get the messaged the client sent
                    Message received = (Message) in.readObject();
                    //if client left, disconnect
                    if(received.getMessage().equals("DC")) {
                        disconnect();
                    }
                    else {
                        //process the message to a string with the time
                        Message m = new Message(
                                toString(),
                                received.getMessage(),
                                LocalTime.now()
                        );
                        messageQueue.put(m); //add the processed messaged to the queue
                    }
                }
                //catch socket exception or eof excetion and disconnect
                catch(SocketException | EOFException eof) {
                    disconnect();
                }
                //catch exception and print
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Client Connection constructor.
         * Allows clients to be authenticated and connection to server to be made.
         * @param client takes an ssl socket for the client to connect.
         * @throws Exception
         */
        public ClientConnection(SSLSocket client) throws Exception {
            this.client = client;   //set the client
            this.in = new ObjectInputStream(client.getInputStream());   //set the client input
            this.out = new ObjectOutputStream(client.getOutputStream());    //set the client output
            Message m = (Message)in.readObject();   //get the message for connection parameters
            String[] connectionParameters = m.getMessage().split("/#/");    //set the connection parameters

            //if the parameters meet the length and the server password is correct
            if(connectionParameters.length == 3 && SERVER_PASSWORD.equals(connectionParameters[0])) {
                authenticated = true;   //authenticate the user
                clientConneections.put(this);   //add the client to the connection queue
                connectionPool.execute(this);   //execute the client to the client pool
                connectionList.setListData(clientConneections.toArray());   //update the connection list
                screenName = connectionParameters[1];   //get the client screen name
                hash = passwordHash(connectionParameters[2]);   //get the hashed password from the client connection

                //let the client and server log the client has been authenticated
                send(new Message("SERVER", "Authentication successful.\n", LocalTime.now()));
            }
            else {
                //else let the client and server log the client has failed to authenticate
                out.writeObject(new Message("SERVER", "DC: Authentication failed.\n", LocalTime.now()));
                disconnect();   //disconnect the connection
            }
            //add the client connecting to the chat log
            chatLog.append(
                    String.format(
                            "[%s] %s %s",
                            LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString(),
                            this.toString(),
                            "has joined the room.\n"
                    )
            );
        }

        /**
         * Send method.
         * Allows messages to be sent over the server.
         * @param message takes the message and outputs it to the log.
         */
        public void send(Message message) {
            try {
                //try to output the message
                out.writeObject(message);
            }
            //catch an exception and print
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * getHash method.
         * This method returns the hash to check it against inputs.
         * @return hash for the password for authentication
         */
        public String getHash() {
            return hash;
        }

        /**
         * disconnect method.
         * Allows the client to be removed from the server connection.
         */
        public void disconnect() {
            try {
                Message m = null; //broadcast message
                //if user has not already been disconnected (to prevent kicking from broadcasting twice)
                if(authenticated) {
                    m = new Message(
                            "SERVER",
                            toString() + " has left the room.",
                            LocalTime.now()
                    );
                }
                //deauthenticate the client
                authenticated = false;
                //try to close the client
                client.close();
                //remove the client from the connections and the pool
                clientConneections.remove(this);
                connectionPool.remove(this);
                //update connection list
                connectionList.setListData(clientConneections.toArray());
                //broadcast that client has left
                if(m != null) {
                    messageQueue.put(m);
                }
            }
            //catch an exception and print
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Override for toString.
         *  Allows a messaged to be returned as a string.
         * @return a string in the correct format fo screen name and hash.
         */
        @Override
        public String toString() {
            return String.format("%s (%s)", screenName, hash);
        }

    }
}
