package RemoteChat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServer extends Remote
{
    final String CHATROOM_NAME = "CS735_PROJECT_ROOM";

    /**
     * A remote user.
     *
     * @param userID the ID of the user to be returned.
     * @return a remote user (stub) backed by the corresponding user in the chatroom
     * @throws ChatException if the given ID is not valid
     */
    User getUser(long userID) throws RemoteException;

    /**
     * Creates a new user.
     *
     * @return the ID of the newly created User, which is guaranteed to be positive
     * and different from all other User IDs.
     */
    long createUser(String name) throws RemoteException;

    /**
     * Deletes an existing user.
     *
     * @return the name of the User that was closed.
     * @throws ChatException if the given ID doesn't correspond to a User
     */
    String deleteUser(long userID) throws RemoteException;

    String printStats(long id) throws RemoteException;

    Chatroom getRemoteChatroom(long Id) throws RemoteException;

    long createChatRoom(String name) throws RemoteException;

    int incrementMessages(long id) throws RemoteException;

}
