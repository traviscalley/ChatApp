package RemoteChat;

import java.io.Serializable;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {
    private int likes;
    private boolean deleted;
    private final long  id;
    private final long parent;
    private final User poster;
    private String content;
    private final ArrayList<Long> children;

    public Message(long parent, String message, long id, User user){
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
        return ++likes;
    }

    public synchronized int dislike(){
        return --likes;
    }

    public synchronized int getLikes() {
        return likes;
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

    public synchronized String getContent() {
        return content;
    }

    public synchronized List<Long> getChildren() {
        return children;
    }

    public synchronized void addChild(long id) {
        children.add(id);
    }

    @Override
    public String toString(){
        return poster.name + ": " + "[" + getLikes() + "] - " + getContent() + " - [" + getId() + "]";
    }

    //TODO: define equality and hash
}
