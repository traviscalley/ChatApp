package RemoteChat;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class LikeGhostClient {
    private final ChatServer server;

    private LikeGhostClient(String service) throws RemoteException,
            NotBoundException, MalformedURLException
    {
        server = (ChatServer) java.rmi.Naming.lookup(service);
    }

    private static class LikeTask extends TimerTask {
        User usr;
        Chatroom room;
        Queue<RemoteMessage> messages;
        Set<Long> ids;

        LikeTask(User usr, Chatroom room) {
            this.usr = usr;
            this.room = room;
            messages = new ConcurrentLinkedQueue<>();
            ids = new ConcurrentSkipListSet<>();
        }

        @Override
        public void run() {
            try {
                for (long mid : room.getRootMessages()) {
                    if (!ids.contains(mid)) {
                        messages.add(room.getMessage(mid));
                        ids.add(mid);
                    }
                }
                for (RemoteMessage msg: messages) {
                    msg.dislike();
                    for (RemoteMessage child : msg.getChildren()) {
                        if (!ids.contains(child.getId())) {
                            messages.add(child);
                            ids.add(child.getId());
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private static class DislikeTask extends TimerTask {
        User usr;
        Chatroom room;
        Queue<RemoteMessage> messages;
        Set<Long> ids;

        DislikeTask(User usr, Chatroom room) {
            this.usr = usr;
            this.room = room;
            messages = new ConcurrentLinkedQueue<>();
            ids = new ConcurrentSkipListSet<>();
        }

        @Override
        public void run() {
            try {
                for (long mid : room.getRootMessages()) {
                    if (!ids.contains(mid)) {
                        messages.add(room.getMessage(mid));
                        ids.add(mid);
                    }
                }
                for (RemoteMessage msg: messages) {
                    msg.dislike();
                    for (RemoteMessage child : msg.getChildren()) {
                        if (!ids.contains(child.getId())) {
                            messages.add(child);
                            ids.add(child.getId());
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException
    {
        String service = "rmi://" + args[0] + ":" + args[1] + "/" + ChatServerApp.CHATROOM_NAME;
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

        likeTimer.scheduleAtFixedRate(new LikeTask(likeUsr, ghostRoom), 0, 2000);
        disTimer1.scheduleAtFixedRate(new DislikeTask(usr1, ghostRoom), 0, 1600);

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
        }, 0, 4000);
    }
}
