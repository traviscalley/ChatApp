package RemoteChat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LocalChatroom extends UnicastRemoteObject implements Chatroom
{
    private static final long serialVersionUID = -7917490423989282914L;
    public final String name;
    private Map<Long, RemoteUser> users;
    private AtomicLong userID;


    public LocalChatroom(String name) throws RemoteException {
        this.name = name;
        users = new ConcurrentHashMap<>();
        userID = new AtomicLong(1);
    }

    public String toString() {
        return "LocalChatroom: " + name;
    }

    @Override
    public long[] getAllUsers() throws RemoteException {
        return new long[0];
    }

    @Override
    public RemoteUser getRemoteUser(long userID) throws RemoteException {
        return null;
    }

    @Override
    public long createUser(String name) throws RemoteException {
        long ID = userID.getAndIncrement();
        users.putIfAbsent(ID, new UserImpl(ID, name));
        return ID;
    }

    @Override
    public String deleteUser(long userID) throws RemoteException {
        return null;
    }
    // end of LocalChatroom implementation

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
        public void send(String message) throws RemoteException {

        }

        @Override
        public void like(long msgID) throws RemoteException {

        }

        @Override
        public synchronized void changeName(String newName) throws RemoteException {
            name = newName;
        }
    }
    // end of UserImpl implementation
}
