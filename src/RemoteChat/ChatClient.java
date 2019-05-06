package RemoteChat;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Sample client.
 */
public class ChatClient {

    private final Chatroom chatroom;
    private final ChatServer server;

    private ChatClient(String service) throws RemoteException, NotBoundException, MalformedURLException {
        //chatroom = (Chatroom)java.rmi.Naming.lookup(service);
        server = (ChatServer) java.rmi.Naming.lookup(service);
        chatroom = new LocalChatroom("Client's Room");
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
            long id = client.server.createUser("Travis");
            User user = client.server.getUser(id);
            System.out.println("Welcome to our ChatServer!");
            System.out.println("Your name is " + user.name + " and id is "
                    + user.id);
//            Message sentMsg = new Message(0, "Hello, world!", 1);
//            client.sendMessage(user, sentMsg);

            Scanner scanner = new Scanner(System.in);
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();

                // 1 <roomname> - create room
                if (line.startsWith("1")) {
                    String name = line.substring(1, line.length());
                    long uid = client.server.createChatRoom(name);
                    System.out.println("New ChatRoom made with name: " +
                            name + " and id: " + id);
                }
                // 2 <userId> <roomId> - add user to room
                else if (line.startsWith("2")) {
                    String[] input = line.split(" ");
                    User toAdd = client.server.getUser(Long.valueOf(input[1]));
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[2]));
                    room.addUser(toAdd);
                    System.out.println("User " + input[1] + " was added to room " +
                            input[2]);
                }
                // 3 <userId> <roomId> - remove user to room
                else if (line.startsWith("3")) {
                    String[] input = line.split(" ");
                    User toRemove = client.server.getUser(Long.valueOf(input[1]));
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[2]));
                    room.removeUser(toRemove);
                    System.out.println("User " + input[1] + " was removed from room " +
                            input[2]);
                }
                // 4 <userId> <roomId> - block user to room
                else if (line.startsWith("4")) {
                    String[] input = line.split(" ");
                    User toBlock = client.server.getUser(Long.valueOf(input[1]));
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[2]));
                    room.blockUser(toBlock);
                    System.out.println("User " + input[1] + " was blocked from room " +
                            input[2]);
                }
                // 5 <roomId> - print contents of room
                else if (line.startsWith("5")) {
                    String[] input = line.split(" ");
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[1]));
                    System.out.println(room.toString());
                }
                // 6 <roomId> <messageId> - like message
                else if (line.startsWith("6")) {
                    String[] input = line.split(" ");
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[1]));
                    Message msg = room.getMessage(Long.valueOf(input[2]));
                    msg.like();
                    System.out.println("Message " + input[2] + " was liked in room " +
                            input[1]);
                    System.out.println("Likes/Dislikes: " + msg.getLikes() + "/" + msg.getDislikes());
                }
                // 7 <roomId> <messageId> - dislike message
                else if (line.startsWith("7")) {
                    String[] input = line.split(" ");
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[1]));
                    Message msg = room.getMessage(Long.valueOf(input[2]));
                    msg.dislike();
                    System.out.println("Message " + input[2] + " was disliked in room " +
                            input[1]);
                    System.out.println("Likes/Dislikes: " + msg.getLikes() + "/" + msg.getDislikes());
                }
                // 8 - print stats
                else if (line.startsWith("8")) {
                    System.out.println("NOT IMPLEMENTED");//client.server.printStats());
                }
                // 9 <roomId> <additional roomId (optional)> <parentId or 0> - create message
                else if (line.startsWith("9")) {
                    String[] input = line.split(" ");
                    ArrayList<Chatroom> rooms = new ArrayList<>();
                    Long pid = Long.valueOf(input[input.length-1]); // parent id
                    for (int i = 1; i < input.length-2; i++)
                        rooms.add(client.server.getRemoteChatroom(Long.valueOf(input[i])));

                    System.out.println("Type a message...");
                    String contents = scanner.nextLine();
                    long mid = 0;                                   // message id
                    for (int j = 0; j < rooms.size() - 1; j++)
                        mid = rooms.get(j).createMessage(contents, pid, id);
                    System.out.println("Message was created with id " + mid +
                            " and sent to rooms " + rooms.toString());
                }
            }
        } catch (ChatException e) {
            System.out.printf("chat exception: %s%n", e.getMessage());
        }
    }
}