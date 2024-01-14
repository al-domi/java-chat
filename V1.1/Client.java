import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client implements Runnable, ActionListener, FocusListener {

    // class variables
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String username;
    private String textToSend = null;
    private File fileAttached = null;
    private File fileToSend = null;

    // message components
    private int usernameLength = 0;
    private byte[] usernameBytes;
    private int messageLength = 0;
    private byte[] messageBytes;
    private int filenameLength  = 0;
    private byte[] filenameBytes;
    private int fileLength  = 0;
    private byte[] fileBytes;

    // swing components
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout screens;

    // chat screen components
    private JPanel chatScreen;
    private JPanel chatView;
    private JScrollPane scrollPane;
    private JButton attachButton;
    private JButton sendButton;
    private JTextField messageField;
    private JLabel attachmentLabel;
    private JButton removeButton;
    private JFileChooser fileChooser;
    String messageHint = "Message...";

    // connection screen components
    private JPanel connectionScreen;
    private JLabel titleLabel;
    private JLabel promptLabel;
    private JTextField usernameField;
    private JButton connectButton;

    public void run() {
        // GUI assembly

        // create frame
        this.frame = new JFrame("Chat Client");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(400, 600);
        this.frame.setResizable(false);
        this.frame.setVisible(true);

        // chat screen - where the user will be able to send and receive messages
        this.chatScreen = new JPanel();
        this.chatScreen.setLayout(null);
        // create panel to hold sent messages
        this.chatView = new JPanel();
        this.chatView.setLayout(new BoxLayout(this.chatView, BoxLayout.Y_AXIS));
        // create scroll pane so user can see all past messages and always keep scrollbar visible
        this.scrollPane = new JScrollPane(this.chatView);
        this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.scrollPane.setBounds(10, 10, 376, 475);
        this.chatScreen.add(this.scrollPane);
        // create attach file button
        this.attachButton = new JButton("+");
        this.attachButton.setBounds(10, 485, 40, 40);
        this.attachButton.setFont(new Font("Arial", Font.PLAIN, 20));
        this.attachButton.setMargin(new Insets(1, 1, 1, 1));
        this.attachButton.addActionListener(this);
        this.attachButton.setActionCommand("attach file");
        this.attachButton.setEnabled(true);
        this.chatScreen.add(this.attachButton);
        // create send message button
        this.sendButton = new JButton(">");
        this.sendButton.setBounds(345, 485, 40, 40);
        this.sendButton.setFont(new Font("Arial", Font.PLAIN, 20));
        this.sendButton.setMargin(new Insets(1, 1, 1, 1));
        this.sendButton.addActionListener(this);
        this.sendButton.setActionCommand("send message");
        this.chatScreen.add(this.sendButton);
        // create message field
        this.messageField = new JTextField();
        this.messageField.setBounds(51, 485, 294, 41);
        this.messageField.setText(messageHint);
        this.messageField.addFocusListener(this);
        this.messageField.addActionListener(this);
        this.messageField.setActionCommand("send message");
        this.chatScreen.add(this.messageField);
        // create label to hold the name of the file attached
        this.attachmentLabel = new JLabel();
        this.attachmentLabel.setBounds(10, 527, 316, 40);
        this.chatScreen.add(this.attachmentLabel);
        // create button to remove attachments
        this.removeButton = new JButton("Remove");
        this.removeButton.setBounds(325, 537, 60, 20);
        this.removeButton.setFont(new Font("Arial", Font.BOLD, 12));
        this.removeButton.setMargin(new Insets(1, 1, 1, 1));
        this.removeButton.addActionListener(this);
        this.removeButton.setActionCommand("remove file");
        this.removeButton.setVisible(false);
        this.chatScreen.add(this.removeButton);

        // connection screen - where the user will enter their username and connect to the group chat
        this.connectionScreen = new JPanel();
        this.connectionScreen.setLayout(null);
        // create label to hold the title of the messaging app
        this.titleLabel = new JLabel("Group Chat", SwingConstants.CENTER);
        this.titleLabel.setBounds(0, 175, 400, 50);
        this.titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        this.connectionScreen.add(this.titleLabel);
        // create label to prompt user for their username
        this.promptLabel = new JLabel("Enter your username:", SwingConstants.CENTER);
        this.promptLabel.setBounds(0, 230, 400, 50);
        this.connectionScreen.add(this.promptLabel);
        // create text field where user can enter their username
        this.usernameField = new JTextField();
        this.usernameField.setBounds(100, 275, 200, 30);
        this.usernameField.addActionListener(this);
        this.usernameField.setActionCommand("connect");
        this.connectionScreen.add(this.usernameField);
        // create button to connect to group chat
        this.connectButton = new JButton("Connect");
        this.connectButton.setBounds(150, 335, 100, 30);
        this.connectButton.addActionListener(this);
        this.connectButton.setActionCommand("connect");
        this.connectionScreen.add(this.connectButton);

        // create a screen controller
        this.screens = new CardLayout();
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(screens);
        // add screens to panel controller
        this.mainPanel.add(this.connectionScreen, "connectionScreen");
        this.mainPanel.add(this.chatScreen, "chatScreen");
        // add panel controller to frame
        this.frame.add(this.mainPanel);
        // show connection screen on startup
        this.screens.show(this.mainPanel, "connectionScreen");
    }

    public void focusGained(FocusEvent e) {
        // user selected the message field
        if(this.messageField.getText().equals(messageHint)) {
            // empty the message field only if the default hint is present
            // this will not affect the field if it has a message
            this.messageField.setText("");
        }
    }

    public void focusLost(FocusEvent e) {
        // user deselected the message field
        if(this.messageField.getText().equals("")) {
            // set the message field to display the default hint if the user hasn't composed a message
            // this will not affect the field if it has a message
            this.messageField.setText(messageHint);
        }
    }

    public void actionPerformed(ActionEvent e) {
        // action has been performed
        
        // get command from action
        String command = e.getActionCommand();

        if(command.equals("connect")) {
            // user wants to connect to the group chat

            // get username bytes

            if(!this.usernameField.getText().equals("")) {
                this.username = this.usernameField.getText();
                this.usernameBytes = this.username.getBytes();
                this.usernameLength = this.usernameBytes.length;
            }

            // show the chat screen
            this.screens.show(this.mainPanel, "chatScreen");

        } else if(command.equals("attach file")) {
            // user wants to attach file

            // disable the attach file button (we don't want user attaching multiple files)
            this.attachButton.setEnabled(false);
            // create a JFileChooser
            this.fileChooser = new JFileChooser();
            this.fileChooser.setDialogTitle("Choose a file to send");

            // open the JFileChooser
            if(this.fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                // user has selected a file

                // return selected file, show a prompt with the file name, show attachment removal button
                this.fileAttached = this.fileChooser.getSelectedFile();
                this.attachmentLabel.setText("Attached file: " + this.fileAttached.getName());
                this.removeButton.setVisible(true);
            } else {
                // user did not select a file and closed the JFileChooser

                // enable the attach file button again
                this.attachButton.setEnabled(true);
            }
        } else if(command.equals("remove file")) {
            // clear the prompt label with the name of the attached file
            this.attachmentLabel.setText("");
            // hide the remove button
            this.removeButton.setVisible(false);
            // clear the file being held in the fileAttached variable
            this.fileAttached = null;
        } else if(command.equals("send message")) {
            // user wants to send their message

            // get the message and file that the user wants to send
            getMessageToSend();
            getFileToSend();

            try {
                // check if client is still connected to the server
                if(this.socket.isConnected()) {
                    // send username length
                    this.dataOutputStream.writeInt(this.usernameLength);
                    // send username bytes
                    this.dataOutputStream.write(this.usernameBytes);
                    // send message length
                    this.dataOutputStream.writeInt(this.messageLength);
                    // send message bytes
                    this.dataOutputStream.write(this.messageBytes);
                    // send filename length
                    this.dataOutputStream.writeInt(this.filenameLength);
                    // send filename bytes
                    this.dataOutputStream.write(this.filenameBytes);
                    // send file length
                    this.dataOutputStream.writeInt(this.fileLength);
                    // send file bytes
                    this.dataOutputStream.write(this.fileBytes);
                }
            } catch (IOException ioe) {
                closeAll(this.socket, this.dataOutputStream, this.dataInputStream);
            }

            // prepare the GUI to receieve the user's next message
            guiCleanup();
        }

    }

    /**
     * Creates a new client
     * @param socket - The socket used for communicating with the server
     * @param username - The client's username
     */
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.username = username;
        } catch (IOException e) {
            closeAll(socket, dataOutputStream, dataInputStream);
        }
    }

    /**
     * Method to listen to incoming messages and process them
     */
    private void messageListener() {
        // create new thread for the listener to run on
        // otherwise we would be stuck only sending messages
        new Thread(new Runnable() {
            /**
             * Run method that runs in a separate thread to listen for incoming messages
             */
            public void run() {
                while(socket.isConnected()) {
                    try {
                        // read in first sent integer
                        int firstReceivedInt = dataInputStream.readInt();

                        // check if message is a special connect/disconnect alert
                        if(firstReceivedInt == Integer.MIN_VALUE) {
                            int alertLength = dataInputStream.readInt();
                            byte[] alertBytes = new byte[alertLength];
                            dataInputStream.readFully(alertBytes, 0, alertLength);

                            String alert = new String(alertBytes);
                            createTextMessageSentBy("server alert", alert);
                        } else {
                            // message is not an alert

                            // read in sender's username
                            int senderUsernameLength = firstReceivedInt;
                            byte[] senderUsernameBytes = new byte[senderUsernameLength];
                            dataInputStream.readFully(senderUsernameBytes, 0, senderUsernameLength);
                            String senderUsername = new String(senderUsernameBytes);
                        
                            // read in length and content of sender's message
                            int incomingMessageLength = dataInputStream.readInt();
                            byte[] incomingMessageBytes = new byte[incomingMessageLength];
                            dataInputStream.readFully(incomingMessageBytes, 0, incomingMessageLength);
                            String incomingMessage = new String(incomingMessageBytes);

                            // check if incoming message has any length
                            if(incomingMessageBytes.length > 0) {
                                createTextMessageSentBy("server", incomingMessage);
                            }

                            // read in filename
                            int incomingFileNameLength = dataInputStream.readInt();
                            byte[] incomingFileNameBytes = new byte[incomingFileNameLength];
                            dataInputStream.readFully(incomingFileNameBytes, 0, incomingFileNameLength);
                            String incomingFileName = new String(incomingFileNameBytes);

                            // check if incoming filename has any length
                            if(incomingFileNameBytes.length > 0) {
                                createFileMessageSentBy("server", incomingFileName);
                            }

                            // read in file contents
                            int incomingFileLength = dataInputStream.readInt();
                            byte[] incomingFileBytes = new byte[incomingFileLength];
                            dataInputStream.readFully(incomingFileBytes, 0, incomingFileLength);
                        }
                    } catch (IOException e) {
                        // close connection and break out of loop if something goes wrong
                        closeAll(socket, dataOutputStream, dataInputStream);
                        break;
                    }
                }
                
            }

        }).start(); // actually start the thread
    }

    /**
     * Closes socket connection and streams
     * @param socket
     * @param dataOutputStream
     * @param dataInputStream
     */
    private void closeAll(Socket socket, DataOutputStream dataOutputStream, DataInputStream dataInputStream) {
        try {
            // close streams if they're not null
            if(dataOutputStream != null) {
                dataOutputStream.close();
            }
            if(dataInputStream != null) {
                dataInputStream.close();
            }
            // close socket if it isn't null
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the message the user wants to send
     */
    private void getMessageToSend() {
        // get the text the user wants to send
        if(this.messageField.getText().equals("") || this.messageField.getText().equals(messageHint)) {
            // user has not typed a message
            this.textToSend = null;

            // set message variables to null or 0
            // receiver will handle this
            this.messageBytes = new byte[0];
            this.messageLength = 0;
        } else {
            // user has typed in an actual message
            this.textToSend = this.messageField.getText();

            // get the length and bytes of the message String
            this.messageBytes = this.textToSend.getBytes();
            this.messageLength = this.messageBytes.length;

            // create a new message in the scroll pane with the message
            createTextMessageSentBy("me", this.textToSend);
        }
    }

    /**
     * Gets the file the user wants to send
     */
    private void getFileToSend() {
        // get the user's file attachment
        if(fileAttached != null) {
            // there is a file attached
            this.fileToSend = this.fileAttached;

            // get the length and bytes of the filename
            this.filenameBytes = this.fileToSend.getName().getBytes();
            this.filenameLength = this.filenameBytes.length;

            try {
                // create a file input stream to read in our file
                FileInputStream fileInputStream = new FileInputStream(this.fileToSend);

                // set up a byte array to hold the bytes of the file
                this.fileBytes = new byte[(int) this.fileToSend.length()];
                // read the bytes of the file into the array using our file input stream
                fileInputStream.read(this.fileBytes);
                // get the length of our file (in terms of bytes)
                this.fileLength = this.fileBytes.length;

                // close the file input stream for efficiency
                fileInputStream.close();

                // create a message in the scroll panel with the file information
                createFileMessageSentBy("me", this.fileToSend.getName());
            } catch (FileNotFoundException e) {
                // exception with the file input stream - didn't find the file
                closeAll(socket, dataOutputStream, dataInputStream);
            } catch (IOException e) {
                closeAll(socket, dataOutputStream, dataInputStream);
            }
        } else {
            // set all variables relating to sending a file to be null or 0
            // receiver will work this out on their end
            this.filenameBytes = new byte[0];
            this.filenameLength = 0;
            this.fileBytes = new byte[0];
            this.fileLength = 0;
        }
    }

    /**
     * Readies program to accept the user's next message by clearing variables associated with message info and resetting the GUI
     */
    private void guiCleanup() {
        // enable the attach file button, remove the file removal button and clear the prompt with the file name being sent
        this.removeButton.setVisible(false);
        this.attachButton.setEnabled(true);
        this.attachmentLabel.setText("");
        // clear all stored information about the message
        this.textToSend = null;
        this.fileAttached = null;
        this.fileToSend = null;

        this.messageLength = 0;
        this.messageBytes = null;
        this.filenameLength  = 0;
        this.filenameBytes = null;
        this.fileLength  = 0;
        this.fileBytes = null;

        // clear the message field appropriately based on what the user's cursor is focused on
        if(this.messageField.hasFocus()) {
            // user is focused on the message field
            // clear it completely for user to write another message in
            this.messageField.setText("");
        } else {
            // user is focused on another component
            // set the message field to its default message hint
            this.messageField.setText(messageHint);
        }
    }

    /**
     * Creates a label to hold a text message
     * @param sender - The sender of the message
     * @param text - The text message being sent
     */
    private void createTextMessageSentBy(String sender, String text) {
        // create a new panel which will hold another panel with the message
        JPanel fileRow = new JPanel();
        fileRow.setLayout(new BoxLayout(fileRow, BoxLayout.Y_AXIS));

        // create label to hold the message
        JLabel messageLabel = new JLabel(text);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        messageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // check who the sender of the message is
        if(sender.equals("me")) {
            // format client's messages on the right of the window
            messageLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            fileRow.setBackground(Color.GREEN);
        } else if(sender.equals("server")) {
            // format other client's messages on the left of the window
            messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            fileRow.setBackground(Color.LIGHT_GRAY);
        } else if(sender.equals("server alert")) {
            // center incoming connection/disconnection messages
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        
        // add the label to the scroll panel
        fileRow.add(messageLabel);
        chatView.add(fileRow);
        // refresh the scroll panel to show the new message
        frame.validate();
    }

    /**
     * Creates a label to hold a file message
     * @param sender - The sender of the message
     * @param filename - The name of the file being sent
     */
    private void createFileMessageSentBy(String sender, String filename) {
        // create a new panel which will hold another panel with the filename
        JPanel fileRow = new JPanel();
        fileRow.setLayout(new BoxLayout(fileRow, BoxLayout.Y_AXIS));

        // create label to hold filename
        JLabel messageLabel = new JLabel(filename);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 15));
        messageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // check who the sender of the message is
        if(sender.equals("me")) {
            // format client's messages on the right of the window
            messageLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            fileRow.setBackground(Color.GREEN);
        } else if(sender.equals("server")) {
            // format other client's messages on the left of the window 
            messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            fileRow.setBackground(Color.LIGHT_GRAY);
        }

        // add the label to the scroll panel
        fileRow.add(messageLabel);
        chatView.add(fileRow);
        // refresh the scroll panel to show the new message
        frame.validate();
    }

    /**
     * Method executed when program is ran
     * @param args - The command line arguments
     * @throws IOException
     * @throws UnknownHostException - Server IP address could not be found
     */
    public static void main(String[] args) throws UnknownHostException, IOException {
        // create socket to connect to group chat
        Socket socket = new Socket("localhost", 9999);
        Client gui = new Client(socket, "");

        // create an instance of our program and start it in event thread
        SwingUtilities.invokeLater(gui);
        // start listening for messages
        gui.messageListener();
    }
    
}