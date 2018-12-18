import java.io.Serializable;
import java.time.LocalTime;

/**
 * Message Class.
 * This class allows for the correct format of all messages.
 */
public class Message implements Serializable {

    private String screenName;  //screen name for the client
    private String message; //message that the client is sending
    private LocalTime timestamp;    //time stamp of when the message was sent

    /**
     * Message constructor.
     * This sets the screen name, message, and time of each message.
     * @param screenName is the screen name of the client sending a message.
     * @param message is the message that client is sending over the server.
     * @param timestamp is the time of the message being sent.
     */
    public Message(String screenName, String message, LocalTime timestamp) {
        this.screenName = screenName;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Getter for screen name.
     * @return the screen name of the client.
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * Getter for the message.
     * @return the message the client is sending.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getter for the time stamp.
     * @return the time stamp of the message being sent.
     */
    public LocalTime getTimestamp() {
        return timestamp;
    }

    /**
     * toString override.
     * Constructs the proper format for each message based on the name, timestamp and message.
     * @return the string of the message format.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp.toString(), screenName, message);
    }
}
