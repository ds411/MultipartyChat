import java.awt.*;
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

        JPanel mainForm = new JPanel(); //create main panel
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));    //set the layout of the GUI);

        JButton serverBtn = new JButton("Launch Server");   //button for selecting server
        serverBtn.setPreferredSize(new Dimension(400, 190));
        JPanel serverBtnPane = new JPanel();
        serverBtnPane.add(serverBtn);
        serverBtn.setFocusPainted(false);
        /**
         * Action listener for the serverBtn.
         * This creates the server input fields.
         * Allows connection as a server.
         */
        serverBtn.addActionListener(evt -> {
            JSpinner portField = new JSpinner(new SpinnerNumberModel(8000, 8000, 8099, 1));     //create field for port num
            JTextField passwordField = new JTextField();    //create field for password
            JSpinner maxSize = new JSpinner(new SpinnerNumberModel(2, 2, 8, 1));  //create field for size of room
            //Load fields into object array
            Object[] fields = {
                    "Server Port: ", portField,
                    "Server Password: ", passwordField,
                    "Max Size: ", maxSize,
            };

            //set option to check if user connects or cancels
            int option = JOptionPane.showConfirmDialog(null, fields, "Connect to Server", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            //if option selected is not cancel
            if(option == JOptionPane.OK_OPTION) {
                //try to create a new server
                try {
                    //create server with the field inputs
                    Server server = new Server(
                            (int)portField.getValue(),
                            (int)maxSize.getValue(),
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
        clientBtn.setPreferredSize(new Dimension(400, 190));
        JPanel clientBtnPane = new JPanel();
        clientBtnPane.add(clientBtn);
        clientBtn.setFocusPainted(false);
        /**
         * Action listener for clientBtn.
         * Creates the client input fields.
         * Allows connection as a client.
         */
        clientBtn.addActionListener(evt -> {
            JTextField ipField = new JTextField();  //create field for IP
            JSpinner portField = new JSpinner(new SpinnerNumberModel(8000, 8000, 8099, 1));   //create field for prot num
            JTextField passwordField = new JTextField();    //create field for password
            JTextField screenNameField = new JTextField();  //create field for screen name
            JTextField predigestField = new JTextField();    //create field for predigest
            //load fields into object array
            Object[] fields = {
                    "IP: ", ipField,
                    "Port: ", portField,
                    "Server Password: ", passwordField,
                    "Screen Name: ", screenNameField,
                    "Predigest: ", predigestField
            };

            //set option to check if user connects or cancels
            int option = JOptionPane.showConfirmDialog(null, fields, "Connect to Server", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            //if option selected is not cancel
            if(option == JOptionPane.OK_OPTION) {
                //try to create a new client
                try {
                    //create new client with the input fields
                    Client client = new Client(
                            ipField.getText(),
                            (int)portField.getValue(),
                            passwordField.getText(),
                            screenNameField.getText(),
                            predigestField.getText()
                    );
                }
                //catch an exception and print it
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        JButton calcBtn = new JButton("Open Hash Calculator"); //button for hmac calculator
        //setting size of button
        calcBtn.setPreferredSize(new Dimension(400, 190));
        JPanel calcBtnPane = new JPanel();
        calcBtnPane.add(calcBtn);
        /**
         * Action listener for calcBtn.
         * Launches the Hash Calculator.
         */
        calcBtn.addActionListener(evt -> {
            HashCalculator hmacCalculator = new HashCalculator();
        });

        mainForm.add(serverBtnPane);    //add the serverBtn to the JPanel
        mainForm.add(clientBtnPane);    //add the clientBtn to the JPanel
        mainForm.add(calcBtnPane);      //add the calcBtn to the JPanel

        add(mainForm);  //add the mainForm panel to the MainMenu

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //set the program to stop if you close

        //set the size and allow it to be visible
        setSize(400, 600);
        setVisible(true);
    }
}
