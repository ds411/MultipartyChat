import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private ArrayBlockingQueue<ClientConnection> clientConnecctions;
    private ThreadPoolExecutor connectionPool;

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

        clientConnecctions = new ArrayBlockingQueue<>(clientPoolSize);
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
                        ClientConnection clientConnection = new ClientConnection(client);
                        System.out.println(1);
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
                        Message received = messageQueue.take();
                        for(ClientConnection conn : clientConnecctions) {
                            conn.send(new Message(received.getScreenName() + " (" + conn.getHash() + ")", received.getMessage(), LocalTime.now()));
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

        JTextArea chatLog = new JTextArea();
        chatLog.setEditable(false);
        JScrollPane scrollingChatLog = new JScrollPane(chatLog);

        logPane.add(closeButton, BorderLayout.NORTH);
        logPane.add(scrollingChatLog, BorderLayout.CENTER);

        JList connectionList = new JList(clientConnecctions.toArray());
        JButton kickButton = new JButton("Kick");
        kickButton.addActionListener(evt -> {
            ((ClientConnection) connectionList.getSelectedValue()).disconnect();
        });

        connectionsPane.add(connectionList, BorderLayout.CENTER);
        connectionsPane.add(kickButton, BorderLayout.SOUTH);

        rootPane.add(logPane, BorderLayout.CENTER);
        rootPane.add(connectionsPane, BorderLayout.EAST);

        add(rootPane);
        setSize(1000, 600);
        setVisible(true);

        chatLog.append("1");
        chatLog.append("1");
        chatLog.append("1");
        chatLog.append("1");
    }

    public void close() {
        try {
            running = false;
            for(ClientConnection conn : clientConnecctions) {
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
        private String hash;

        @Override
        public void run() {
            while(authenticated) {
                try {
                    messageQueue.put((Message)in.readObject());
                }
                catch(EOFException eof) {
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
            if(SERVER_PASSWORD.equals(m.getMessage())) {
                authenticated = true;
                clientConnecctions.put(this);
                connectionPool.execute(this);
                send(new Message("SERVER", "Authentication successful.", LocalTime.now()));
                hash = passwordHash(((Message) in.readObject()).getMessage());
            }
            else {
                out.writeObject(new Message("SERVER", "Authentication failed.  Disconnecting.", LocalTime.now()));
                disconnect();
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

        public String getHash() {
            return hash;
        }

        public void disconnect() {
            try {
                client.close();
                clientConnecctions.remove(this);
                connectionPool.remove(this);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

    }
}
