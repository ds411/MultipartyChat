import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {

    boolean running = true;
    private final String SERVER_PASSWORD;

    SSLServerSocket ssocket;
    LinkedBlockingQueue<Message> messageQueue;
    ArrayBlockingQueue<ClientConnection> clientConnecctions;
    ThreadPoolExecutor connectionPool;

    public Server(int port, int clientPoolSize, String password) throws Exception {
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
                        SSLSocket client = (SSLSocket) ssocket.accept();
                        System.out.println("connected.");
                        ClientConnection clientConnection = new ClientConnection(client);
                        clientConnecctions.put(clientConnection);
                        connectionPool.execute(clientConnection);
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
                            conn.send(new Message(received.getScreenName(), passwordHash(received.getHash()), received.getMessage(), LocalTime.now()));
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

        @Override
        public void run() {
            while(true) {
                try {
                    Message m = (Message)in.readObject();
                    if(!authenticated) {
                        if(SERVER_PASSWORD.equals(m.getMessage())) {
                            authenticated = true;
                        }
                        else {
                            out.writeObject(new Message("SERVER", "", "You have been disconnected.", LocalTime.now()));
                            in.close();
                            out.close();
                            client.close();
                            clientConnecctions.remove(this);
                            connectionPool.remove(this);
                        }
                    }
                    else
                        messageQueue.put(m);
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
        }

        public boolean send(Message message) {
            try {
                out.writeObject(message);
                return true;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }

    }
}
