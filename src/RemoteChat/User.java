package RemoteChat;

import java.io.Serializable;

public class User implements Serializable
{
    public final long id;
    public final String name;

    public User(long id, String name){
        this.id = id;
        this.name = name;
    }
}