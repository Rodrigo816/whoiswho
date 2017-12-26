package whoiswho;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final int PORTNUMBER = 5555;
    final int MAXREQUESTS = 10000;
    private List<Player> arrayThePlayers = Collections.synchronizedList(new ArrayList());
    private String[] names = {"Angelo", "Brandão","Luís F.","Davide",
                              "André", "César", "João S.","Amélia",
                              "João Martins","Sofia","Rodrigo B.",
                              "Renata","Luís S.","Toste","Francisco",
                              "Leandro","Soraia","Lobão","Rodrigo D.","Dário"};


    public static void main(String[] args) {
        Server myServer = new Server();
        myServer.start();
    }


    public void start() throws IOException {

        ServerSocket serverSocket;
        Socket clientSocket;


        serverSocket = new ServerSocket(PORTNUMBER);
        if (serverSocket.isBound()) {
            System.out.println("Server is ready!");
        }
        ExecutorService fixedPool = Executors.newFixedThreadPool(MAXREQUESTS);

        while (serverSocket.isBound()) {
            clientSocket = serverSocket.accept();
            RegisterPlayer registerPlayer = new RegisterPlayer(clientSocket);
            fixedPool.submit(registerPlayer);

            // vereficar sem tens dois player e criar navo thread



        }
    }

    // Register the player class
    public class RegisterPlayer implements Runnable{
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private String temporyName;

        RegisterPlayer(Socket clientSocket) throws IOException {
            this.clientSocket=clientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }

        @Override
        public void run() {
            System.out.println("Connection successful from IP: " + clientSocket.getLocalAddress().getHostAddress() + " Port: "+ clientSocket.getLocalPort());
            String mensageFromClient;

            out.println("Please enter your name NIGGAAAAAA:");
            while (!clientSocket.isClosed() && temporyName==null){
                mensageFromClient = in.readLine();
                temporyName = mensageFromClient;

            }
            arrayThePlayers.add(new Player(temporyName,names));
        }
    }

    // Player helper
    private class Player {
        private String name;
        private String[] names;
        private String nameHolder;

        public Player(String name, String[] names){
            this.name=name;
            this.names=names;
        }
    }

    // GameStart
    public class GameStart implements Runnable{

        @Override
        public void run() {

        }
    }
}
