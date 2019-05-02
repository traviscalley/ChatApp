package RemoteChat;

import java.rmi.RemoteException;

public interface RemoteUser extends java.rmi.Remote
{
    /**
     * User ID. can be cached as it never changes.
     *
     * @return User ID
     */
    long userID() throws RemoteException;

    /**
     * Changes the name of the current User.
     *
     * @param newName the name to be changed to.
     * @throws ChatException if the name is already taken or an empty.
     */
    void changeName(String newName) throws RemoteException;

    /**
     * Get the name of the user.
     */
    String getName() throws RemoteException;
}
