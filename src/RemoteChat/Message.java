package RemoteChat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Message extends UnicastRemoteObject implements RemoteMessage {
    private static final long serialVersionUID = -1891091551243657306L;
    private int likes;
    private boolean deleted;
    private final long  id;
    private final long parent;
    private final User poster;
    private String content;
    private final ArrayList<RemoteMessage> children;
    private final LinkedBlockingQueue<RemoteMessage> replies;

    public Message(long parent, String message, long id, User user) throws RemoteException {
        synchronized (this) {
            likes = 0;
            this.parent = parent;
            this.content = message;
            this.id = id;
            this.children = new ArrayList<>();
            deleted = false;
            poster = user;
            replies = new LinkedBlockingQueue<>();
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

    public synchronized boolean isDeleted() { return deleted; }

    public synchronized long getId() { return id; }

    public synchronized long getParent() { return parent; }

    public synchronized User getUser() {
        return poster;
    }

    public synchronized String getContent() { return content; }

    public synchronized List<RemoteMessage> getChildren() { return children; }

    public synchronized void addChild(RemoteMessage id) {
        children.add(id);
        replies.add(id);
    }

    public RemoteMessage getReply() throws RemoteException {
        try {
            return replies.take(); //blocks
        } catch(InterruptedException e){
            System.err.println("Thread was interupted while blocking!");
        }
        return null;
    }

    public String print(){
        if (deleted)
            return poster.name + "'s message was deleted because of too many dislikes!";
        return poster.name + ": " + "[" + getLikes() + "] - " + getContent() + " - [" + getId() + "]";
    }
}
