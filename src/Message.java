import java.io.Serializable;
import java.time.LocalTime;

public class Message implements Serializable {

    private String screenName;
    private String message;
    private LocalTime timestamp;

    public Message(String screenName, String message, LocalTime timestamp) {
        this.screenName = screenName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getMessage() {
        return message;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp.toString(), screenName, message);
    }
}
