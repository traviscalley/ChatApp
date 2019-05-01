package RemoteChat;

import java.rmi.RemoteException;

public class LocalChatServer implements ChatServer {

    public LocalChatServer(){

    }

    public long[] getAllUsers() {
        return null;
    }

    public RemoteUser getRemoteUser(long userID) {
        return null;
    }

    public long createUser(String name) {
        return 0;
    }

    public String deleteUser(long userID){
        return null;
    }


    public Chatroom getRemoteChatroom(long Id) {
        return null;
    }

    public long createChatRoom() {
        return 0;
    }
}
