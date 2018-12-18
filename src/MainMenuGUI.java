import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;

/**
 * MainMenuGUI class.
 * This creates the GUI for the main menu.
 */
public class MainMenuGUI extends JFrame {

    /**
     * Constructor for MainMenuGUI.
     * This creates the menu objects and sets the Title.
     */
    public MainMenuGUI() {
        super("Chat Application"); //set the title of the GUI

        setLayout(new FlowLayout());    //set the layout of the GUI
        JPanel mainForm = new JPanel(); //create main panel

        JButton serverBtn = new JButton("Launch Server");   //button for selecting server
        serverBtn.setFocusPainted(false);
        /**
         * Action listener for the serverBtn.
         * This creates the server input fields.
         * Allows connection as a server.
         */
        serverBtn.addActionListener(evt -> {
            JTextField portField = new JTextField();    //create field for port num
            JTextField passwordField = new JTextField();    //create field for password
            JTextField maxSize = new JTextField();  //create field for size of room
            //Load fields into object array
            Object[] fields = {
                    "Server Port: ", portField,
                    "Server Password: ", passwordField,
                    "Max Size: ", maxSize,
            };

            //set option to check if user connects or cancels
            int option = JOptionPane.showConfirmDialog(null, fields, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
            //if option selected is not cancel
            if(option == JOptionPane.OK_OPTION) {
                //try to create a new server
                try {
                    //create server with the field inputs
                    Server server = new Server(
                            Integer.parseInt(portField.getText()),
                            Integer.parseInt(maxSize.getText()),
                            passwordField.getText()
                    );
                }
                //catch an exception and print it
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });


        JButton clientBtn = new JButton("Launch Client"); //button for selecting client
        clientBtn.setFocusPainted(false);
        /**
         * Action listener for clientBtn.
         * Creates the client input fields.
         * Allows connection as a client.
         */
        clientBtn.addActionListener(evt -> {
            JTextField ipField = new JTextField();  //create field for IP
            JTextField portField = new JTextField();    //create field for prot num
            JTextField passwordField = new JTextField();    //create field for password
            JTextField screenNameField = new JTextField();  //create field for screen name
            JTextField hashField = new JTextField();    //create field for hash
            //load fields into object array
            Object[] fields = {
                    "IP: ", ipField,
                    "Port: ", portField,
                    "Server Password: ", passwordField,
                    "Screen Name: ", screenNameField,
                    "Hash String: ", hashField
            };

            //set option to check if user connects or cancels
            int option = JOptionPane.showConfirmDialog(null, fields, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
            //if option selected is not cancel
            if(option == JOptionPane.OK_OPTION) {
                //try to create a new client
                try {
                    //create new client with the input fields
                    Client client = new Client(
                            ipField.getText(),
                            Integer.parseInt(portField.getText()),
                            passwordField.getText(),
                            screenNameField.getText(),
                            hashField.getText()
                    );
                }
                //catch an exception and print it
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mainForm.add(serverBtn);    //add the serverBtn to the JPanel
        mainForm.add(clientBtn);    //add the clientBtn to the JPanel

        add(mainForm);  //add the mainForm panel to the MainMenu

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //set the program to stop if you close

    }
}
