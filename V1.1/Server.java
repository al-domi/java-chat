import java.io.IOException;
import java.net.*;

/**
 * Group chat server to handle client connection requests
 * @author Alexander Domilescu
 */
public class Server {
    
    // class variables
    private ServerSocket serverSocket;

    /**
     * Creates a new server object
     * @param serverSocket - The server socket used to listen for connections
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Starts server and keeps it running
     */
    private void runServer() {
        try {
            // print confirmation that server has started
            System.out.println("SERVER STARTUP SUCCESSFUL");

            // constantly run server until the server socket is closed
            while(!serverSocket.isClosed()) {
                // wait for a client to connect
                // create a socket to talk to the client once a connection is requested
                Socket socket = serverSocket.accept();
                System.out.println("NEW CLIENT CONNECTED");

                // create a new client handler to communicate with the client
                ClientHandler clientHandler = new ClientHandler(socket);

                // create a new thread to run the client handler in
                Thread thread = new Thread(clientHandler);
                // start the thread
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    /**
     * Error handling method that closes the server socket
     */
    private void closeServerSocket() {
        // an error has occurred and so we want to close the server socket
        try {
            if(serverSocket != null) {
                // close server socket if it isn't null
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method executed when program is ran
     * @param args - The command line arguments
     * @throws IOException - IO error when opening the server socket
     */
    public static void main(String[] args) throws IOException {
        // create a server that listens to connections on port 9999 and start running it
        ServerSocket serverSocket = new ServerSocket(9999);
        Server server = new Server(serverSocket);
        server.runServer();
    }
}