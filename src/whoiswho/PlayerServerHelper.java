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
    private String[] gender = {"M", "M", "M", "M", "M",
            "M", "M", "F", "M", "F",
            "M", "F", "M", "M", "M",
            "M", "F", "M", "M", "M",
            "M", "F", "M", "F", "M"};
    private Characters[] characters = new Characters[names.length];
    private Characters[][] boardGame;
    private Menu menu;
    private String nameHolder;
    private boolean init = false;
    private BufferedReader in;
    private PrintWriter out;
    private Server.GameStart gameStart;
    private Socket socket;
    public enum CurrentTurn { ACTIVE, INACTIVE, WAITING}
    private CurrentTurn currentTurn = CurrentTurn.WAITING;


    public PlayerServerHelper(String name, Socket socket) throws IOException {
        this.name = name;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        for (int i = 0; i < characters.length; i++) {
            characters[i] = new Characters(names[i], gender[i],i+1);
        }
        menu = new Menu(this);
        boardGame = new Characters[5][5];
    }

    @Override
    public void run() {

        getMenu().initialScreen();
        try {
            while(!getMenu().isStartGame()) {
                getMenu().menuInit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        playerInfo();

        try {
            String message;
            String [] firstWordSplit;
            int currentIndexPlayer = gameStart.players.indexOf(this);
            int counter = 0;

            while (true){

                if(gameStart.players.get(0).isInit() && gameStart.players.get(1).isInit()){

                    if (counter == 0) {
                        counter++;
                        gameStartedMessage();
                        startBoard();
                        showBoard();
                    }

                    message = in.readLine();
                    firstWordSplit = message.split(" ", 2);

                    if (firstWordSplit[0].toUpperCase().equals("/ASK")){

                        if (currentTurn!=CurrentTurn.ACTIVE){
                            send("[Server:] Wait for your turn");
                            continue;
                        }

                        if (firstWordSplit.length == 1){
                            send("Wrong command please use /ask followed by the question");
                            continue;
                        }

                        gameStart.sendToAll("[" + name + " ASK:] " + firstWordSplit[1]);
                        currentTurn=CurrentTurn.WAITING;

                        if (currentTurn == CurrentTurn.WAITING && gameStart.players.get(currentIndexPlayer==1?0:1).currentTurn == CurrentTurn.WAITING) {
                            gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(CurrentTurn.INACTIVE);
                        }

                        continue;

                    }
                    if (firstWordSplit[0].toUpperCase().equals("/YES") || firstWordSplit[0].toUpperCase().equals("/NO") || firstWordSplit[0].toUpperCase().equals("/UNKNOWN")){
                        if (currentTurn!=CurrentTurn.INACTIVE){
                            send("[Server:] It's not your time to answer");
                            continue;
                        }
                        currentTurn = CurrentTurn.ACTIVE;
                        gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(CurrentTurn.INACTIVE);
                        gameStart.sendToAll("[" + name + " ANSWER:] " + firstWordSplit[0].substring(1));
                        showBoard();
                        continue;
                    }
                    if (firstWordSplit[0].toUpperCase().equals("/REMOVE") && currentTurn==CurrentTurn.ACTIVE){
                        if (firstWordSplit.length == 1){
                            send("Wrong command please use /remove followed by the name");
                            continue;
                        }
                        for (int i = 0; i <characters.length ; i++) {
                            if (firstWordSplit[1].equals(characters[i].getName())){
                                setBoard();

                            }
                        }
                    }
                    if (firstWordSplit[0].toUpperCase().equals("/TRY")) {
                        if (currentTurn!=CurrentTurn.ACTIVE){
                            send("[Server:] Wait for your turn");
                            continue;
                        }
                        if (firstWordSplit.length == 1){
                            send("Wrong command please use /try followed by the name");
                            continue;
                        }


                        if (firstWordSplit[1].toUpperCase().equals(gameStart.players.get(currentIndexPlayer==1?0:1).getNameHolder().toUpperCase())){
                            send("[Server:] Your guess "+firstWordSplit[1]+ " is correct.\n Congratulations " + name + ". You won the game");
                            gameStart.players.get(currentIndexPlayer==1?0:1).send("[Server:] Your opponent " + name + " tried " +firstWordSplit[1] + ". You have lost the game");
                            for (int i = 0; i <gameStart.players.size() ; i++) {
                                gameStart.players.get(i).in.close();
                                gameStart.players.get(i).out.close();
                                gameStart.players.get(i).socket.close();
                            }
                            break;
                        }
                        send("[Server:] Your guess "+firstWordSplit[1]+ " is incorrect. Keep trying.");
                        gameStart.players.get(currentIndexPlayer==1?0:1).send("[Server:] Your opponent " + name + " tried " +firstWordSplit[1] + " and failed. It's your turn now");
                        currentTurn=CurrentTurn.WAITING;
                        gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(CurrentTurn.ACTIVE);
                        continue;
                    }
                    gameStart.sendToAll(name + ": "+ message);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gameStartedMessage() {

        send("[Server:] Game Started"); //if change this print have to change also on the client class
        if(currentTurn == CurrentTurn.ACTIVE){
            send("[Server:] It's your turn\n");
        }
        if(currentTurn == CurrentTurn.WAITING) {
            send("[Server:] Your opponent plays first\n");
        }
    }

    public void playerInfo() {

        out.println("Insert your username: "); //if change this print have to change also on the client class

        try {

            name = in.readLine();

            out.println("Characters:");
            for (int i = 0; i < characters.length; i++) {
                out.println((i + 1) + ": " + characters[i].getName());
            }

            int number = chooseCharacter();

            nameHolder = characters[number - 1].getName();
            out.println("You picked " + nameHolder + "."); //if change this print have to change also on the client class
            out.println("\n*****************************************\n");
            init = true;
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
                out.println("Please insert a valid character's number:");
            }
        }
        return number;
    }

    public void showBoard(){
        out.println("============================");
        out.println("|   GUESS WHO? IN <A/C_>   |");
        out.println("============================");
        out.println("| Menu options:            |");
        out.println("|        1. Start Game     |");
        out.println("|        2. Instructions   |");
        out.println("|        3. Credits        |");
        out.println("|        4. Exit           |");
        out.println("============================");

        int counter = 0;
        for(int Line = 0 ; Line < 5 ; Line++){
            out.println();
            for(int Column = 0 ; Column < 5 ; Column++){
                out.print("   " + counter+1 + ": "+ boardGame[Line][Column].getName());
                counter++;
            }
            out.println();
            for(int Column = 0; Column < 5; Column++){
                out.print("    "+ boardGame[Line][Column].getGender());
            }
        }
    }

    public void setBoard(int number){

    }

    public void startBoard(){
        int counter = 0;
        for(int i=0 ; i<boardGame.length ; i++){
            for(int j=0 ; j<boardGame.length ; j++){
                boardGame[i][j] = characters[counter];
                counter++;
            }
        }
    }

    public void setGameStart(Server.GameStart gameStart) {
        this.gameStart = gameStart;
    }

    public void send(String message) {
        out.println(message);
    }

    public boolean isInit() {
        return init;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setCurrentTurn(CurrentTurn currentTurn) {
        this.currentTurn = currentTurn;
    }

    public String getNameHolder() {
        return nameHolder;
    }
}