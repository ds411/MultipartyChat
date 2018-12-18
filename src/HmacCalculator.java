import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.util.Base64;

/**
 * HMAC Calculate Class.
 * This Class creates an HMAC calculator to compute the HMAC
 * based on a key of a string.
 */
public class HmacCalculator extends JFrame {

    /**
     * HMAC caclulator constructor.
     * This creates a GUI for calculating the HMAC of a string.
     */
    public HmacCalculator() {
        super("HMAC Calculator");   //set the title of the GUI

        Base64.Encoder encoder = Base64.getEncoder();   //set the encoder to base 64

        JPanel root = new JPanel(); //panel for the gui
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));  //layout of the panel
        JTextField messageField = new JTextField(50);   //field for the message
        JTextField keyField = new JTextField(50);   //field for the key
        JTextField resultField = new JTextField(50);    //result of the hash field
        resultField.setEditable(false); //dont allow the result to be edited

        JButton calcButton = new JButton("Calculate HMAC"); //button to calculate the hmac
        /**
         * Action listener for the calculate btn.
         * If clicked, the message and key are taken to hash a message
         * using HMAC SHA256 and returns the hash to the result
         */
        calcButton.addActionListener(evt -> {
            try {
                //try to get the message and key inputs
                String message = messageField.getText();
                String keyString = keyField.getText();
                //set the key spec using the input key
                SecretKeySpec keySpec = new SecretKeySpec(keyString.getBytes(), "HmacSHA256");
                //set the hmac to hmac sha256
                Mac hmac = Mac.getInstance("HmacSHA256");
                hmac.init(keySpec); //initialize the hmac based on the key spec

                //get the bytes of the mac
                byte[] macBytes = hmac.doFinal(message.getBytes());

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
        root.add(messageField);
        root.add(keyField);
        root.add(calcButton);
        root.add(resultField);

        //add the panel to the gui frame
        add(root);

        //set the size and make the gui visible
        setSize(400, 400);
        setVisible(true);
    }
}
