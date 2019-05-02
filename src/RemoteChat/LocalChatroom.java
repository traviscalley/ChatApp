package RemoteChat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LocalChatroom extends UnicastRemoteObject implements Chatroom {
    private static final long serialVersionUID = -7917490423989282914L;
    public final String name;
    private final AtomicLong messageID;
    private Map<Long, RemoteUser> users;
    private Map<Long, RemoteUser> blockedUsers;
    private Map<Long, Message> messages; // key: message ID


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
    public RemoteUser getRemoteUser(long userID) throws RemoteException {
        return null;
    }

    @Override
    public long addUser(String name, long ID) throws RemoteException {
        users.putIfAbsent(ID, new UserImpl(ID, name));
        return ID;
    }

    @Override
    public String removeUser(long userID) throws RemoteException {
        if (!users.containsKey(userID))
            throw new ChatException("User with ID:" + userID + " does not exist!");
        return users.remove(userID).getName();
    }


    public long[] getRootMessages(){
        return null;
    }


    public Message getMessage(long id){
        if (!messages.containsKey(id))
            throw new ChatException("Message doesn't exist!");
        return messages.remove(id);
    }

    public int likeMessage(long id){
        return -1;
    }

    public int dislikeMessage(long id) {
        return -1;
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

    public boolean blockUser(long userID) {
        if (!users.containsKey(userID))
            throw new ChatException("User: " + userID +
                    " doesn't exist and cannot be blocked!");
        //return blockedUsers.putIfAbsent(userID);
        return false;
    }


    private class UserImpl extends UnicastRemoteObject implements RemoteUser
    {
        private static final long serialVersionUID = -807467293818944318L;
        private final long ID;
        private String name;

        public UserImpl(long id, String name) throws RemoteException{
            ID = id;
            this.name = name;
        }

        @Override
        public long userID() throws RemoteException {
            return ID;
        }

        @Override
        public synchronized void changeName(String newName) throws RemoteException {
            name = newName;
        }

        public synchronized String getName() {
            return name;
        }
    }
    // end of UserImpl implementation
}
