package RemoteChat;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Message extends UnicastRemoteObject implements RemoteMessage {
    private static final long serialVersionUID = -1891091551243657306L;
    private int likes;
    private boolean deleted;
    private final long  id;
    private final long parent;
    private final User poster;
    private String content;
    private final ArrayList<Long> children;

    public Message(long parent, String message, long id, User user) throws RemoteException {
        synchronized (this) {
            likes = 0;
            this.parent = parent;
            this.content = message;
            this.id = id;
            this.children = new ArrayList<>();
            deleted = false;
            poster = user;
        }
    }

    public synchronized int like(){
        if (deleted)
            return likes;
        return ++likes;
    }

    public synchronized int dislike() {
        if (deleted)
            return likes;
        return --likes;
    }

    public synchronized int getLikes() {
        return likes;
    }

    @Override
    public synchronized boolean delete() throws RemoteException {
        if (!deleted) {
            deleted = true;
            content = "<DELETED>";
        }
        return deleted;
    }

    public synchronized boolean isDeleted() {
        return deleted;
    }

    public synchronized long getId() {
        return id;
    }

    public synchronized long getParent() {
        return parent;
    }

    public synchronized User getUser() {
        return poster;
    }

    public synchronized String getContent() {
        return content;
    }

    public synchronized List<Long> getChildren() {
        return children;
    }

    public synchronized void addChild(long id) {
        children.add(id);
    }

    public String print(){
        if (deleted)
            return poster.name + "'s message was deleted because of too many dislikes!";
        return poster.name + ": " + "[" + getLikes() + "] - " + getContent() + " - [" + getId() + "]";
    }

    //TODO: define equality and hash
}
