package RemoteChat;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LocalChatroom extends UnicastRemoteObject implements Chatroom {
    private static final long serialVersionUID = -7917490423989282914L;
    private final String name;
    private Map<Long, User> users;
    private Map<Long, User> blockedUsers;
    private Map<Long, Message> messages; // key: message ID
    private final AtomicLong messageID;


    public LocalChatroom(String name) throws RemoteException {
        this.name = name;
        users = new ConcurrentHashMap<>();
        blockedUsers = new ConcurrentHashMap<>();
        messages = new ConcurrentHashMap<>();
        messageID = new AtomicLong(1);
    }

    public String toString() {
        return "LocalChatroom: " + name;
    }

    @Override
    public User getUser(long userID) throws RemoteException {
        if (!users.containsKey(userID))
            throw new ChatException("User does not exist!");
        return users.get(userID);
    }

    @Override
    public long addUser(User add) throws RemoteException {
        users.putIfAbsent(add.id, add);
        return add.id;
    }

    @Override
    public long removeUser(User remove) throws RemoteException {
        if (!users.containsKey(remove.id))
            throw new ChatException("User with ID:" + remove.id + " does not exist!");
        return users.remove(remove.id).id;
    }

    public long blockUser(User block) {
        if (!users.containsKey(block.id))
            throw new ChatException("User: " + block.id +
                    " doesn't exist and cannot be blocked!");
        User ret = blockedUsers.putIfAbsent(block.id, block);
        if (ret != null)
            return ret.id;
        return -1;
    }

    public long[] getRootMessages(){
        java.util.Set<Long> keys = messages.keySet();
        long[] rootMsgs = new long[keys.size()];
        int i = 0;
        for (Long k: keys)
            rootMsgs[i++] = k;
        return rootMsgs;
    }

    public Message getMessage(long id){
        if (!messages.containsKey(id))
            throw new ChatException("Message doesn't exist!");
        return messages.remove(id);
    }

    public long createMessage(String content, long parentID, long userID) {
        if (!users.containsKey(userID))
            throw new ChatException("User: " + userID +
                    " is not allowed to send messages in this room!");
        if (blockedUsers.containsKey(userID))
            throw new ChatException("Users" + userID + " is blocked and cannot" +
                    " send messages in this Chatroom!");
        Message newMsg = new Message(parentID, content, messageID.getAndIncrement()); // @ TODO change ID
        return newMsg.getId();
    }

    public int likeMessage(long id){
        if (!messages.containsKey(id))
            throw new ChatException("Message does not exist!");
        return messages.get(id).like();
    }

    public int dislikeMessage(long id) {
        if (!messages.containsKey(id))
            throw new ChatException("Message does not exist!");
        return messages.get(id).dislike();
    }
}
