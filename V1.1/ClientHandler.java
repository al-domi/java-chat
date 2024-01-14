import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Group chat client handler that manages communication with clients
 * @author Alexander Domilescu
 */
public class ClientHandler implements Runnable {

    // class variables
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String clientUsername;

    /**
     * Creates a new client handler object to manage communication with one client
     * @param socket - The socket created by the server after connecting to a client
     */
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;

            // initialize the input and output streams
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());

            // client will send information regarding their username after connecting

            // read in the byte length of client username
            int clientUsernameLength = dataInputStream.readInt(); 
            // read in client username and set it
            byte[] clientUsernameBytes = new byte[clientUsernameLength];
            dataInputStream.readFully(clientUsernameBytes, 0, clientUsernameLength);
            this.clientUsername = new String(clientUsernameBytes);

            // add current client handler being built to the arraylist
            clientHandlers.add(this);

            // send out a connection message to all clients about who has connected
            broadcastText(clientUsername + " has entered the chat!");
        } catch (IOException e) {
            closeAll(socket, dataOutputStream, dataInputStream);
        }
    }

    /**
     * Method to listen for incoming messages from connected client
     * This method runs on a separate thread
     */
    public void run() {
        // create variables to hold information about incoming message
        int usernameLength = 0;
        byte[] usernameBytes = new byte[0];
        int incomingMessageLength = 0;
        byte[] incomingMessageBytes = new byte[0];
        int incomingFileNameLength = 0;
        byte[] incomingFileNameBytes = new byte[0];
        int incomingFileLength = 0;
        byte[] incomingFileBytes = new byte[0];

        // listen for messages while client is connected
        while(socket.isConnected()) {
            try {
                // read in length of client username
                usernameLength = dataInputStream.readInt();
                // read in the bytes of the client username
                usernameBytes = new byte[usernameLength];
                dataInputStream.readFully(usernameBytes, 0, usernameLength);

                // read in length of incoming message
                incomingMessageLength = dataInputStream.readInt();
                // check if there is a message being received
                if(incomingMessageLength > 0) {
                    // read in the message being received
                    incomingMessageBytes = new byte[incomingMessageLength];
                    dataInputStream.readFully(incomingMessageBytes, 0, incomingMessageLength);
                } else if (incomingMessageLength == 0) {
                    // received a message length of 0 which indicates that there was no message String sent
                    incomingMessageLength = 0;
                    incomingMessageBytes = new byte[0];
                }

                // read in length of incoming filename 
                incomingFileNameLength = dataInputStream.readInt();
                // check if there is a file being sent
                if(incomingFileNameLength > 0) {
                    // read in filename being received
                    incomingFileNameBytes = new byte[incomingFileNameLength];
                    dataInputStream.readFully(incomingFileNameBytes, 0, incomingFileNameLength);

                    // read in bytes of file being received
                    incomingFileLength = dataInputStream.readInt();
                    incomingFileBytes = new byte[incomingFileLength];
                    dataInputStream.readFully(incomingFileBytes, 0, incomingFileLength);
                } else if (incomingFileNameLength == 0) {
                    // received a filename length of 0 which indicates that there was no file sent
                    incomingFileNameLength = 0;
                    incomingFileNameBytes = new byte[0];
                    incomingFileLength = 0;
                    incomingFileBytes = new byte[0];
                }

                // broadcast information about the received message to all connected clients
                broadcast(usernameLength, usernameBytes, incomingMessageLength, incomingMessageBytes, incomingFileNameLength, incomingFileNameBytes, incomingFileLength, incomingFileBytes);
            } catch (IOException e) {
                break;
            }
        }
        
    }

    /**
     * Method to broadcast a string to all connected clients
     * @param text - The String to be broadcasted
     * @throws IOException
     */
    private void broadcastText(String text) {
        // get the bytes of the String
        byte[] textBytes = text.getBytes();

        try {
            // send a special integer so that the client knows that this is a text-only connection/disconnection message
            dataOutputStream.writeInt(Integer.MIN_VALUE);
            // send the number of bytes in the String
            dataOutputStream.writeInt(textBytes.length);
            // send the bytes of the String
            dataOutputStream.write(textBytes);
        } catch (IOException e) {
            closeAll(socket, dataOutputStream, dataInputStream);
        }
        
    }

    private void broadcast(int usernameLength, byte[] usernameBytes, int messageLength, byte[] messageBytes, int filenameLength, byte[] filenameBytes, int fileLength, byte[] fileBytes) {
        // loop through arraylist of client handlers and send the message received to all connected clients
        for(ClientHandler c : clientHandlers) {
            try {
                // check if current client handler in arraylist is the client who sent the message
                if(!c.clientUsername.equals(clientUsername)) {
                    // send message to all other clients
                    c.dataOutputStream.writeInt(usernameLength);
                    c.dataOutputStream.write(usernameBytes);
                    c.dataOutputStream.writeInt(messageLength);
                    c.dataOutputStream.write(messageBytes);
                    c.dataOutputStream.writeInt(filenameLength);
                    c.dataOutputStream.write(filenameBytes);
                    c.dataOutputStream.writeInt(fileLength);
                    c.dataOutputStream.write(fileBytes);
                }
            } catch (IOException e) {
                closeAll(socket, dataOutputStream, dataInputStream);
            }
        }
    }

    /**
     * Method to remove client handler
     */
    private void removeClientHandler() {
        broadcastText(clientUsername + " has left the chat!");
        clientHandlers.remove(this);
    }

    /**
     * Method to close connection to client and close streams
     * @param socket - The socket being closed
     * @param dataOutputStream - The data output stream being closed
     * @param dataInputStream - The data input stream being closed
     */
    private void closeAll(Socket socket, DataOutputStream dataOutputStream, DataInputStream dataInputStream) {
        // remove client from group
        removeClientHandler();
        try {
            // close streams and socket if they're not null
            if(dataOutputStream != null) {
                dataOutputStream.close();
            }
            if(dataInputStream != null) {
                dataInputStream.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
