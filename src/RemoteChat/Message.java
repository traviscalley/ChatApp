package RemoteChat;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private int likes, dislikes;
    private boolean deleted;
    private final long  id;
    private final long parent;
    private String content;
    private final ArrayList<Long> children;

    public Message(long parent, String message, long id){
        synchronized (this) {
            likes = 0;
            dislikes = 0;
            this.parent = parent;
            this.content = message;
            this.id = id;
            this.children = new ArrayList<>();
            deleted = false;
        }
    }

    public synchronized int like(){
        return ++likes;
    }

    public synchronized int dislike(){
        return ++dislikes;
    }

    public synchronized int getLikes() {
        return likes;
    }

    public synchronized int getDislikes() {
        return dislikes;
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

    @Override
    public String toString(){
        return getContent();
    }

    //TODO: define equality and hash
}
