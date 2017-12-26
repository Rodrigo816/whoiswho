package whoiswho;

import java.io.*;
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
    private List<Player> playerList = Collections.synchronizedList(new ArrayList());
    private String[] names = {"Angelo", "Brandão","Luís F.","Davide",
                              "André", "César", "João S.","Amélia",
                              "João Martins","Sofia","Rodrigo B.",
                              "Renata","Luís S.","Toste","Francisco",
                              "Leandro","Soraia","Lobão","Rodrigo D.","Dário"};


    public static void main(String[] args) throws IOException {
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
            if(playerList.size()==2){
                GameStart gameStart = new GameStart(playerList.get(0),playerList.get(1));


                fixedPool.submit(gameStart);
            }




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
                try {
                    mensageFromClient = in.readLine();
                    temporyName = mensageFromClient;
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            try {
                playerList.add(new Player(temporyName,names,clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Player helper
    private class Player {
        private String name;
        private String[] names;
        private String nameHolder;
        private BufferedReader reader;
        private PrintWriter writer;

        private Socket socket;

        public Player(String name, String[] names,Socket socket) throws IOException {
            this.name=name;
            this.names=names;
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));


        }




    }

    // GameStart
    public class GameStart implements Runnable{

        private final Player player1;
        private final Player player2;

        public GameStart(Player p1, Player p2){
            this.player1 = p1;
            this.player2 = p2;

        }
        @Override
        public void run() {
          player1.writer.println("Hello stranger, WTF you doin here ? go home!");
          player2.writer.println("You arePlayer 2");




        }
    }
}
