package RemoteChat;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

public class MessageGhostClient {
    private final ChatServer server;

    private MessageGhostClient(String service) throws RemoteException,
            NotBoundException, MalformedURLException {
        server = (ChatServer) java.rmi.Naming.lookup(service);
    }

    private static class GhostTask extends TimerTask {
        String str;
        long id;
        Chatroom room;
        long i;

        GhostTask(String str, long id, Chatroom room, long i) {
                this.str = str;
                this.id = id;
                this.room = room;
                this.i = i;
        }

        @Override
        public void run() {
            try {
                RemoteMessage msg = room.getMessage(i).getReply();
                i = room.createMessage(str, msg.getId(), id);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        String service = "rmi://" + args[0] + ":" + args[1] + "/" + ChatServerApp.CHATROOM_NAME;
        MessageGhostClient client = new MessageGhostClient(service);

        // Room for them to speak in
        long rid = client.server.createChatRoom("Ghost Messaging Room");
        Chatroom ghostRoom = client.server.getRemoteChatroom(rid);
        System.out.println("Ghost Messaging Room ID: " + rid);

        // Ghost 1
        long id1 = client.server.createUser("Ghost 1");
        User usr1 = client.server.getUser(id1);
        ghostRoom.addUser(usr1);
        // Ghost 2
        long id2 = client.server.createUser("Ghost 2");
        User usr2 = client.server.getUser(id2);
        ghostRoom.addUser(usr2);


        long mid = ghostRoom.createMessage("Start of conversation", 0, id1);
        long mid2 = ghostRoom.createMessage("First Reply", mid, id2);


        // Ghost 1 Timer
        Timer t1 = new Timer();
        t1.scheduleAtFixedRate(new GhostTask("Hey!", id1, ghostRoom, mid),
                0, 2000);

        // Ghost 2 Timer
        Timer t2 = new Timer();
        t2.scheduleAtFixedRate(new GhostTask("Hi!", id2, ghostRoom, mid2),
                0, 2000);

        Timer printTimer = new Timer();
        printTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println(ghostRoom.print());
                    System.out.println(client.server.printStats(id1));
                    System.out.println(client.server.printStats(id2));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 4000);
    }
}
