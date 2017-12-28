package whoiswho;
import java.io.*;
import java.net.Socket;

public class PlayerServerHelper implements Runnable {
    private String name;
    private String[] names = {"Ângelo", "Brandão", "Luís F", "Davide", "André",
            "César", "João S", "Amélia", "João M", "Sofia",
            "Rodrigo B", "Renata", "Luís S", "Toste", "Francisco",
            "Leandro", "Soraia", "Lobão", "Rodrigo D", "Dário",
            "Ferrão", "Catarina", "Sérgio", "Audrey", "Faustino"};
    private Characters[] characters = new Characters[names.length];
    private String nameHolder;
    private boolean init = false;
    private BufferedReader in;
    private PrintWriter out;
    Server.GameStart gameStart;
    private Socket socket;
    private int currentTurn=0;

    public PlayerServerHelper(String name, Socket socket) throws IOException {
        this.name = name;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        for (int i = 0; i < characters.length; i++) {
            characters[i] = new Characters(names[i]);
        }
    }

    @Override
    public synchronized void run() {
        out.println("Insert your name: ");
        String messageFromClient;

        try {
            messageFromClient = in.readLine();
            name = messageFromClient;
            System.out.println(messageFromClient);

            out.println("Characters:");
            for (int i = 0; i < characters.length; i++) {
                out.println((i + 1) + ": " + characters[i].getName());
            }

            int number = chooseCharacter();

            nameHolder = characters[number - 1].getName();
            out.println("You picked " + nameHolder + ".");
            out.println("************************\n*      *       *       *\n*      *       *       *\n");
            init = true;

            String[] mensageSplit;
            String mensage;
            String fristWord;
            while (true){

                if(gameStart.players.get(0).isInit() && gameStart.players.get(1).isInit()){
                    mensage = in.readLine();
                    // if your replace this two lines with a single line regex I will be happy :)
                    mensageSplit= mensage.split(" ");
                    fristWord = mensageSplit[0];

                    if (fristWord.toUpperCase().equals("/ASK")){
                        if (currentTurn!=1){
                            send("Bitch please don't cheat its not your turn");
                            continue;
                        }

                        gameStart.sendToAll(name + ": "+ mensage);
                        currentTurn=3;
                        continue;

                    }
                    if (fristWord.toUpperCase().equals("/YES") || mensage.toUpperCase().equals("/NO") || mensage.toUpperCase().equals("/DON'T KNOW")){
                        if (currentTurn!=0){
                            send("NIGAA NOOO CHEAT");
                            continue;
                        }
                        currentTurn = 1;
                        int currentIndexPlayer = gameStart.players.indexOf(this);
                        gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(0);

                        gameStart.sendToAll(name + ": "+ mensage);
                        continue;
                    }
                    gameStart.sendToAll(name + ": "+ mensage);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int chooseCharacter() throws IOException {
        int number = 0;
        while (number < 1 || number > names.length) {
            out.println("Please pick your character's number:");
            String choice = in.readLine();
            number = Integer.parseInt(choice);
            if (number > names.length || number < 1) {
                out.println("Please insert a valid character");
            }
        }
        return number;
    }

    public void setGameStart(Server.GameStart gameStart) {
        this.gameStart = gameStart;
    }

    public void send(String responseLine) {
        out.println(responseLine);
    }

    public boolean isInit() {
        return init;
    }

    public PrintWriter getOut() {
        return out;
    }
    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }
}