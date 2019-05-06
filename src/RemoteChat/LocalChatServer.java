package RemoteChat;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LocalChatServer extends UnicastRemoteObject implements ChatServer {
    private static final long serialVersionUID = 2922902607870763074L;
    private final ConcurrentHashMap<Long, User> users;
    private final ConcurrentHashMap<Long, Integer> totMessages;
    private final ConcurrentHashMap<Long, LocalChatroom> rooms;
    private final AtomicLong nextUserID;
    private final AtomicLong nextRoomID;


    LocalChatServer() throws RemoteException {
        users = new ConcurrentHashMap<>();
        rooms = new ConcurrentHashMap<>();
        //totLikes = new ConcurrentHashMap<>();
        totMessages = new ConcurrentHashMap<>();
        nextUserID = new AtomicLong(1);
        nextRoomID = new AtomicLong(1);
    }

    public User getUser(long userID) {
        var user = users.getOrDefault(userID, null);
        if(user == null)
            throw new ChatException("User does not exist");
        return user;
    }

    public long createUser(String name) {
        long id = nextUserID.getAndIncrement();
        var user = new User(id, name);
        users.put(id, user);
        return id;
    }

    public String deleteUser(long userID){
        var user = users.remove(userID);
        return user.name;
    }

    public String printStats(long id) throws RemoteException {
        StringBuilder stats = new StringBuilder();

        User usr = users.get(id);
        stats.append(usr.name);
        stats.append("'s Statistics:\n");
        stats.append("    Total Messages Sent: ");
        stats.append(totMessages.get(id));
        stats. append("\n    Total likes: ");

        int totLikes = 0;
        for (long rid: rooms.keySet()) {
            LocalChatroom room = rooms.get(rid);
            Map<Long, RemoteMessage> msgs = room.getMessageMap();
            for (long mid: room.getMessageMap().keySet()) {
                RemoteMessage msg = msgs.get(mid);
                if (msg.getUser().id == id)
                    totLikes += msg.getLikes();
            }
        }

        stats.append(totLikes);

        return stats.toString();
    }

    public Chatroom getRemoteChatroom(long Id) {
        Chatroom room = rooms.getOrDefault(Id, null);
        if(room == null)
            throw new ChatException("chatroom does not exist");
        return room;
    }

    public long createChatRoom(String name) throws RemoteException {
        var room = new LocalChatroom(name);
        long id = nextRoomID.getAndIncrement();
        rooms.put(id, room);
        return id;
    }

    public synchronized int incrementMessages(long id) {
        totMessages.putIfAbsent(id, 0);
        int num = totMessages.get(id);
        num += 1;
        totMessages.put(id, num);
        return num;

    }
}