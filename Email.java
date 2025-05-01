/**
 * Represents one email message and its label.
 * Define the data model for each email
 */
public class Email {
    public int label; // 1 = spam, 0 = not spam
    public String text;

    public Email(int label, String text) {
        this.label = label;
        this.text = text;
    }
}