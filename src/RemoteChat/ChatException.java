package RemoteChat;

public class ChatException extends RuntimeException
{
    private static final long serialVersionUID = 7868151224246874686L;

    ChatException(String message) {
        super(message);
    }
}
