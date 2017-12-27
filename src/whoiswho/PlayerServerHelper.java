package whoiswho;
import java.io.*;
import java.net.Socket;

public class PlayerServerHelper implements Runnable {
    private String name;
    private String[] names = {"Angelo", "Brandão", "Luís F", "Davide", "André",
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
    public void run() {
        out.println("Insert your name: ");
        String messageFromClient;

        try {
            messageFromClient = in.readLine();
            name = messageFromClient;


            out.println("Characters:");
            for (int i = 0; i < characters.length; i++) {
                out.println((i + 1) + ": " + characters[i].getName());
            }

            int number = chooseCharacter();
            nameHolder = characters[number - 1].getName();
            out.println("You picked " + nameHolder + ".");
            init = true;
            String mensage;
            while (true){
                mensage = in.readLine();
                gameStart.sendToAll(name + ": "+ mensage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int chooseCharacter() throws IOException {
        int number = 0;
        while (number < 1 || number > names.length - 1) {
            out.println("Please pick your character's number:");
            String choice = in.readLine();
            number = Integer.parseInt(choice);
            if (number > names.length - 1 || number < 1) {
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
}