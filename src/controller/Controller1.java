package controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class Controller1 {
    
    @FXML private ToggleGroup group1;
    @FXML private ToggleGroup group2;
    @FXML private Spinner<Integer> spinner;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
        spinner.setValueFactory(valueFactory);
        spinner.setOnScroll(event -> {
            if (event.getDeltaY() < 0) {
                spinner.decrement();
            } else if (event.getDeltaY() > 0) {
                spinner.increment();
            }
        });
    }

    public void makeVisible() {
        if (((RadioButton)group2.getSelectedToggle()).getText().equals("ARCHIVE")) {
            spinner.setVisible(true);
            spinner.setManaged(true);
        } else {
            spinner.setVisible(false);
            spinner.setManaged(false);
        }
    }

    public void startGame() throws IOException {
        Stage stage = (Stage)spinner.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/scene2.fxml"));
        stage.setScene(new Scene(loader.load()));
        Controller2 controller2 = loader.getController();
        controller2.calculateBestSwaps(((RadioButton)group1.getSelectedToggle()).getText().equals("DELUXE"), ((RadioButton)group2.getSelectedToggle()).getText().equals("ARCHIVE"), spinner.getValue());
    }

    public void exit() {
        Platform.exit();
    }
}
