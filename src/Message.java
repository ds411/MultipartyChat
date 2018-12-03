import java.io.Serializable;
import java.time.LocalTime;

public class Message implements Serializable {

    private String screenName;
    private String password;
    private String message;
    private LocalTime timestamp;

    public Message(String screenName, String password, String message, LocalTime timestamp) {
        this.screenName = screenName;
        this.password = password;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getPassword() {
        return password;
    }

    public String getMessage() {
        return message;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }
}
