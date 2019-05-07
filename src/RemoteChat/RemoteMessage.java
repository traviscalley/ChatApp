package RemoteChat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMessage extends Remote
{
    public int like() throws RemoteException;

    public int dislike()throws RemoteException;

    public int getLikes() throws RemoteException;

    public boolean delete() throws RemoteException;

    public User getUser() throws RemoteException;

    public boolean isDeleted() throws RemoteException;

    public long getId() throws RemoteException;

    public long getParent() throws RemoteException;

    public String getContent() throws RemoteException;

    public java.util.List<Long> getChildren() throws RemoteException;

    public void addChild(long id) throws RemoteException;

    public String print() throws RemoteException;

    public Long getReply() throws RemoteException;
}
