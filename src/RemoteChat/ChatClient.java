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

    private final Chatroom chatroom;
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

    private ChatClient(String service) throws RemoteException, NotBoundException, MalformedURLException {
        server = (ChatServer) java.rmi.Naming.lookup(service);
        chatroom = new LocalChatroom("Client's Room");
    }

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

                try {
                // 10 <srcRoomID> <destRoomID> <msgID>
                if(line.startsWith("10")) {
                    String[] input = line.split(" ");
                    if (input.length != 4)
                        throw new IllegalArgumentException("");
                    client.server.copyMessage(Long.valueOf(input[1]),
                            Long.valueOf(input[2]),
                            Long.valueOf(input[3]));
                    System.out.println("Message " + input[3] + " was copied from room " +
                            input[1] + " to room " + input[2]);
                }
                // 1 <roomname> - create room
                else if (line.startsWith("1")) {
                    String[] input  = line.split(" ");

                    if (input.length != 2)
                        throw new IllegalArgumentException("");

                    long uid = client.server.createChatRoom(input[1]);
                    System.out.println("New ChatRoom made with name: " +
                            input[1] + " and id: " + uid);
                }
                // 2 <userId> <roomId> - add user to room
                else if (line.startsWith("2")) {
                    String[] input = line.split(" ");

                    if (input.length != 3)
                        throw new IllegalArgumentException("");

                    User toAdd = client.server.getUser(Long.valueOf(input[1]));
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[2]));
                    room.addUser(toAdd);
                    System.out.println("User " + input[1] + " was added to room " +
                            input[2]);
                }
                // 3 <userId> <roomId> - remove user to room
                else if (line.startsWith("3")) {
                    String[] input = line.split(" ");

                    if (input.length != 3)
                        throw new IllegalArgumentException("");

                    User toRemove = client.server.getUser(Long.valueOf(input[1]));
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[2]));
                    room.removeUser(toRemove);
                    System.out.println("User " + input[1] + " was removed from room " +
                            input[2]);
                }
                // 4 <userId> <roomId> - block user to room
                else if (line.startsWith("4")) {
                    String[] input = line.split(" ");

                    if (input.length != 3)
                        throw new IllegalArgumentException("");

                    User toBlock = client.server.getUser(Long.valueOf(input[1]));
                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[2]));
                    room.blockUser(toBlock);
                    System.out.println("User " + input[1] + " was blocked from room " +
                            input[2]);
                }
                // 5 <roomId> - print contents of room
                else if (line.startsWith("5")) {
                    String[] input = line.split(" ");

                    if (input.length != 2)
                        throw new IllegalArgumentException("");

                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[1]));
                    System.out.println(room.print());
                }
                // 6 <roomId> <messageId> - like message
                else if (line.startsWith("6")) {
                    String[] input = line.split(" ");

                    if (input.length != 3)
                        throw new IllegalArgumentException("");

                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[1]));
                    RemoteMessage msg = (RemoteMessage) room.getMessage(Long.valueOf(input[2]));
                    msg.like();
                    System.out.println("Message " + input[2] + " was liked in room " +
                            input[1]);
                }
                // 7 <roomId> <messageId> - dislike message
                else if (line.startsWith("7")) {
                    String[] input = line.split(" ");

                    if (input.length != 3)
                        throw new IllegalArgumentException("");

                    Chatroom room = client.server.getRemoteChatroom(Long.valueOf(input[1]));
                    RemoteMessage msg = (RemoteMessage) room.getMessage(Long.valueOf(input[2]));
                    msg.dislike();
                    System.out.println("Message " + input[2] + " was disliked in room " +
                            input[1]);
                }
                // 8 - print stats
                else if (line.startsWith("8")) {
                    String[] input = line.split(" ");

                    if (input.length != 1)
                        throw new IllegalArgumentException("");

                    System.out.println(client.server.printStats(id));
                }
                // 9 <roomId> <additional roomId (optional)> <parentId or 0> - create message
                else if (line.startsWith("9")) {
                    String[] input = line.split(" ");

                    if (input.length < 3)
                        throw new IllegalArgumentException("");

                    ArrayList<Chatroom> rooms = new ArrayList<>();
                    long pid = Long.valueOf(input[input.length-1]); // parent id
                    for (int i = 1; i < input.length-1; i++)
                        rooms.add(client.server.getRemoteChatroom(Long.valueOf(input[i])));

                    System.out.println("Type a message...");
                    String contents = scanner.nextLine();
                    long mid = 0;                                   // message id

                    mid = rooms.get(0).createMessage(contents, pid, id);
                    RemoteMessage msg = rooms.get(0).getMessage(mid);
                    for (int i = 1; i < rooms.size(); i++)
                        rooms.get(i).addMessage(mid, msg);

                    System.out.println("Message was created with id " + mid +
                            " and sent to " + rooms.size() + " room(s)");
                }
                else if (line.startsWith("?")) {
                    System.out.println(helpText);
                }
            } catch (ChatException e) {
                    System.out.printf("chat exception: %s%n", e.getMessage());
                }
                catch (IllegalArgumentException e) {
                    System.out.println("illegal arguments! please retype command!");
                }
            }
    }
}