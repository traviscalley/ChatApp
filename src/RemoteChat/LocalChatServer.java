package RemoteChat;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LocalChatServer implements ChatServer {

    private final ConcurrentHashMap<Long, User> users;
    private final ConcurrentHashMap<Long, LocalChatroom> rooms;
    private final AtomicLong nextUserID;
    private final AtomicLong nextRoomID;


    public LocalChatServer(){
        users = new ConcurrentHashMap<>();
        rooms = new ConcurrentHashMap<>();
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
}