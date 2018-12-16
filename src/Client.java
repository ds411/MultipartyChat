import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalTime;

public class Client {

    private SSLSocket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String screenName;
    private String hash;
    private boolean authenticated = false;

    public Client(String ip, int port, String password, String screenName, String hash) throws Exception {
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
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        processMessages.setDaemon(true);
        processMessages.start();
    }

    private void authenticate(String password) throws Exception {
        out.writeObject(new Message(screenName, password, LocalTime.now()));
        Message authenticationResponse = (Message)in.readObject();
        if(authenticationResponse.getMessage().equals("Authentication failed.  Disconnecting.")) {
            socket.close();
        }
        else {
            authenticated = true;
            out.writeObject(new Message(screenName, hash, LocalTime.now()));
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

    }
}
