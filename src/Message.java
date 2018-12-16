import java.io.Serializable;
import java.time.LocalTime;

public class Message implements Serializable {

    private String screenName;
    private String hash;
    private String message;
    private LocalTime timestamp;

    public Message(String screenName, String hash, String message, LocalTime timestamp) {
        this.screenName = screenName;
        this.hash = hash;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getHash() {
        return hash;
    }

    public String getMessage() {
        return message;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }
}
