public enum Commands {
    GET_INFO("/info"), UPLOAD("/u"), DOWNLOAD("/d"), AUTH("/auth"), STOP("/stop");

    public final String command;

    Commands(String command) {
        this.command = command;
    }
}
