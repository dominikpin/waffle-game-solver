package hampter.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import hampter.WaffleGameSolver;
import hampter.logic.Logic;
import hampter.util.Letter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Controller2 {

    @FXML private Text loadingText;
    @FXML private VBox content;
    @FXML private TilePane tileBoard;
    private static ArrayList<Letter[][]> boards = new ArrayList<>();
    private static int boardNumber = 0;
    private static boolean isDelu;
    private static boolean isArch;
    private static int gameNum;
    
    public void calculateBestSwaps(boolean isDeluxe, boolean isArchive, int gameNumber) throws FileNotFoundException {
        isDelu = isDeluxe;
        isArch = isArchive;
        gameNum = gameNumber;
        boardNumber = 0;
        boards.clear();
        Letter[][] board;
        if (isDeluxe) {
            board = new Letter[7][7];
        } else {
            board = new Letter[5][5];
        }
        tileBoard.setPrefRows(board.length);
        tileBoard.setPrefColumns(board.length);
        new Thread(() -> {
            ArrayList<String> allPossibleWords = Logic.getWebsiteDataAndSetupBoard(isArchive, isDeluxe, gameNumber, board);
            if (allPossibleWords == null) {
                Platform.runLater(() -> nextSolve());
            } else {
                boards.add(Logic.copyBoard(board));
                Platform.runLater(() -> {
                    loadingText.setVisible(false);
                    content.setVisible(true);
                    makeBoard();
                });
                char[] endState = Logic.getFinishedState(allPossibleWords, board);
                ArrayList<ArrayList<Integer>> switches = Logic.getSwitches(endState, board);
                Logic.showSwitches(switches, board, boards);
            }
            Thread.currentThread().interrupt();
        }).start();
    }

    public void switchBoard(ActionEvent event) {
        if (((Button)event.getSource()).getText().equals("Previous")) {
            boardNumber -= boardNumber == 0 ? 0 : 1;
        } else {
            boardNumber += boardNumber == boards.size() - 1 ? 0 : 1;
        }
        makeBoard();
    }

    public void nextSolve() {
        Stage stage = (Stage)content.getScene().getWindow();
        FXMLLoader loader = WaffleGameSolver.getFXMLLoader("view/scene1");
        try {
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Controller1 controller1 = loader.getController();
        controller1.setToggleGroupsAndSpinnerValue(isDelu, isArch, gameNum);
    }

    private void makeBoard() {
        Letter[][] board = boards.get(boardNumber);
        tileBoard.getChildren().clear();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                StackPane pane = new StackPane();
                Rectangle rectangle = new Rectangle(50, 50, board[i][j] == null ? null : board[i][j].getState() == 3 ? Color.PURPLE :board[i][j].getState() == 2 ? Color.GREEN : board[i][j].getState() == 1 ? Color.YELLOW : Color.GRAY);
                rectangle.setArcHeight(10);
                rectangle.setArcWidth(10);
                Text text = new Text(board[i][j] == null ? " " : board[i][j].getLetter() + "");
                text.setFont(new Font(30));
                pane.getChildren().addAll(rectangle, text);
                tileBoard.getChildren().add(pane);
            }
        }
    }
}
