

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private ChatFilter wordCheck;


    private ChatServer(int port, String words_to_filter) {
        this.port = port;
        wordCheck = new ChatFilter(words_to_filter);
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {

            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while(true) {
                    Socket socket = serverSocket.accept();
                    Runnable r = new ClientThread(socket, uniqueId++);
                    Thread t = new Thread(r);
                    clients.add((ClientThread) r);
                    t.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



    private synchronized void broadcast(String message) {
        Date date = new Date();
        SimpleDateFormat pattern = new SimpleDateFormat("HH:mm:ss");
        String time = pattern.format(date);


        message = wordCheck.filter(message);


        for (int i = 0; i < clients.size(); i++) {
            ClientThread y = clients.get(i);
            y.writeMessage("> " + time + " " + message + "\n");  //does it keep spaces??
                 //print msg to server?
        }
        System.out.println(time + " "+ message);
    }

    private synchronized void remove(int id) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread x = clients.get(i);
            if (x.id == id) {
                clients.remove(i);
                return;     //check if needed
            }
        }
    }


    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server = null;
        if (args.length == 0) {
            server = new ChatServer(1500,null);
        } else if (args.length == 1) {
            server = new ChatServer(Integer.parseInt(args[0]),null);
        }
        server.start();
                // if chatclient disconnects
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                /*for(int i = 0; i < clients.size(); i++)
                {
                    String str = clients.get(i).username;
                    if(str.equals(username))
                    {
                        System.out.println("Client with this username already exists");
                        clients.get(i).close();
                        break;
                    }
                }*/
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private boolean writeMessage(String msg) {
            if (!socket.isConnected()) {
                return false;
            }
            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }


        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            boolean logout = true;
            while (logout) {
                // Read the username sent to you by client
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(username + " log out");
                    break;
                    //e.printStackTrace();
                }
                System.out.println(username);


                // Send message back to the client
                try {
                    sOutput.writeObject("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String message = cm.getMessage();

                switch (cm.getType()) {

                    case ChatMessage.GENMESSAGE:
                        broadcast(username + ": " + message);
                        break;

                    case ChatMessage.LOGOUT:
                        broadcast(username + ": disconnected");
                        break;

                    case ChatMessage.directMessage:
                            directMessage(message + ": " + cm, username);
                            break;

                    case ChatMessage.displayList:
                        list();
                        break;



                }
            }
        }


        private void close() {
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

        private synchronized void directMessage(String message, String username) {
            Date date = new Date();
            SimpleDateFormat pattern = new SimpleDateFormat("HH:mm:ss");
            String time = pattern.format(date);


            message = wordCheck.filter(message);
            for (int i = 0; i < clients.size(); i++) {
                if (cm.getRecipient().equals(username)) {
                    ClientThread cl = clients.get(i);
                    cl.writeMessage("> " + time + " " + message + "\n");
                    break;
                }
            }

        }
        private synchronized void list() {
            ClientThread dk = null;
            for (int i = 0; i < clients.size(); i++) {
                dk = clients.get(i);
                if (dk.writeMessage("/list")) {
                    clients.remove(i);
                    break;
                }
            }

                    for (int j = 0; j < clients.size(); j++)
                    {
                        dk.writeMessage(clients.get(j).username);
                    }

                }
            }
        }



