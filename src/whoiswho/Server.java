package whoiswho;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Server {
    private final int PORTNUMBER = 5555;
    private final int MAXREQUESTS = 10000;
    ExecutorService fixedPool = Executors.newFixedThreadPool(MAXREQUESTS);

    private LinkedBlockingQueue<PlayerServerHelper> playerList = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws IOException {
        Server myServer = new Server();
        myServer.start();

    }

    public void start() throws IOException {
        ServerSocket serverSocket;
        Socket clientSocket;
        int counter = 0;

        serverSocket = new ServerSocket(PORTNUMBER);
        if (serverSocket.isBound()) {
            System.out.println("Server is ready!");
        }


        while (serverSocket.isBound()) {
            clientSocket = serverSocket.accept();
            counter++;
            String temporaryName = "player" + counter;
            PlayerServerHelper aux = new PlayerServerHelper(temporaryName, clientSocket);
            playerList.offer(aux);
            aux.getOut().println("Waiting for your opponent...");


            synchronized (playerList) {
                if (playerList.size() >= 2) {
                    GameStart gameStart = new GameStart(playerList.poll(), playerList.poll());
                    fixedPool.submit(gameStart);

                }
            }
        }
    }

    /*
    -----------------------------------
    |  GAME START
    -----------------------------------
     */
    public class GameStart implements Runnable {
        List<PlayerServerHelper> players = Collections.synchronizedList(new ArrayList<>());

        public GameStart(PlayerServerHelper p1, PlayerServerHelper p2) {
            players.add(p1);
            players.add(p2);

        }

        @Override
        public void run() {
            System.out.println("Game Started");
            for (int i = 0; i < players.size(); i++) {
                players.get(i).setGameStart(this);
                fixedPool.submit(players.get(i));
            }
            players.get(0).setCurrentTurn(1);
        }
        public void sendToAll(String messageFromClient) {
            synchronized (players) {
                for (int i = 0; i < players.size(); i++) {
                    players.get(i).send(messageFromClient);
                }
            }
        }

    }
}


