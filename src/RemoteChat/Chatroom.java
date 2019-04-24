package RemoteChat;

import java.rmi.RemoteException;

public interface Chatroom extends java.rmi.Remote
{
    /**
     * All users in the Chatroom. This may only be any approximation if
     * accounts are added/removed while this call is taking place.
     *
     * @return a newly allocated array with all the users in the Chatroom.
     * @throws RemoteException
     */
    long[] getAllUsers() throws RemoteException;

    /**
     * A remote user.
     *
     * @param userID the ID of the user to be returned.
     * @return a remote user (stub) backed by the corresponding user in the chatroom
     * @throws ChatException if the given ID is not valid
     */
    RemoteUser getRemoteUser(long userID) throws RemoteException;

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
}
