package RemoteChat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Chatroom extends java.rmi.Remote {
    /**
     * A remote user.
     *
     * @param userID the ID of the user to be returned.
     * @return a remote user (stub) backed by the corresponding user in the chatroom
     * @throws ChatException if the given ID is not valid
     */
    User getUser(long userID) throws RemoteException;

    String print() throws RemoteException;

    /**
     * Creates a new user.
     *
     * @return the ID of the newly created User, which is guaranteed to be positive
     * and different from all other User IDs.
     */
    long addUser(User add) throws RemoteException;

    /**
     * Deletes an existing user.
     *
     * @return the name of the User that was closed.
     * @throws ChatException if the given ID doesn't correspond to a User
     */
    long removeUser(User remove) throws RemoteException;

    /**
     * Blocks an existing user.
     *
     * @returns a boolean if the block was successful
     * @throws ChatException if the given user isn't blocked
     */
    long blockUser(User block) throws RemoteException;

    /**
     *
     * @param content String of the message contents
     * @param parentID A positive number is the Message this Message is replying to.
     *                 A '0' value means that this message has no parent
     * @return Id of newly created message
     * @throws RemoteException
     */
    long createMessage(String content, long parentID, long userID) throws RemoteException;

    /**
     *
     * @return array of IDs of all the parent messages
     * @throws RemoteException
     */
    long[] getRootMessages() throws RemoteException;

    Message getMessage(long id) throws RemoteException;

}
