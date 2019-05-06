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
    private Map<Long, Message> messages; // key: message ID
    private Set<Long> rootMessages;
    private final AtomicLong messageID;


    public LocalChatroom(String name) throws RemoteException {
        this.name = name;
        users = new ConcurrentHashMap<>();
        blockedUsers = new ConcurrentHashMap<>();
        messages = new ConcurrentHashMap<>();
        messageID = new AtomicLong(1);
        rootMessages = new ConcurrentSkipListSet<>();
    }

    private String printChatThread(Message m, String prefix){
        var buf = new StringBuffer();
        buf.append(prefix);
        buf.append(m.toString());
        for(long id : m.getChildren()){
            buf.append("\n");
            var child = getMessage(id);
            buf.append(printChatThread(child, prefix + "    "));
        }
        return buf.toString();
    }

    public String print() {
        var buf = new StringBuffer();
        buf.append("== Room: ");
        buf.append(name);
        buf.append(" ==\n");

        for(long id : rootMessages)
            buf.append(printChatThread(messages.get(id), ""));

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

    public Message getMessage(long id) {
        if (!messages.containsKey(id))
            throw new ChatException("Message doesn't exist!");
        return messages.remove(id);
    }

    public long createMessage(String content, long parentID, long userID) {
        User user = users.getOrDefault(userID, null);
        if (user == null)
            throw new ChatException("User: " + userID +
                    " is not allowed to send messages in this room!");
        if (blockedUsers.containsKey(userID)) {
            System.out.println("Users" + userID + " is blocked and cannot" +
                    " send messages in this Chatroom!");
            return -1;
        }
        Message newMsg = new Message(parentID, content, messageID.getAndIncrement(), user);
        if(parentID < 1)
            rootMessages.add(newMsg.getId());
        else
            messages.get(parentID).addChild(newMsg.getId());
        messages.putIfAbsent(newMsg.getId(), newMsg);
        return newMsg.getId();
    }

    public int likeMessage(long id) {
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
