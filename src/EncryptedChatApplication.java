/**
 * Main class for the Chat Application
 * This is an application that allows a client to connect to a server
 * and for them to send encrypted messages over the server
 * This opens a MainMenu and allows a user to select launch client or server
 * @author Dan, Mike, Mark
 */
public class EncryptedChatApplication {

    /**
     * Main to allow the MainMenu GUI to display
     * @param args A string array containing
     * the command line arguments.
     */
    public static void main(String[] args) {
        MainMenuGUI gui = new MainMenuGUI();    //create main menu

        //set the size and allow it to be visible
        gui.setSize(800, 600);
        gui.setVisible(true);
    }

}
