import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
           System.out.println("Connection is not there.");
           return false;
            // e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (ConnectException e)
        {
            System.out.println("error");
        }
        catch (IOException e) {
            System.out.println("IO Exception");
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) throws IOException {
        // Get proper arguments and override defaults
        Scanner s = new Scanner(System.in);
        ChatClient client = null;
            if (args.length == 0)
            {
                  client = new ChatClient("localhost", 1500, "CS 180 Student");
            }
            else if(args.length == 1)
            {
                client  = new ChatClient("localhost", 1500, args[0]);
            }
            else if(args.length == 2)
            {
                client  = new ChatClient("localhost", Integer.parseInt(args[1]), args[0]);
            }
            else if(args.length == 3)
            {
                client = new ChatClient(args[2], Integer.parseInt(args[1]), args[0]);
            }
            else if(args[0].equals("/logout") || args[1].equals("/logout") || args[2].equals("/logout"))
            {
                client.socket.close();
                client.sInput.close();
                client.sOutput.close();
            }
        // Create your client and start it
        client.start();
        while(true) {
            // Send an empty message to the server
            ChatMessage cm = new ChatMessage(0, s.nextLine());

            if (cm.getMessage().equals("/logout")) {
                client.disconnect();
                System.out.println(client.username + " disconnected.");
                break;
            } else if (cm.getMessage().equals("/list"))
            {
                cm = new ChatMessage(3, s.next());
                client.sendMessage(cm);
                break;
            }
            else  if (cm.getMessage().equals("/msg <" + client.username + "> <" + cm + ">"))
            {
                cm = new ChatMessage(2, s.nextLine());
                break;
            }
            else
            {
                client.sendMessage(cm);
            }
        }
    }
    private void disconnect() {
        try {
            if (sInput != null) {
                sInput.close();
            }
        }
            catch(Exception e)
            {
                e.printStackTrace();
            }


        try {
            if (sOutput != null) {
                sOutput.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try {
            if (socket != null) {
                socket.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        }




    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while (true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                } catch (IOException | ClassNotFoundException e) {
                    //e.printStackTrace();
                    //System.out.println(username + " disconnected.");
                    break;
                }

            }
        }
    }
}
