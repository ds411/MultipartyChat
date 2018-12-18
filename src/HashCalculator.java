import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Hash Calculate Class.
 * This Class creates a hash calculator to compute the hash
 * based on a key of a string.
 */
public class HashCalculator extends JFrame {

    /**
     * Hash caclulator constructor.
     * This creates a GUI for calculating the SHA256 hash of a string.
     */
    public HashCalculator() {
        super("Hash Calculator");   //set the title of the GUI

        Base64.Encoder encoder = Base64.getEncoder();   //set the encoder to base 64

        JPanel root = new JPanel(); //panel for the gui
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));  //layout of the panel
        JLabel predigestLabel = new JLabel("Predigest:"); //label for key
        JTextField predigestField = new JTextField(50);   //field for the key
        JLabel resultLabel = new JLabel("Result:");
        JTextField resultField = new JTextField(50);    //result of the hash field
        resultField.setEditable(false); //dont allow the result to be edited

        JButton calcButton = new JButton("Calculate Hash"); //button to calculate the hmac
        calcButton.setPreferredSize(new Dimension(400, 60));
        JPanel calcButtonPane = new JPanel();
        calcButtonPane.add(calcButton);
        /**
         * Action listener for the calculate btn.
         * If clicked, the predigest is used to hash a message
         * using SHA256 and returns the hash to the result
         */
        calcButton.addActionListener(evt -> {
            try {
                //try to get the message and key inputs
                String predigest = predigestField.getText();
                //initialize the hashing function
                MessageDigest hashFunction = MessageDigest.getInstance("SHA-256");

                //get the bytes of the mac
                byte[] macBytes = hashFunction.digest(predigest.getBytes());

                //set the result to the hmac byes and set the result field to the result string
                String result = encoder.encodeToString(macBytes);
                resultField.setText(result);
            }
            //catch exception and print
            catch(Exception e) {
                e.printStackTrace();
            }
        });

        //add the fields and btns to the panel
        root.add(predigestLabel);
        root.add(predigestField);
        root.add(calcButtonPane);
        root.add(resultLabel);
        root.add(resultField);

        //add the panel to the gui frame
        add(root);

        //set the size and make the gui visible
        setSize(400, 400);
        setVisible(true);
    }
}
