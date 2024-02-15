package util;

public class Letter {

    private char letter;
    private int state;

    public Letter(char letter, int state) {
        this.letter = letter;
        this.state = state;
    }

    public char getLetter() {
        return this.letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
