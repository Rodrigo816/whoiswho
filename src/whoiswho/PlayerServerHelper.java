package whoiswho;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerServerHelper implements Runnable {
    private String name;
    private final String[] names = {"Ângelo", "Brandão", "Luís F", "Davide", "André",
            "César", "João S", "Amélia", "João M", "Sofia",
            "Rodrigo B", "Renata", "Luís S", "Toste", "Francisco",
            "Leandro", "Soraia", "Lobão", "Rodrigo D", "Dário",
            "Ferrão", "Catarina", "Sérgio", "Audrey", "Faustino"};
    private Characters[] characters = new Characters[names.length];
    private String nameHolder;
    private boolean init = false;
    private BufferedReader in;
    private PrintWriter out;
    private Server.GameStart gameStart;
    private Socket socket;
    public enum CurrentTurn { ACTIVE, INACTIVE, WAITING}
    private CurrentTurn currentTurn = CurrentTurn.INACTIVE;


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

        try {

            name = in.readLine();
            System.out.println(name);

            out.println("Characters:");
            for (int i = 0; i < characters.length; i++) {
                out.println((i + 1) + ": " + characters[i].getName());
            }

            int number = chooseCharacter();

            nameHolder = characters[number - 1].getName();
            out.println("You picked " + nameHolder + ".");
            out.println("************************\n*      *       *       *\n*      *       *       *\n");
            init = true;


            String message;
            String [] firstWordSplit;
            int currentIndexPlayer = gameStart.players.indexOf(this);

            int counter =0;
            while (true){

                if(gameStart.players.get(0).isInit() && gameStart.players.get(1).isInit()){

                    if (counter==0) {
                        counter++;
                        gameStart.sendToAll("[Server:] Game Started.");
                        for (int i = 0; i < gameStart.players.size(); i++) {
                            if (gameStart.players.get(i).getCurrentTurn() == CurrentTurn.ACTIVE) {
                                gameStart.players.get(i).send("[Server:] Its your turn");
                            } else {
                                gameStart.players.get(i).send("[Server:] Your opponent plays first.");
                            }
                        }
                    }

                    message = in.readLine();
                    firstWordSplit = message.split(" ", 2);
                    for (int i = 0; i <firstWordSplit.length ; i++) {
                        System.out.println(firstWordSplit[i]);
                    }

                    if (firstWordSplit[0].toUpperCase().equals("/ASK")){
                        if (currentTurn!=CurrentTurn.ACTIVE){
                            send("[Server:] Wait for your turn.");
                            continue;
                        }

                        gameStart.sendToAll("["+name+" ASK:] " + firstWordSplit[1]);
                        currentTurn=CurrentTurn.WAITING;
                        continue;

                    }
                    if (firstWordSplit[0].toUpperCase().equals("/YES") || firstWordSplit[0].toUpperCase().equals("/NO") || firstWordSplit[0].toUpperCase().equals("/DON\'T KNOW")){
                        if (currentTurn!=CurrentTurn.INACTIVE){
                            send("[Server:] You already have answered, make a question to your opponent.");
                            continue;
                        }
                        currentTurn = CurrentTurn.ACTIVE;
                        gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(CurrentTurn.INACTIVE);

                        firstWordSplit = message.split(" ", 2);
                        gameStart.sendToAll("["+name+" Answer "+ firstWordSplit[0] +" :] " + firstWordSplit[1]);
                        continue;
                    }
                    if (firstWordSplit[0].toUpperCase().equals("/TRY")) {
                        if (currentTurn!=CurrentTurn.ACTIVE){
                            send("[Server:] Wait for your turn.");
                            continue;
                        }
                        if (firstWordSplit.length==0){
                            send("Wrong command please use /try followed by the name");
                            continue;
                        }


                        if (firstWordSplit[1].toUpperCase().equals(gameStart.players.get(currentIndexPlayer==1?0:1).getNameHolder().toUpperCase())){
                            send("[Server:] Your guess "+firstWordSplit[1]+ "is correct.\n Congratulations "+name+ "You won the game.");
                            gameStart.players.get(currentIndexPlayer==1?0:1).send("[Server:] Your opponent" + name + " tried " +firstWordSplit[1] + ". You have lost the game.");
                            for (int i = 0; i <gameStart.players.size() ; i++) {
                                gameStart.players.get(i).in.close();
                                gameStart.players.get(i).out.close();
                                gameStart.players.get(i).socket.close();
                            }
                            break;
                        }
                        send("[Server:] Your guess "+firstWordSplit[1]+ " is incorrect. Keep trying.");
                        gameStart.players.get(currentIndexPlayer==1?0:1).send("[Server:] Your opponent" + name + " tried " +firstWordSplit[1] + " and failed. It's your turn now");
                        currentTurn=CurrentTurn.INACTIVE;
                        gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(CurrentTurn.ACTIVE);
                    }
                    gameStart.sendToAll(name + ": "+ message);
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
            Pattern p = Pattern.compile("[0-9]+"); //only accept numbers
            Matcher m = p.matcher(choice);
            if (m.matches()) {
                number = Integer.parseInt(choice);
            } else {
                number = 0;  //if user insert non digit characters ask to pick number again
            }

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
    public void setCurrentTurn(CurrentTurn currentTurn) {
        this.currentTurn = currentTurn;
    }
    public String getNameHolder() {
        return nameHolder;
    }

    public CurrentTurn getCurrentTurn() {
        return currentTurn;
    }

}