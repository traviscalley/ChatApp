package RemoteChat;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class ChatServerApp
{
    public static final String CHATROOM_NAME = "CS735_PROJECT_ROOM";

    private final ChatServer server;
    private Registry registry;

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
     * <li>If {@code port} is negative, the server attempts to start a new registry at this
     * port.</li>
     *
     * <li>If {@code port} is 0, the server attempts to start a new registry at a randomly chosen
     * port.</li>
     *
     * </ul>
     *
     * @return the registry port
     */
    public synchronized int start(int port) throws RemoteException {
        if (registry != null)
            System.err.println("Error server is already running on port: " + port);
        Registry reg;
        if (port > 0) { // registry already exists
            reg = LocateRegistry.getRegistry(port);
        } else if (port < 0) { // create on given port
            port = -port;
            reg = LocateRegistry.createRegistry(port);
        } else { // create registry on random port
            Random rand = new Random();
            int tries = 0;
            while (true) {
                port = 50000 + rand.nextInt(10000);
                try {
                    reg = LocateRegistry.createRegistry(port);
                    break;
                } catch (RemoteException e) {
                    if (++tries < 10 && e.getCause() instanceof java.net.BindException)
                        continue;
                    throw e;
                }
            }
        }
        reg.rebind(CHATROOM_NAME, server);
        registry = reg;
        return port;
    }

    /**
     * Stops the server by removing the bank form the registry.  The bank is left exported.
     */
    public synchronized void stop() {
        if (registry != null) {
            try {
                registry.unbind(CHATROOM_NAME);
            } catch (Exception e) {
                Logger.getLogger("cs735_835").warning(String.format("unable to stop: %s%n", e.getMessage()));
            } finally {
                registry = null;
            }
        }
    }

    /**
     * Command-line program.  Single (optional) argument is a port number (see {@link #start(int)}).
     */
    public static void main(String[] args) throws Exception {
        int port = 0;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        LocalChatroom chatroom = new LocalChatroom(CHATROOM_NAME);
        ChatServerApp server = new ChatServerApp();
        try {
            port = server.start(port);
            System.out.printf("server running on port %d%n", port);
            ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        } catch (RemoteException e) {
            Throwable t = e.getCause();
            if (t instanceof java.net.ConnectException)
                System.err.println("unable to connect to registry: " + t.getMessage());
            else if (t instanceof java.net.BindException)
                System.err.println("cannot start registry: " + t.getMessage());
            else
                System.err.println("cannot start server: " + e.getMessage());
            UnicastRemoteObject.unexportObject(chatroom, false);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
