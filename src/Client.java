import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Client {

    SSLSocket socket;

    public Client(String ip, int port) throws Exception {
        //Load keystore from server certificate
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream in = new FileInputStream("cert.pem");
        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
        keyStore.load(null);
        keyStore.setCertificateEntry("cert", cert);
        in.close();

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
    }
}
