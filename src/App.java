import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import util.Letter;

public class App {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN_BG = "\u001B[42m";
    private static final String YELLOW_BG = "\u001B[43m";
    private static final String WHITE_BG = "\u001B[47m";
    private static final String PURPLE_BG = "\u001B[45m";
    private static final int game = 1;
    private static final int archiveGameNumber = 100;
    private static int correctGameCounter = 0;
    private static Letter[][] board;

    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<String> allPossibleWords = getWebsiteDataAndSetupBoard();
        char[] endState = getFinishedState(allPossibleWords);
        if (correctGameCounter != 1) {
            System.exit(1);
        }
        ArrayList<ArrayList<Integer>> switches = getSwitches(endState);
        showSwitches(switches);
        System.exit(0);
    }

    private static ArrayList<String> getWebsiteDataAndSetupBoard() throws FileNotFoundException {
        ArrayList<String> allPossibleWords = new ArrayList<>();
        Elements elements = null;
        switch (game) {
            case 0:
                elements = getContentOfWebsite("https://wafflegame.net/daily", false, false,  -1);
                getAllWords("lib/words-5.txt", allPossibleWords);
                break;
            case 1:
                elements = getContentOfWebsite("https://wafflegame.net/archive", true, false, archiveGameNumber);
                getAllWords("lib/words-5.txt", allPossibleWords);
                break;
            case 2:
                elements = getContentOfWebsite("https://wafflegame.net/deluxe", false, true, -1);
                getAllWords("lib/words-7.txt", allPossibleWords);
                break;
            case 3:
                elements = getContentOfWebsite("https://wafflegame.net/archive", true, true, archiveGameNumber);
                getAllWords("lib/words-7.txt", allPossibleWords);
                break;
        }
        createBoard(elements);
        return allPossibleWords;
    }

    private static Elements getContentOfWebsite(String url, boolean isArchive, boolean isDeluxe, int index) {
        System.setProperty("webdriver.chrome.driver", "lib/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe");
        WebDriver driver = new ChromeDriver(options);
        driver.get(url);
        try {
            WebDriverWait wait1 = new WebDriverWait(driver, Duration.ofSeconds(2));
            wait1.until(ExpectedConditions.visibilityOfElementLocated(By.className("fc-consent-root")));
            driver.findElement(By.className("fc-button-label")).click();
        } catch (Exception e) {}
        if (isArchive) {
            if (isDeluxe) {
                driver.findElement(By.className("tab--deluxe")).click();
                driver.findElement(By.className("button--watch-ad")).click();
            }
            WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait2.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("item"), index));
            List<WebElement> elements = driver.findElements(By.className("item"));
            WebDriverWait wait3 = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait3.until(ExpectedConditions.elementToBeClickable(elements.get(index - 1)));
            elements.get(index - 1).click();
        }
        if (isDeluxe) {
            board = new Letter[7][7];
        } else {
            board = new Letter[5][5];
        }
        String html = driver.getPageSource();
        driver.close();
        return Jsoup.parse(html).getElementsByClass("tile");
    }

    private static void getAllWords(String file, ArrayList<String> allPossibleWords) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(file));
        while (scanner.hasNextLine()) {
            allPossibleWords.add(scanner.nextLine());
        }
        scanner.close();
    }

    private static void createBoard(Elements elements) {
        int counter = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (i % 2 == 1 && j % 2 == 1) {
                    counter++;
                    continue;
                }
                int index = i * board.length + j - counter;
                char letter = elements.get(index).text().toLowerCase().charAt(0);
                int state = elements.get(index).hasClass("green") ? 2 : elements.get(index).hasClass("yellow") ? 1 : 0;
                board[i][j] = new Letter(letter, state);
            }
        }
    }

    private static char[] getFinishedState(ArrayList<String> allPossibleWords) {
        ArrayList<ArrayList<String>> words = getAllPossibleWords(allPossibleWords);
        char[] endState = new char[board.length * board.length];
        findSolutionRecursively(words, new boolean[words.size()], endState);
        return endState;
    }
    
    private static ArrayList<ArrayList<String>> getAllPossibleWords(ArrayList<String> allPossibleWords) {
        Map<Character, Integer> allLetters = evaluateLetters();
        ArrayList<ArrayList<String>> words = new ArrayList<>();
        for (int i = 0; i < board.length; i += 2) {
            words.add(getPossibleWords(i, true, new ArrayList<>(allPossibleWords), allLetters, true));
            words.add(getPossibleWords(i, false, new ArrayList<>(allPossibleWords), allLetters, true));
        }
        return words;
    }

    private static Map<Character, Integer> evaluateLetters() {
        Map<Character, Integer> allLetters = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (i % 2 == 1 && j % 2 == 1) {
                    continue;
                }
                Letter letter = board[i][j];
                int count = allLetters.containsKey(letter.getLetter()) ? allLetters.get(letter.getLetter()) : 0;
                if (letter.getState() != 2) {
                    allLetters.put(letter.getLetter(), count + 1);
                }
            }
        }
        return allLetters;
    }

    private static ArrayList<String> getPossibleWords(int start, boolean isHorizontal, ArrayList<String> possibleWords, Map<Character, Integer> allLetters, boolean isFirst) {
        Map<Character, Integer> possibleAmountOfLetters = new HashMap<>();
        if (isFirst) {
            for (int i = 0; i < board.length; i++) {
                Letter letter = isHorizontal ? board[start][i] : board[i][start];
                if (letter.getState() == 0) {
                    possibleAmountOfLetters.put(letter.getLetter(), 0);
                }
            }
            for (int i = 0; i < board.length; i++) {
                Letter letter = isHorizontal ? board[start][i] : board[i][start];
                if (letter.getState() != 0 && possibleAmountOfLetters.containsKey(letter.getLetter())) {
                    possibleAmountOfLetters.put(letter.getLetter(), possibleAmountOfLetters.get(letter.getLetter()) + 1);
                }
            }       
        }
        for (int i = 0; i < board.length; i++) {
            Letter letter = isHorizontal ? board[start][i] : board[i][start];
            for (int j = 0; j < possibleWords.size(); j++) {
                String possibleWord = possibleWords.get(j);
                if (isFirst && areAllLettersAccesseble(letter, possibleWord, i, allLetters) ||
                    isFirst && areTooManySameLetters(letter, possibleWord, i, possibleAmountOfLetters) ||
                    isCorrectLetterInCorrectSpotInWord(letter, possibleWord, i) ||
                    isWrongLetterInWrongSpot(letter, possibleWord, i) ||
                    isSemiCorrectLetterInWord(letter, possibleWord, i)
                    ) {
                        possibleWords.remove(j);
                        j--;
                }
            }
        }
        return possibleWords;
    }

    private static boolean areAllLettersAccesseble(Letter letter, String possibleWord, int position, Map<Character, Integer> allLetters) {
        return letter.getState() != 2 && !allLetters.containsKey(possibleWord.charAt(position));
    }

    private static boolean areTooManySameLetters(Letter letter, String possibleWord, int position, Map<Character, Integer> possibleAmountOfLetters) {
        if (!possibleAmountOfLetters.containsKey(letter.getLetter())) {
            return false;
        }
        return (possibleWord.length() - possibleWord.replaceAll(letter.getLetter() + "", "").length()) > possibleAmountOfLetters.get(letter.getLetter());
    }
    
    private static boolean isCorrectLetterInCorrectSpotInWord(Letter letter, String possibleWord, int position) {
        return letter.getState() == 2 && possibleWord.charAt(position) != letter.getLetter();
    }

    private static boolean isWrongLetterInWrongSpot(Letter letter, String possibleWord, int position) {
        return (letter.getState() == 0 || letter.getState() == 1) && possibleWord.charAt(position) == letter.getLetter();
    }

    private static boolean isSemiCorrectLetterInWord(Letter letter, String possibleWord, int position) {
        return position % 2 == 1 && letter.getState() == 1 && !possibleWord.contains(letter.getLetter() + "");
    }

    private static void findSolutionRecursively(ArrayList<ArrayList<String>> words, boolean[] colapsed, char[] endState) {
        // System.out.println(words);
        // for (int i = 0; i < colapsed.length; i++) {
        //     System.out.print(colapsed[i] + " ");
        // }
        // System.out.println();
        // printBoard();
        for (int i = 0; i < colapsed.length; i++) {
            if (!colapsed[i]) {
                break;
            }
            if (colapsed[i] && colapsed.length == i + 1) {
                correctGameCounter++;
                printBoard();
                copyBoard(endState);
                return;
            }
        }
        int shortestList = -1;
        int index = -1;
        for (int i = 0; i < colapsed.length; i++) {
            if ((index == -1 || shortestList > words.get(i).size()) && !colapsed[i]) {
                shortestList = words.get(i).size();
                index = i;
            }
        }
        if (shortestList == 0) {
            return;
        }
        colapsed[index] = true;
        for (String possibleWord : words.get(index)) {
            String returnValue = colapse(possibleWord, index);
            if (returnValue.charAt(0) != 'f') {
                ArrayList<ArrayList<String>> newWords = removeAllImpossibleWords(words);
                // System.out.println(possibleWord);
                // System.out.println(newWords);
                if (!newWords.stream().anyMatch(ArrayList::isEmpty)) {
                    findSolutionRecursively(newWords, colapsed, endState);
                }
            }
            decolapse(returnValue);
        }
        colapsed[index] = false;
    }

    private static String colapse(String word, int index) {
        String returnValue = "";
        boolean isHorizontal = index % 2 == 0;
        int start = index - index % 2;
        for (int i = 0; i < board.length; i++) {
            Letter letter = isHorizontal ? board[start][i] : board[i][start];
            if (letter.getState() == 2) {
                continue;
            }
            int y1 = isHorizontal ? start : i;
            int x1 = isHorizontal ? i : start;
            int y2 = -1, x2 = -1;
            outerLoop:
            for (int j = 0; j < board.length; j++) {
                for (int k = 0; k < board[j].length; k++) {
                    if (board[j][k] == null) {
                        continue;
                    }
                    Letter switchLetter = board[j][k];
                    if (switchLetter.getLetter() == word.charAt(i) && switchLetter.getState() != 2) {
                        y2 = j;
                        x2 = k;
                        break outerLoop;
                    }
                }
            }
            if (y2 == -1) {
                return 'f' + returnValue;
            }
            Letter switchedLetter = switchLetters(y1, x1, y2, x2);
            returnValue += String.format("%d%d%d%d%d%d", y1, x1, letter.getState(), y2, x2, switchedLetter.getState());
            letter.setState(3);
            switchedLetter.setState(2);
        }
        return returnValue;
    }

    private static ArrayList<ArrayList<String>> removeAllImpossibleWords(ArrayList<ArrayList<String>> words) {
        ArrayList<ArrayList<String>> newWords = new ArrayList<>();
        for (ArrayList<String> posWord : words) {
            newWords.add(new ArrayList<>(posWord));
        }
        for (int i = 0; i < board.length; i += 2) {
            getPossibleWords(i, true, newWords.get(i), null, false);
            getPossibleWords(i, false, newWords.get(i + 1), null, false);
        }
        return newWords;
    }

    private static void decolapse(String returnValue) {
        boolean isWrong = false;
        int[] values = new int[returnValue.length()];
        for (int i = 0; i < returnValue.length(); i++) {
            if (returnValue.charAt(i) == 'f') {
                isWrong = true;
                continue;
            }
            values[i] = Integer.parseInt(returnValue.charAt(i) + "");
        }

        for (int i = values.length - 1; i > 0 + (isWrong ? 1 : 0); i -= 6) {
            int y1 = values[i - 5];
            int x1 = values[i - 4];
            int state1 = values[i - 3];
            int y2 = values[i - 2];
            int x2 = values[i - 1];
            int state2 = values[i];
            board[y1][x1].setState(state2);
            switchLetters(y1, x1, y2, x2).setState(state1);
        }
    }

    private static Letter switchLetters(int y1, int x1, int y2, int x2) {
        Letter temp = board[y1][x1];
        board[y1][x1] = board[y2][x2];
        board[y2][x2] = temp;
        return board[y1][x1];
    }

    private static void copyBoard(char[] endState) {
        int counter = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != null) {
                    endState[counter] = board[i][j].getLetter();
                } else {
                    endState[counter] = ' ';
                }
                counter++;
            }
        }
    }

    private static ArrayList<ArrayList<Integer>> getSwitches(char[] endArray) {
        char[] startArray = setupStartEndArrays(endArray);
        return getLeastPossibleAmountOfSwaps(startArray, endArray);
    }

    private static char[] setupStartEndArrays(char[] endArray) {
        char[] startArray = new char[board.length * board.length];
        int counter = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                startArray[counter] = (board[i][j] == null || board[i][j].getState() == 2) ? ' ' : board[i][j].getLetter();
                counter++;
            }
        }
        return startArray;
    }

    private static ArrayList<ArrayList<Integer>> getLeastPossibleAmountOfSwaps(char[] startArray, char[] endArray) {
        ArrayList<ArrayList<Integer>> swaps = new ArrayList<>();
        while (!isEmpty(startArray)) {
            ArrayList<Integer> firstShortestCycle = new ArrayList<>();
            for (int i = 0; i < startArray.length; i++) {
                if (startArray[i] == ' ') {
                    continue;
                }
                getCycle(i, startArray, endArray, new ArrayList<>(), firstShortestCycle);                
            }
            swaps.add(firstShortestCycle);
            for (int index : firstShortestCycle) {
                startArray[index] = ' ';
            }
        }
        return swaps;
    }

    private static boolean isEmpty(char[] startArray) {
        for (char letter : startArray) {
            if (letter != ' ') {
                return false;
            }
        }
        return true;
    }

    private static ArrayList<Integer> getCycle(int start, char[] array1, char[] array2, ArrayList<Integer> cycle, ArrayList<Integer> firstShortestCycle) {
        if (cycle.isEmpty()) {
            cycle.add(start);
        } else if (array2[start] == array1[cycle.getFirst()]) {
            return cycle;
        }
        for (int i = 0; i < array1.length; i++) {
            if (!cycle.contains(i) && array2[cycle.getLast()] == array1[i]) {
                cycle.add(i);
                if ((firstShortestCycle.isEmpty() || cycle.size() <= firstShortestCycle.size()) && getCycle(i, array1, array2, cycle, firstShortestCycle) != null) {
                    if (firstShortestCycle.isEmpty() || firstShortestCycle.size() > cycle.size()) {
                        firstShortestCycle.clear();
                        firstShortestCycle.addAll(cycle);
                    }
                }
                cycle.removeLast();
            }
        }
        return null;
    }

    private static void showSwitches(ArrayList<ArrayList<Integer>> switches) {
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
                printBoard();
                letters[0].setState(2);
                char tempLetter = letters[1].getLetter();
                letters[1].setLetter(letters[0].getLetter());
                letters[0].setLetter(tempLetter);
            }
            letters[1].setState(2);
        }
        printBoard();
    }

    private static void printBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (i % 2 == 1 && j % 2 == 1) {
                    System.out.print(" |");
                    continue;
                }
                switch (board[i][j].getState()) {
                    case 0:
                        System.out.print(WHITE_BG + board[i][j].getLetter() + RESET);
                        break;
                    case 1:
                        System.out.print(YELLOW_BG + board[i][j].getLetter() + RESET);
                        break;
                    case 2:
                        System.out.print(GREEN_BG + board[i][j].getLetter() + RESET);
                        break;
                    case 3:
                        System.out.print(PURPLE_BG + board[i][j].getLetter() + RESET);
                        break;
                    default:
                        System.out.print(board[i][j].getLetter());
                        break;
                }
                if (j + 1 != board[i].length) {
                    System.out.print("|");
                }
            }
            if (i + 1 != board[i].length) {
                System.out.println("\n" + "-+".repeat(board.length - 1) + "-");
            }
        }
        System.out.println("\n");
    }
}
