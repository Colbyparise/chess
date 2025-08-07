package websocket.messages;
import com.google.gson.annotations.SerializedName;


public class ErrorMessage extends ServerMessage {
    @SerializedName("errorMessage")
    private final String errorMessage;

    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }

    public String getMessage() {
        return errorMessage;
    }
}