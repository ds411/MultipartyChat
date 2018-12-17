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
import java.util.Base64;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class Server extends JFrame {

    boolean running = true;
    private final String SERVER_PASSWORD;

    private SSLServerSocket ssocket;
    private LinkedBlockingQueue<Message> messageQueue;
    private ArrayBlockingQueue<ClientConnection> clientConneections;
    private ThreadPoolExecutor connectionPool;

    private JTextArea chatLog;
    private JList connectionList;

    public Server(int port, int clientPoolSize, String password) throws Exception {
        super("Chat Server");

        messageQueue = new LinkedBlockingQueue<>();
        SERVER_PASSWORD = password;

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

        clientConneections = new ArrayBlockingQueue<>(clientPoolSize);
        connectionPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(clientPoolSize);

        //While server is running, accept clients and run new threads
        Thread acceptConnections = new Thread() {
            @Override
            public void run() {
                while(running) {
                    System.out.println(1);
                    try {
                        System.out.println("Listening...");
                        SSLSocket client = (SSLSocket) ssocket.accept();
                        System.out.println("connected.");
                        if(clientConneections.size() < clientPoolSize) {
                            System.out.println("Connection accepted");
                            ClientConnection clientConnection = new ClientConnection(client);
                            System.out.println(1);
                        }
                    }
                    catch(SocketException se) {
                        System.out.println("Server socket closed.");
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        acceptConnections.setDaemon(true);
        acceptConnections.start();

        //While server is running, process every message added to the queue
        Thread processMessages = new Thread() {
            @Override
            public void run() {
                System.out.println(2);
                while(running) {
                    try {
                        Message m = messageQueue.take();
                        chatLog.append(String.format(
                                "[%s] %s: %s\n",
                                m.getTimestamp().toString(),
                                m.getScreenName(),
                                m.getMessage()
                        ));
                        for(ClientConnection conn : clientConneections) {
                            conn.send(m);
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        processMessages.setDaemon(true);
        processMessages.start();



        JPanel rootPane = new JPanel(new BorderLayout());

        JPanel logPane = new JPanel(new BorderLayout());
        JPanel connectionsPane = new JPanel(new BorderLayout());

        JButton closeButton = new JButton("Close Server");
        closeButton.addActionListener(evt -> {
            close();
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        chatLog = new JTextArea();
        chatLog.setEditable(false);
        JScrollPane scrollingChatLog = new JScrollPane(chatLog);
        scrollingChatLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        logPane.add(closeButton, BorderLayout.NORTH);
        logPane.add(scrollingChatLog, BorderLayout.CENTER);

        connectionList = new JList(clientConneections.toArray());
        JButton kickButton = new JButton("Kick");
        kickButton.addActionListener(evt -> {
            ((ClientConnection) connectionList.getSelectedValue()).disconnect();
            connectionList.setListData(clientConneections.toArray());
        });

        connectionsPane.add(connectionList, BorderLayout.CENTER);
        connectionsPane.add(kickButton, BorderLayout.SOUTH);

        rootPane.add(logPane, BorderLayout.CENTER);
        rootPane.add(connectionsPane, BorderLayout.EAST);

        add(rootPane);
        setSize(1000, 600);
        setVisible(true);
    }

    public void close() {
        try {
            running = false;
            for(ClientConnection conn : clientConneections) {
                conn.disconnect();
            }
            ssocket.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String passwordHash(String password) throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();
        MessageDigest hashFunction = MessageDigest.getInstance("SHA-256");
        return encoder.encodeToString(hashFunction.digest(password.getBytes()));
    }

    private class ClientConnection implements Runnable {

        private SSLSocket client;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private boolean authenticated = false;
        private String screenName;
        private String hash;

        @Override
        public void run() {
            while(authenticated && running) {
                try {
                    Message received = (Message) in.readObject();
                    Message processed = new Message(
                            toString(),
                            received.getMessage(),
                            LocalTime.now()
                    );
                    messageQueue.put(processed);
                }
                catch(SocketException | EOFException eof) {
                    disconnect();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public ClientConnection(SSLSocket client) throws Exception {
            this.client = client;
            this.in = new ObjectInputStream(client.getInputStream());
            this.out = new ObjectOutputStream(client.getOutputStream());
            Message m = (Message)in.readObject();
            String[] connectionParameters = m.getMessage().split("/#/");
            if(connectionParameters.length == 3 && SERVER_PASSWORD.equals(connectionParameters[0])) {
                authenticated = true;
                clientConneections.put(this);
                connectionPool.execute(this);
                connectionList.setListData(clientConneections.toArray());
                screenName = connectionParameters[1];
                hash = passwordHash(connectionParameters[2]);
                send(new Message("SERVER", "Authentication successful.", LocalTime.now()));
            }
            else {
                out.writeObject(new Message("SERVER", "Authentication failed.  Disconnecting.", LocalTime.now()));
                disconnect();
            }
            chatLog.append(this.toString());
        }

        public void send(Message message) {
            try {
                out.writeObject(message);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        public String getHash() {
            return hash;
        }

        public void disconnect() {
            try {
                client.close();
                clientConneections.remove(this);
                connectionPool.remove(this);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", screenName, hash);
        }

    }
}
