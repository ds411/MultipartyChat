import java.io.Serializable;

public class Message implements Serializable {

    String screenName;
    String password;
    String message;

    public Message(String screenName, String password, String message) {
        this.screenName = screenName;
        this.password = password;
        this.message = message;
    }
}
