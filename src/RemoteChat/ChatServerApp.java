package RemoteChat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class ChatServerApp {
    public static final String CHATROOM_NAME = "CS735_PROJECT_ROOM";

    private volatile ChatServer server;
    private Registry registry;
    private final Executor exec = Executors.newFixedThreadPool(50);

    /**
     * Creates a server for the given bank.
     */
    public ChatServerApp() {
        try {
            server = new LocalChatServer();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server by binding it to a registry.
     *
     * <ul>
     *
     * <li>If {@code port} is positive, the server attempts to locate a registry at this port.</li>
     *
     * <li>If {@code port} is negative, the server attempts to startRMI a new registry at this
     * port.</li>
     *
     * <li>If {@code port} is 0, the server attempts to startRMI a new registry at a randomly chosen
     * port.</li>
     *
     * </ul>
     *
     * @return the registry port
     */
    public synchronized int startRMI(int port) throws RemoteException {
        if (registry != null)
            System.err.println("Error server is already running on port: " + port);
        Registry reg;
        try {
        reg = LocateRegistry.createRegistry(port);
        } catch(Exception e){
            System.err.println("Failed to open port " + port);
            return -1;
        }
        reg.rebind(CHATROOM_NAME, server);
        registry = reg;
        return port;
    }

    /**
     * Stops the server by removing the bank form the registry.  The bank is left exported.
     */
    public synchronized void stopRMI() {
        if (registry != null) {
            try {
                registry.unbind(CHATROOM_NAME);
            } catch (Exception e) {
                Logger.getLogger("cs735_835").warning(String.format("unable to stopRMI: %s%n", e.getMessage()));
            } finally {
                registry = null;
            }
        }
    }

    private String serverUserMethod(String method, String arg) throws RemoteException {
        if(method.equals("getUser")) {
            var userId = Long.valueOf(arg);
            User user = server.getUser(userId);
            String userStr = "id:" + user.id + ";name:" + user.name;
            return userStr;
        } else if(method.equals("createUser")) {
            Long id = server.createUser(arg);
            return "ID:" + id.toString();
        } else if(method.equals("deleteUser")){
            var userId = Long.valueOf(arg);
            String deleted = server.deleteUser(userId);
            return "DELETED:" + deleted;
        }

        return "ERROR:Could not parse";
    }

    private String handleChatRoom(Chatroom room, List<String> input) throws RemoteException {
        String methodCall = input.remove(0);
        var methodParts = new ArrayList<>(Arrays.asList(methodCall.split(":")));
        String methodName = methodParts.remove(0);
        var methodArgs = methodParts;

        long userId = -1;
        try {
            userId = Long.valueOf(methodArgs.remove(0));
        } catch(Exception e){}
        Long retLong = null;

        if(methodName.equals("addUser")){
            User user = server.getUser(userId);
            retLong = room.addUser(user);
        } else if(methodName.equals("removeUser")){
            User user = server.getUser(userId);
            retLong = room.removeUser(user);
        } else if(methodName.equals("blockUser")){
            User user = server.getUser(userId);
            retLong =  room.blockUser(user);
        } else if(methodName.equals("getUser")){
            User user = server.getUser(userId);
            String userStr = "id:" + user.id + ";name:" + user.name;
            return userStr;
        } else if(methodName.equals("createMessage")){
            long parentId = Long.valueOf(methodArgs.remove(0));
            StringBuffer sb = new StringBuffer();
            while(!methodArgs.isEmpty())
                sb.append(methodArgs.remove(0));
            retLong = room.createMessage(sb.toString(), parentId, userId);
        } else if(methodName.equals("getMessage")) {
            return room.getMessage(userId).toString();
        } else if(methodName.equals("getMessages")) {
            return room.print();
        } else if(methodName.equals("likeMessage")) {
            Integer likes = room.getMessage(userId).like();
            return "LIKES:" + likes.toString();
        } else if(methodName.equals("dislikeMessage")) {
        Integer likes = room.getMessage(userId).dislike();
        return "LIKES:" + likes.toString();
        }

        assert(retLong != null);
        return "ID:" + retLong.toString();
    }

    private String handleInput(String input){
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(input.split(";")));
        var client_string = parts.remove(0);
        long clientId = Long.valueOf(client_string);
        String serverMethodCall = parts.remove(0);
        var serverMethodParts = new ArrayList<>(Arrays.asList(serverMethodCall.split(":")));
        String serverMethod = serverMethodParts.remove(0);
        var serverArgs = serverMethodParts;

        try {
            if (serverMethod.contains("User")) {
                return serverUserMethod(serverMethod, serverArgs.remove(0));
            } else if (serverMethod.equals("createChatRoom")) {
                Long id = server.createChatRoom(serverArgs.remove(0));
                return "ID:" + id.toString();
            } else if (serverMethod.equals("printStats")){
                var userId = Long.valueOf(serverArgs.remove(0));
                return "STATS:" + server.printStats(userId);
            } else if (serverMethod.equals("getRemoteChatroom")) {
                var room = server.getRemoteChatroom(Long.valueOf(serverArgs.remove(0)));
                return handleChatRoom(room, parts);
            }
        } catch(Exception e){
            return "EXCEPTION:" + e.getMessage();
        }

        return "ERROR:Could not parse";
    }

    private void handleRequest(Socket connection){
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );
            String input = in.readLine();
            String response = handleInput(input);
            PrintWriter writer = new PrintWriter(connection.getOutputStream(), true);
            writer.println(response);
            writer.flush();
        } catch(IOException e){
            System.err.println("failed to read or write from socket");
        }
    }

    /**
     * socket server loop
     */
    public synchronized void startSockets(int port){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException e){
            Random rand = new Random();
            int tries = 0;
            while (true) {
                port = 50000 + rand.nextInt(10000);
                try {
                    serverSocket = new ServerSocket(port);
                } catch (IOException ioe) {
                    if (++tries > 10) {
                        System.err.println("Failed to open port");
                        return;
                    } else
                        break;
                }
            }

        }

        System.out.println("Socket Server running on port: " + port);

        while(true){
            try {
                Socket connection = serverSocket.accept();
                exec.execute(() -> handleRequest(connection));
            } catch(IOException e){
                System.err.println("failed to accept connection");
            }
        }

    }

    /**
     * Command-line program.  Single (optional) argument is a port number (see {@link #startRMI(int)}).
     */
    public static void main(String[] args) throws Exception {
        int port = 51350;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        LocalChatroom chatroom = new LocalChatroom(CHATROOM_NAME);
        ChatServerApp server = new ChatServerApp();
        try {
            port = server.startRMI(port);
            System.out.printf("server running on port %d%n", port);
            server.startSockets(port+1);
            ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        } catch (RemoteException e) {
            Throwable t = e.getCause();
            if (t instanceof java.net.ConnectException)
                System.err.println("unable to connect to registry: " + t.getMessage());
            else if (t instanceof java.net.BindException)
                System.err.println("cannot startRMI registry: " + t.getMessage());
            else
                System.err.println("cannot startRMI server: " + e.getMessage());
            UnicastRemoteObject.unexportObject(chatroom, false);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::stopRMI));
    }
}
