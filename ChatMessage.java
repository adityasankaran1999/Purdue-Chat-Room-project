import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    private String message;
    private int type;
    static final int GENMESSAGE = 0, LOGOUT = 1, directMessage= 2, displayList = 3;
    private String recipient;


    public ChatMessage(String message, int type, String recipient) {
        this.message = message;
        this.type = type;   //0 is general message , 1 is logout message
        this.recipient = recipient;

    }

    public ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;

    }

    public int getType() {
        return type;
    }

    public String getRecipient()
    {
        return this.recipient;
    }

    public String getMessage() {
        return message;
    }
}
