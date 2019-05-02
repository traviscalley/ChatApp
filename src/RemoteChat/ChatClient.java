package RemoteChat;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Sample client.
 */
public class ChatClient {

    private final Chatroom chatroom;

    private ChatClient(String service) throws RemoteException, NotBoundException, MalformedURLException {
        chatroom = (Chatroom)java.rmi.Naming.lookup(service);
    }

    private void addUser(User user) throws RemoteException {
        System.out.printf("user added: %d%n", chatroom.addUser(user));
    }

    private void removeUser(User user) throws RemoteException {
        System.out.printf("user removed with id: %d%n", chatroom.removeUser(user));
    }

    private void blockUser(User user) throws RemoteException {
        System.out.printf("user blocked with id: %d%n", chatroom.blockUser(user));
    }

    private void sendMessage(User user, Message msg) throws RemoteException {
        chatroom.createMessage(msg.getContent(), msg.getParent(), user.id);
        System.out.println(user.name + ": " + msg.getContent() + " was sent");
    }

    public static void main(String[] args) throws Exception {
        String service = "rmi://127.0.0.1:" + 51350 + "/" + ChatServerApp.CHATROOM_NAME; // @TODO undo hardcoding

        ChatClient client = new ChatClient(service);
        try {
            User user = new User(1, "Travis");
            client.addUser(user);
            Message sentMsg = new Message(0, "Hello, world!", 1);
            client.sendMessage(user, sentMsg);
            return;
        } catch (ChatException e) {
            System.out.printf("chat exception: %s%n", e.getMessage());
        }
    }
}