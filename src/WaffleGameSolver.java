import java.io.FileNotFoundException;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WaffleGameSolver extends Application {

    public static void main(String[] args) throws FileNotFoundException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(new FXMLLoader(getClass().getResource("view/scene1.fxml")).load());
        stage.setTitle("Waffle game solver");
        stage.setScene(scene);
        stage.show();
    }
}
