import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class EncryptedChatApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        MainMenuScene mainMenu = new MainMenuScene();
        primaryStage.setTitle("Encrypted Chat Application");
        primaryStage.setScene(mainMenu);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

    }

}
