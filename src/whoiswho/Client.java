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
                if (serverMessage.equals("[Server:] Game Started.")) {
                    sendText = true;
                }
                if (serverMessage.contains("You picked ")) {
                    sendText = false;
                }
                if (serverMessage.equals("Insert your user name: ")) {
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
