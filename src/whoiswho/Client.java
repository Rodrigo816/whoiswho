package whoiswho;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    private final int PORTNUMBER = 5555;
    private final String HOSTNAME = "localhost";
    private Scanner myScanner;
    private ClientMenu menu;
    private boolean sendText = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.initMenu();
    }

    public Client () {
        this.menu = new ClientMenu();
        menu.initialScreen();

    }

    public void initMenu() {

        myScanner = new Scanner(System.in);
        menu.setScanner(myScanner);

        while(!menu.isGameStartSelected()) {
            System.out.print("\033[H\033[2J");
            try {
                   menu.menuInit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connect();

    }

    public void connect () {

        Socket clientSocket;

        try {

            clientSocket = new Socket(HOSTNAME, PORTNUMBER);
            menu.setGameStartSelected(false);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
            singleExecutor.submit(new ClientHelper(clientSocket));

            while (!clientSocket.isClosed()){
                String serverMessage = in.readLine();

                if (serverMessage.contains("won the game")) {
                    sendText = false;
                    System.out.println(serverMessage);
                    in.close();
                    clientSocket.close();
                    break;
                }

                if (serverMessage.equals("[Server:] Game Started")) {  //The game started when the 2 players have picked a character and inserted the user name
                    sendText = true;
                }
                if (serverMessage.contains("You picked ")) {  //The second time is when choose the character. Then don't send until the game started
                    sendText = false;
                }
                if (serverMessage.equals("Insert your username: ")) { //The first time client send text to server is when insert username
                    sendText = true;
                }
                System.out.println(serverMessage);
            }

            initMenu();


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

            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                while (!clientSocket.isClosed()){

                    String myLine = myScanner.nextLine();
                    if (sendText) {
                        out.println(myLine);
                    }
                }
                out.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
