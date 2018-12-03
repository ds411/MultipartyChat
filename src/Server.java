import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class Server {

    SSLServerSocket ssocket;

    public Server(int port) throws Exception {
        //Load keystore from server certificate
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream in = new FileInputStream("cert.jks");
        keyStore.load(in, "projectCertificate".toCharArray());
        in.close();

        //Initialize key manager factory from keystore
        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance("X509");
        kmFactory.init(keyStore, "projectCertificate".toCharArray());

        //Initialize ssl context from key manager factory
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmFactory.getKeyManagers(), null, null);

        //Get ssl server socket factory from ssl context
        SSLServerSocketFactory ssocketFactory = sslContext.getServerSocketFactory();

        //Create ssl server socket from ssl server socket factory
        ssocket = (SSLServerSocket) ssocketFactory.createServerSocket(port);
    }
}
