package RemoteChat;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LikeGhostClient {
    private final Chatroom chatroom;
    private final ChatServer server;

    private LikeGhostClient(String service) throws RemoteException,
            NotBoundException, MalformedURLException
    {
        server = (ChatServer) java.rmi.Naming.lookup(service);
        chatroom = new LocalChatroom("Client's Room");
    }

    private static class LikeTask extends TimerTask {
        User usr;
        Chatroom room;

        LikeTask(User usr, Chatroom chatroom) {
            this.usr = usr;
            this.room = room;
        }

        @Override
        public void run() {
            try {
                long[] messages = room.getRootMessages();
                if (messages.length > 0) {
                    for (int i = 0; i < messages.length; i++) {
                        List<Long> msgs = room.getMessage(messages[i]).getChildren();
                        for ( Long mid : msgs) {
                            room.getMessage(mid).like();
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                // do nothing but wait for messages
            }
        }
    }

    private static class DislikeTask extends TimerTask {
        User usr;
        Chatroom room;

        DislikeTask(User usr, Chatroom room) {
            this.usr = usr;
            this.room = room;
        }

        @Override
        public void run() {
            try {
                long[] messages = room.getRootMessages();
                if (messages.length > 0) {
                    for (int i = 0; i < messages.length; i++) {
                        List<Long> msgs = room.getMessage(messages[i]).getChildren();
                        for ( Long mid : msgs) {
                            room.getMessage(mid).dislike();
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                // do nothing but wait for messages
            }
        }
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException
    {
        String service = "rmi://127.0.0.1:" + 51350 + "/" + ChatServerApp.CHATROOM_NAME;
        LikeGhostClient client = new LikeGhostClient(service);

        // Room for them to speak in
        long rid = client.server.createChatRoom("Ghost Like/Dislike Room");
        Chatroom ghostRoom = client.server.getRemoteChatroom(rid);
        System.out.println("Ghost Like/Dislike Room ID: " + rid);

        long lid = client.server.createUser("Like Ghost");
        User likeUsr = client.server.getUser(lid);
        Timer likeTimer = new Timer();

        long did1 = client.server.createUser("Dislike Ghost 1");
        User usr1 = client.server.getUser(did1);
        Timer disTimer1 = new Timer();

        long did2 = client.server.createUser("Dislike Ghost 2");
        User usr2 = client.server.getUser(did2);
        Timer disTimer2 = new Timer();

        likeTimer.scheduleAtFixedRate(new LikeTask(likeUsr, ghostRoom), 0, 5000);
        disTimer1.scheduleAtFixedRate(new DislikeTask(usr1, ghostRoom), 0, 5000);
        disTimer2.scheduleAtFixedRate(new DislikeTask(usr2, ghostRoom), 0 ,5000);

        Timer printTimer = new Timer();
        printTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println(ghostRoom.print());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 8000);
    }
}
