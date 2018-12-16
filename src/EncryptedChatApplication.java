import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class EncryptedChatApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        /*Scene scene = new Scene(new MainMenuGUI(),800, 600);
        primaryStage.setTitle("Encrypted Chat Application");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show(); */

        MainMenuGUI gui = new MainMenuGUI();
        gui.setSize(800, 600);
        gui.setVisible(true);
        /*
        ClientGUI guiC = new ClientGUI();
        guiC.setSize(800, 600);
        guiC.setVisible(true); */


    }

}
