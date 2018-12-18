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
    private String hmac; //hmac of the message

    /**
     * Message constructor.
     * This sets the screen name, message, and time of each message.
     * @param screenName is the screen name of the client sending a message.
     * @param message is the message that client is sending over the server.
     * @param timestamp is the time of the message being sent.
     */
    public Message(String screenName, String message, String hmac, LocalTime timestamp) {
        this.screenName = screenName;
        this.message = message;
        this.hmac = hmac;
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
     * Getter for the hmac.
     * @return the hmac the client is sending.
     */
    public String getHmac() {
        return hmac;
    }
}
