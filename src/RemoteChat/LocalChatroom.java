package RemoteChat;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public class LocalChatroom extends UnicastRemoteObject implements Chatroom {
    private static final long serialVersionUID = -7917490423989282914L;
    private final String name;
    private Map<Long, User> users;
    private Map<Long, User> blockedUsers;
    private Map<Long, RemoteMessage> messages; // key: message ID
    private Set<Long> rootMessages;
    private static final AtomicLong messageID = new AtomicLong(1);

    LocalChatroom(String name) throws RemoteException {
        this.name = name;
        users = new ConcurrentHashMap<>();
        blockedUsers = new ConcurrentHashMap<>();
        messages = new ConcurrentHashMap<>();
        //messageID = new AtomicLong(1);
        rootMessages = new ConcurrentSkipListSet<>();
        Timer timer = new Timer();
        DislikeChecker checker = new DislikeChecker(messages);
        timer.scheduleAtFixedRate(new TimerTask() {public void run() { checker.run(); }},
                0, 15000);
    }

    private String printChatThread(RemoteMessage m, String prefix) throws RemoteException{
        StringBuilder buf = new StringBuilder("\n");
        buf.append(prefix);
        buf.append(m.print());
        for(long id : m.getChildren()){
            var child = getMessage(id);
            buf.append(printChatThread(child, prefix + "    "));
        }
        return buf.toString();
    }

    public String print() throws RemoteException {
        StringBuilder buf = new StringBuilder();
        buf.append("== Room: ");
        buf.append(name);
        buf.append(" ==\n");

        for(long id : rootMessages)
            buf.append(printChatThread(getMessage(id), ""));

        return buf.toString();
    }

    @Override
    public User getUser(long userID) {
        if (!users.containsKey(userID))
            throw new ChatException("User does not exist!");
        return users.get(userID);
    }

    @Override
    public long addUser(User add) {
        users.putIfAbsent(add.id, add);
        return add.id;
    }

    @Override
    public long removeUser(User remove) {
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
        return rootMessages.stream().mapToLong(x->x).toArray();
    }

    public RemoteMessage getMessage(long id) {
        if (!messages.containsKey(id))
            throw new ChatException("Message doesn't exist!");
        return messages.get(id);
    }

    Map<Long, RemoteMessage> getMessageMap() throws RemoteException {
        return Collections.unmodifiableMap(messages);
    }

    public long createMessage(String content, long parentID, long userID) throws RemoteException {
        User user = users.getOrDefault(userID, null);
        if (user == null)
            throw new ChatException("User: " + userID +
                    " is not allowed to send messages in this room!");
        if (blockedUsers.containsKey(userID)) {
            System.out.println("Users" + userID + " is blocked and cannot" +
                    " send messages in this Chatroom!");
            return -1;
        }

        synchronized (this) {
        RemoteMessage newMsg;
        try {
            newMsg = new Message(parentID, content, messageID.getAndIncrement(), user);
        } catch (RemoteException e) {
            throw new ChatException("Message was unable to be created!");
        }
        if(parentID < 1)
            rootMessages.add(newMsg.getId());
        else
            messages.get(parentID).addChild(newMsg.getId());
        messages.putIfAbsent(newMsg.getId(), newMsg);
        return newMsg.getId();
        }
    }

    public synchronized void addMessage(long mid, RemoteMessage msg) throws RemoteException {
        messages.putIfAbsent(mid, msg);
        rootMessages.add(mid);
    }

    public int likeMessage(long id) throws RemoteException {
        if (!messages.containsKey(id))
            throw new ChatException("Message does not exist!");
        return messages.get(id).like();
    }

    public int dislikeMessage(long id) throws RemoteException {
        if (!messages.containsKey(id))
            throw new ChatException("Message does not exist!");
        return messages.get(id).dislike();
    }
}
