package whoiswho;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    private final int PORTNUMBER = 5555;
    private final String HOSTNAME = "localhost";
    private boolean sendText = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.connect();
    }

    public void connect () {

        Socket clientSocket;

        try {

            clientSocket = new Socket(HOSTNAME, PORTNUMBER);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
            singleExecutor.submit(new ClientHelper(clientSocket));

            while (!clientSocket.isClosed()){
                String serverMessage = in.readLine();
                if (serverMessage.equals("[Server:] Game Started")) {  //The game started when the 2 players have picked a character and inserted the user name
                    sendText = true;
                }
                if (serverMessage.contains("You picked ")) {  //The second time is when choose the character. Then don't send until the game started
                    sendText = false;
                }
                if (serverMessage.equals("Insert your username: ")) { //The first time client send text to server is when he inserts his user name
                    sendText = true;
                }
                System.out.println(serverMessage);
            }

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ClientHelper implements Runnable {

        private Socket clientSocket;

        ClientHelper(Socket clientSocket){
            this.clientSocket=clientSocket;
        }

        @Override
        public void run() {
            Scanner myScanner = new Scanner(System.in);
            PrintWriter out = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!clientSocket.isClosed()){

                String myLine = myScanner.nextLine();
                if (sendText) {
                    out.println(myLine);
                }
            }
        }
    }
}
