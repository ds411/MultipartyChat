import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {

    SSLSocket socket;

    public Client(String ip, int port) throws Exception {

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        socket = (SSLSocket) socketFactory.createSocket(ip, port);
    }
}
