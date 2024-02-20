package hampter;

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
        Scene scene = new Scene(getFXMLLoader("view/scene1").load());
        stage.setTitle("Waffle game solver");
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.show();
    }

    public static FXMLLoader getFXMLLoader(String fxml) {
        FXMLLoader fxmlLoader = new FXMLLoader(WaffleGameSolver.class.getResource(fxml + ".fxml"));
        return fxmlLoader;
    }
}
