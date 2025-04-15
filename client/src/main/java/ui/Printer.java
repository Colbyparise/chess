package ui;

public class Printer {


    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void notify(String notification) {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_GREEN + notification);
        printName();
    }

    public void printWelcome() {
        System.out.println("Welcome to Chess client!");
    }

    public void printError(String err) {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + err);
        //printName();
    }

    public void printResponse(String response) {
        System.out.println(EscapeSequences.SET_TEXT_BOLD_AND_BLUE + response);
    }

    public void printName() {
        System.out.print(EscapeSequences.SET_TEXT_NORMAL_AND_WHITE + EscapeSequences.SET_TEXT_BOLD +
                "[" + username + "]: ");
    }

    public void printSubCommand(String sub) {
        System.out.print(EscapeSequences.SET_TEXT_NORMAL_AND_WHITE + EscapeSequences.SET_TEXT_BOLD +
                "\n" + sub + ": ");
    }
}