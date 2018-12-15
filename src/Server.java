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
import java.util.concurrent.LinkedBlockingQueue;

public class Server {

    boolean running = true;

    SSLServerSocket ssocket;
    LinkedBlockingQueue<Message> messageQueue;
    ArrayList<ClientConnection> clientConnecctions;

    public Server(int port, int clientPoolSize) throws Exception {
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

        clientConnecctions = new ArrayList();

        //While server is running, accept clients and run new threads
        Thread acceptConnections = new Thread() {
            @Override
            public void run() {
                while(running) {
                    try {
                        SSLSocket client = (SSLSocket) ssocket.accept();
                        ClientConnection clientConnection = new ClientConnection(client);
                        clientConnecctions.add(clientConnection);
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
                messageQueue = new LinkedBlockingQueue<>();
                while(running) {
                    try {
                        Message received = messageQueue.take();
                        for(ClientConnection conn : clientConnecctions) {
                            conn.send(new Message(received.getScreenName(), passwordHash(received.getPassword()), received.getMessage(), LocalTime.now()));
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

    private class ClientConnection {

        private SSLSocket client;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientConnection(SSLSocket client) throws Exception {
            this.client = client;
            this.in = new ObjectInputStream(client.getInputStream());
            this.out = new ObjectOutputStream(client.getOutputStream());

            Thread receive = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            messageQueue.put((Message) in.readObject());
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            receive.setDaemon(true);
            receive.start();
        }

        public void send(Message message) {
            try {
                out.writeObject(message);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

    }
}
