import java.util.Optional;

public class Result {
    private Optional<String> singleString;
    private Optional<String[]> stringArray;
    private int number;

    // Constructor for single string result
    public Result(String text, int number) {
        this.singleString = Optional.ofNullable(text);
        this.stringArray = Optional.empty();
        this.number = number;
    }

    // Constructor for string array result
    public Result(String[] texts, int number) {
        this.singleString = Optional.empty();
        this.stringArray = Optional.ofNullable(texts);
        this.number = number;
    }

    // Constructor for number-only result
    public Result(int number) {
        this.singleString = Optional.empty();
        this.stringArray = Optional.empty();
        this.number = number;
    }

    // Getter for single string
    public Optional<String> getSingleString() {
        return singleString;
    }

    // Getter for string array
    public Optional<String[]> getStringArray() {
        return stringArray;
    }

    // Getter for number
    public int getMsgNum() {
        return number;
    }

    // New method to retrieve multiple strings as an array
    public Optional<String[]> getMultipleStrings() {
        return stringArray;
    }
}
