package RemoteChat;

import java.rmi.RemoteException;
import java.util.Map;

public class DislikeChecker implements Runnable
{
    private final Map<Long, RemoteMessage> messages;

    DislikeChecker(Map<Long, RemoteMessage> messages) {
        this.messages = messages;
    }

    @Override
    public void run() {
        messages.forEach((k, v) -> {
            try {
                if (v.getLikes() <= -10)
                    v.delete();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
