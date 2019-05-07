package RemoteChat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMessage extends Remote {
    int like() throws RemoteException;

    int dislike()throws RemoteException;

    int getLikes() throws RemoteException;

    boolean delete() throws RemoteException;

    User getUser() throws RemoteException;

    boolean isDeleted() throws RemoteException;

    long getId() throws RemoteException;

    long getParent() throws RemoteException;

    String getContent() throws RemoteException;

    java.util.List<RemoteMessage> getChildren() throws RemoteException;

    void addChild(RemoteMessage id) throws RemoteException;

    String print() throws RemoteException;

    RemoteMessage getReply() throws RemoteException;
}
