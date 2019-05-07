package RemoteChat;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Sample client.
 */
public class ChatClient {

    private final ChatServer server;
    private static final String helpText =
            "1 <roomname> - create room\n" +
            "2 <userId> <roomId> - add user to room\n" +
            "3 <userId> <roomId> - remove user to room\n" +
            "4 <userId> <roomId> - block user to room\n" +
            "5 <roomId> - print contents of room\n" +
            "6 <roomId> <messageId> - like message\n" +
            "7 <roomId> <messageId> - dislike message\n" +
            "8 - print stats\n" +
            "9 <roomId> <additional roomId (optional)> <parentId or 0> - create message\n" +
            "10 <srcRoomID> <destRoomID> <msgID> - copy a message\n";

    private ChatClient(String service) throws RemoteException,
            NotBoundException, MalformedURLException {
        server = (ChatServer) java.rmi.Naming.lookup(service);
    }

    // ==================== METHODS TO HANDLE CLI INPUT ======================
    private void createRoomFromInput(String[] input) throws RemoteException {
        if (input.length < 2)
            throw new IllegalArgumentException("");

        StringBuilder roomName = new StringBuilder(" ");
        for (int i = 1; i < input.length; i++) {
            roomName.append(input[i]);
            roomName.append(" ");
        }
        roomName.deleteCharAt(roomName.length()-1);

        long uid = server.createChatRoom(roomName.toString());
        System.out.println("New ChatRoom made with name: " +
                roomName.toString() + " and id: " + uid);
    }

    private void addToRoomFromInput(String[] input) throws RemoteException {
        if (input.length != 3)
            throw new IllegalArgumentException("");

        User toAdd = server.getUser(Long.valueOf(input[1]));
        Chatroom room = server.getRemoteChatroom(Long.valueOf(input[2]));
        room.addUser(toAdd);
        System.out.println("User " + input[1] + " was added to room " +
                input[2]);
    }

    private void removeUserFromInput(String[] input) throws RemoteException {
        if (input.length != 3)
            throw new IllegalArgumentException("");

        User toRemove = server.getUser(Long.valueOf(input[1]));
        Chatroom room = server.getRemoteChatroom(Long.valueOf(input[2]));
        room.removeUser(toRemove);
        System.out.println("User " + input[1] + " was removed from room " +
                input[2]);
    }

    private void blockUserFromInput(String[] input) throws RemoteException {
        if (input.length != 3)
            throw new IllegalArgumentException("");

        User toBlock = server.getUser(Long.valueOf(input[1]));
        Chatroom room = server.getRemoteChatroom(Long.valueOf(input[2]));
        room.blockUser(toBlock);
        System.out.println("User " + input[1] + " was blocked from room " +
                input[2]);
    }

    private void printRoomFromInput(String[] input) throws RemoteException{
        if (input.length != 2)
            throw new IllegalArgumentException("");

        Chatroom room = server.getRemoteChatroom(Long.valueOf(input[1]));
        System.out.println(room.print());
    }

    private void likeMessageFromInput(String[] input) throws RemoteException {
        if (input.length != 3)
            throw new IllegalArgumentException("");

        Chatroom room = server.getRemoteChatroom(Long.valueOf(input[1]));
        RemoteMessage msg = room.getMessage(Long.valueOf(input[2]));
        msg.like();
        System.out.println("Message " + input[2] + " was liked in room " +
                input[1]);
    }

    private void dislikeMessageFromInput(String[] input) throws RemoteException {
        if (input.length != 3)
            throw new IllegalArgumentException("");

        Chatroom room = server.getRemoteChatroom(Long.valueOf(input[1]));
        RemoteMessage msg = room.getMessage(Long.valueOf(input[2]));
        msg.dislike();
        System.out.println("Message " + input[2] + " was disliked in room " +
                input[1]);
    }

    private void printStatsFromInput(String[] input, long id) throws RemoteException {
        if (input.length != 1)
            throw new IllegalArgumentException("");
        System.out.println(server.printStats(id));
    }

    private void createMessageFromInput(String[] input, Scanner scanner, long id) throws RemoteException{
        if (input.length < 3)
            throw new IllegalArgumentException("");

        ArrayList<Chatroom> rooms = new ArrayList<>();
        long pid = Long.valueOf(input[input.length-1]); // parent id
        for (int i = 1; i < input.length-1; i++)
            rooms.add(server.getRemoteChatroom(Long.valueOf(input[i])));

        System.out.println("Type a message...");
        String contents = scanner.nextLine();
        long mid = rooms.get(0).createMessage(contents, pid, id);
        RemoteMessage msg = rooms.get(0).getMessage(mid);
        for (int i = 1; i < rooms.size(); i++)
            rooms.get(i).addMessage(mid, msg);

        System.out.println("Message was created with id " + mid +
                " and sent to " + rooms.size() + " room(s)");
    }

    private void copyMessageFromInput(String[] input) throws RemoteException {
        if (input.length != 4)
            throw new IllegalArgumentException("");
        server.copyMessage(Long.valueOf(input[1]),
                Long.valueOf(input[2]),
                Long.valueOf(input[3]));
        System.out.println("Message " + input[3] + " was copied from room " +
                input[1] + " to room " + input[2]);
    }
    // ==================== END OF METHODS TO HANDLE CLI ====================

    public static void main(String[] args) throws Exception {
        String service = "rmi://127.0.0.1:" + 51350 + "/" + ChatServerApp.CHATROOM_NAME;

        ChatClient client = new ChatClient(service);
            System.out.println("Welcome to our ChatServer!");
            Scanner scanner = new Scanner(System.in);
            String line;

            System.out.println("What's your name?");
            String usrName = scanner.nextLine();
            long id = client.server.createUser(usrName);
            User user = client.server.getUser(id);
            System.out.println("Your name is " + user.name + " and id is "
                    + user.id);

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                String[] input = line.split(" ");

                try {
                // 10 <srcRoomID> <destRoomID> <msgID> - copy a message
                if(line.startsWith("10"))
                    client.copyMessageFromInput(input);
                // 1 <roomname> - create room
                else if (line.startsWith("1"))
                    client.createRoomFromInput(input);
                // 2 <userId> <roomId> - add user to room
                else if (line.startsWith("2"))
                    client.addToRoomFromInput(input);
                // 3 <userId> <roomId> - remove user to room
                else if (line.startsWith("3"))
                    client.removeUserFromInput(input);
                // 4 <userId> <roomId> - block user to room
                else if (line.startsWith("4"))
                    client.blockUserFromInput(input);
                // 5 <roomId> - print contents of room
                else if (line.startsWith("5"))
                    client.printRoomFromInput(input);
                // 6 <roomId> <messageId> - like message
                else if (line.startsWith("6"))
                    client.likeMessageFromInput(input);
                // 7 <roomId> <messageId> - dislike message
                else if (line.startsWith("7"))
                    client.dislikeMessageFromInput(input);
                // 8 - print stats
                else if (line.startsWith("8"))
                    client.printStatsFromInput(input, id);
                // 9 <roomId> <additional roomId (optional)> <parentId or 0> - create message
                else if (line.startsWith("9"))
                    client.createMessageFromInput(input, scanner, id);
                else if (line.startsWith("?"))
                    System.out.println(helpText);
            } catch (ChatException e) {
                    System.out.printf("chat exception: %s%n", e.getMessage());
                }
                catch (IllegalArgumentException e) {
                    System.out.println("illegal arguments! please retype command!");
                }
            }
    }
}