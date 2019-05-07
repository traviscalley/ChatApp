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
        String msg;
        long id;
        Chatroom room;
        long i;

        GhostTask(String msg, long id, Chatroom room, long i) {
                this.msg = msg;
                this.id = id;
                this.room = room;
                this.i = i;
        }

        @Override
        public void run() {
            try {
                room.createMessage(msg, i, id);
                i+=2;
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                i--;
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
        long id1 = client.server.createUser("Ghost 1");
        User usr1 = client.server.getUser(id1);
        ghostRoom.addUser(usr1);

        long id2 = client.server.createUser("Ghost 2");
        User usr2 = client.server.getUser(id2);
        ghostRoom.addUser(usr2);

        long mid = ghostRoom.createMessage("Start of conversation", 0, id1);

        Timer t1 = new Timer();
        t1.scheduleAtFixedRate(new GhostTask("Hey!", id1, ghostRoom, mid),
                0, 8000);

        long mid2 = ghostRoom.createMessage("First Reply", mid, id2);


        Timer t2 = new Timer();
        t2.scheduleAtFixedRate(new GhostTask("Hi!", id2, ghostRoom, mid2),
                9000, 7000);

        Timer t3 = new Timer();
        t3.scheduleAtFixedRate(new TimerTask() {
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
