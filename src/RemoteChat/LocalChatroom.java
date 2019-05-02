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
    private Map<Long, RemoteUser> users;
    private Map<Long, RemoteUser> blockedUsers;
    private Map<Long, Message> messages; // key: message ID


    public LocalChatroom(String name) throws RemoteException {
        this.name = name;
        users = new ConcurrentHashMap<>();
        blockedUsers = new ConcurrentHashMap<>();
        messages = new ConcurrentHashMap<>();
    }

    public String toString() {
        return "LocalChatroom: " + name;
    }

    @Override
    public long[] getAllUsers() throws RemoteException {
        java.util.Set<Long> keys = users.keySet();
        long[] allUsers = new long[keys.size()];
        int i = 0;
        for (Long k: keys) {
            allUsers[i++] = k;
        }
        return allUsers;
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

    public long createMessage(String content, long parentID){
        long msgID = -1;
        Message newMsg = new Message(parentID, content, msgID); // @ TODO change ID
        return msgID;
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
