package RemoteChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
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

    private final ChatServer server;
    private Registry registry;
    private final Executor exec = Executors.newFixedThreadPool(50);

    /**
     * Creates a server for the given bank.
     */
    public ChatServerApp() {
        server = new LocalChatServer();
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
        if (port > 0) { // registry already exists
            reg = LocateRegistry.getRegistry(port);
        } else { // create on given port
            port = 12345;
            reg = LocateRegistry.createRegistry(port);
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

    private String serverUserMethod(String method, String arg){
        return "TODO";
    }

    private String handleChatRoom(Chatroom room, List<String> input) {
        return "TODO";
    }

    private String handleInput(String input){
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(input.split(";")));
        int i = 0;
        long clientId = Long.getLong(parts.remove(0));
        String serverMethodCall = parts.remove(0);
        String serverMethod = serverMethodCall.split(" ")[0];
        String serverArg = serverMethodCall.split(" ")[1];

        try {
            if (serverMethod.contains("User")) {
                return serverUserMethod(serverMethod, serverArg);
            } else {
                if (true) {
                    Long id = server.createChatRoom(serverArg);
                    return id.toString();
                } else if (serverMethod.equals("getRemoteChatroom")) {
                    var room = server.getRemoteChatroom(Long.getLong(serverArg));
                    return handleChatRoom(room, parts);
                }
            }
        } catch(RemoteException e){}

        return "TODO";
    }

    private void handleRequest(Socket connection){
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            String response = handleInput(in.readLine());
            //TODO: write response

        } catch(IOException e){
            System.err.println("failed to read from socket");
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
            System.err.println("Failed to open port");
            return;
        }

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
        int port = 0;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        LocalChatroom chatroom = new LocalChatroom(CHATROOM_NAME);
        ChatServerApp server = new ChatServerApp();
        try {
            port = server.startRMI(port);
            System.out.printf("server running on port %d%n", port);
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
