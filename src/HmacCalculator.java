import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class HmacCalculator extends JFrame {

    public HmacCalculator() {
        super("HMAC Calculator");

        Base64.Encoder encoder = Base64.getEncoder();

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        JTextField messageField = new JTextField(50);
        JTextField keyField = new JTextField(50);
        JTextField resultField = new JTextField(50);
        resultField.setEditable(false);
        JButton calcButton = new JButton("Calculate HMAC");
        calcButton.addActionListener(evt -> {
            try {
                String message = messageField.getText();
                String keyString = keyField.getText();
                SecretKeySpec keySpec = new SecretKeySpec(keyString.getBytes(), "HmacSHA256");
                Mac hmac = Mac.getInstance("HmacSHA256");
                hmac.init(keySpec);
                byte[] macBytes = hmac.doFinal(message.getBytes());
                String result = encoder.encodeToString(macBytes);
                resultField.setText(result);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        });

        root.add(messageField);
        root.add(keyField);
        root.add(calcButton);
        root.add(resultField);

        add(root);
        setSize(400, 400);
        setVisible(true);
    }
}
