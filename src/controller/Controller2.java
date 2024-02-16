package controller;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import logic.Logic;
import util.Letter;

public class Controller2 {

    @FXML private ProgressBar progressBar;
    @FXML private VBox content;
    @FXML private VBox loadingScreen;
    @FXML private TilePane tileBoard;
    
    public void calculateBestSwaps(boolean isDeluxe, boolean isArchive, int gameNumber) throws FileNotFoundException {
        Letter[][] board;
        if (isDeluxe) {
            board = new Letter[7][7];
        } else {
            board = new Letter[5][5];
        }
        ArrayList<String> allPossibleWords = Logic.getWebsiteDataAndSetupBoard(isArchive, isDeluxe, gameNumber, board);
        // TODO make progress bar work as it should
        makeBoard(board);
        // char[] endState = Logic.getFinishedState(allPossibleWords, board);
        // ArrayList<ArrayList<Integer>> switches = Logic.getSwitches(endState, board);

        // Logic.showSwitches(switches, board);
    }

    private void makeBoard(Letter[][] board) {
        loadingScreen.setVisible(false);
        content.setVisible(true);
        tileBoard.setPrefRows(board.length);
        tileBoard.setPrefColumns(board.length);
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                tileBoard.getChildren().add(new Text(board[i][j] == null ? " " : board[i][j].getLetter() + ""));
            }
        }
    }

    private static void showSwitches(ArrayList<ArrayList<Integer>> switches, Letter[][] board) {
        for (ArrayList<Integer> cycle : switches) {
            Letter[] letters = new Letter[2];
            for (int letterIndex : cycle) {
                if (letters[1] != null) {
                    letters[0] = letters[1];
                }
                int counter = 0;
                outerLoop:
                for (int i = 0; i < board.length; i++) {
                    for (int j = 0; j < board[i].length; j++) {
                        if (counter == letterIndex) {
                            letters[1] = board[i][j];
                            break outerLoop;
                        }
                        counter++;
                    }
                }
                letters[1].setState(3);
                if (letters[0] == null) {
                    letters[0] = letters[1];
                    continue;
                }
                // printBoard(board);
                letters[0].setState(2);
                char tempLetter = letters[1].getLetter();
                letters[1].setLetter(letters[0].getLetter());
                letters[0].setLetter(tempLetter);
            }
            letters[1].setState(2);
        }
        // printBoard(board);
    }



    // private static void printBoard(Letter[][] board) {
    //     for (int i = 0; i < board.length; i++) {
    //         for (int j = 0; j < board[i].length; j++) {
    //             if (i % 2 == 1 && j % 2 == 1) {
    //                 System.out.print(" |");
    //                 continue;
    //             }
    //             switch (board[i][j].getState()) {
    //                 case 0:
    //                     System.out.print(WHITE_BG + board[i][j].getLetter() + RESET);
    //                     break;
    //                 case 1:
    //                     System.out.print(YELLOW_BG + board[i][j].getLetter() + RESET);
    //                     break;
    //                 case 2:
    //                     System.out.print(GREEN_BG + board[i][j].getLetter() + RESET);
    //                     break;
    //                 case 3:
    //                     System.out.print(PURPLE_BG + board[i][j].getLetter() + RESET);
    //                     break;
    //                 default:
    //                     System.out.print(board[i][j].getLetter());
    //                     break;
    //             }
    //             if (j + 1 != board[i].length) {
    //                 System.out.print("|");
    //             }
    //         }
    //         if (i + 1 != board[i].length) {
    //             System.out.println("\n" + "-+".repeat(board.length - 1) + "-");
    //         }
    //     }
    //     System.out.println("\n");
    // }
}
