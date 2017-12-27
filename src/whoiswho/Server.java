package whoiswho;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;

public class Server {
    private final int PORTNUMBER = 5555;
    private final int MAXREQUESTS = 10000;
    ExecutorService fixedPool = Executors.newFixedThreadPool(MAXREQUESTS);


    private LinkedBlockingQueue<Player> playerList = new LinkedBlockingQueue<>();

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
            String temporaryName = "player"+counter;
            Player aux = new Player(temporaryName, clientSocket);
            playerList.offer(aux);
            System.out.println(temporaryName);

            synchronized (playerList) {
                if (playerList.size() >= 2) {
                    GameStart gameStart = new GameStart(playerList.poll(), playerList.poll());
                    fixedPool.submit(gameStart);

                }

            }

         }

    }

    // Player helper
    private class Player implements Runnable{
        private String name;
        private String[] names = {"Angelo", "Brandão","Luís F","Davide", "André",
                                "César", "João S","Amélia", "João M","Sofia",
                                "Rodrigo B", "Renata","Luís S","Toste","Francisco",
                                "Leandro","Soraia","Lobão","Rodrigo D","Dário",
                                "Ferrão", "Catarina", "Sérgio", "Audrey", "Faustino"};
        private Characters[] characters = new Characters[names.length];
        private String nameHolder;
        private boolean init = false;
        private BufferedReader reader;
        private PrintWriter writer;
        private Socket socket;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.pcs.removePropertyChangeListener(listener);
        }

        public Player(String name, Socket socket) throws IOException {
            this.name = name;
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            for (int i = 0; i < characters.length; i++) {
                characters[i] = new Characters(names[i]);
            }
        }


        @Override
        public synchronized void run() {
            writer.println("Insert your name: ");
            String messageFromClient;

            try {
                messageFromClient = reader.readLine();
                name = messageFromClient;


                    writer.println("Characters:");
                    for (int i = 0; i < characters.length; i++) {
                        writer.println((i+1) + ": " + characters[i].getName());
                    }
                    writer.println("Please pick your character's number:");
                    String choice = reader.readLine();
                    int number = Integer.parseInt(choice);
                    nameHolder = characters[number-1].getName();
                    writer.println("You picked " + nameHolder + ".");
                    init = true;


            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        public boolean isInit() {
            return init;
        }



    }

    // GameStart
    public class GameStart implements Runnable, PropertyChangeListener{

        private final Player player1;
        private final Player player2;

        public GameStart(Player p1, Player p2){
            this.player1 = p1;
            this.player2 = p2;

        }

        @Override
        public void run() {
            fixedPool.submit(player1);
            fixedPool.submit(player2);



        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            System.out.println("entrou");
            System.out.println(evt.getPropertyName());

        }
    }
}
