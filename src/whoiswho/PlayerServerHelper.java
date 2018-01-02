package whoiswho;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerServerHelper implements Runnable {
    private String name;
    private final String[] names = {"Francisco", "Toste", "Leandro", "João M", "André",
                                    "Brandão", "Ângelo", "Renata", "Rodrigo D", "Soraia",
                                    "Luís F", "Davide", "Amélia", "Luís S", "Sofia",
                                    "João S", "César", "Rodrigo B", "Lobão", "Dário",
                                    "Ferrão", "Sérgio", "Catarina", "Faustino", "Audrey"};
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
    private int nameHolderNumber;


    public PlayerServerHelper(String name, Socket socket) throws IOException {
        this.name = name;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        for (int i = 0; i < characters.length; i++) {
            characters[i] = new Characters(names[i],i+1);
        }
        menu = new Menu(this);
        boardGame = new Characters[5][5];
    }

    @Override
    public void run() {

        getMenu().initialScreen();
        try {
            while(!getMenu().isStartGame()) {
                clearScreen();
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
                    }

                    message = in.readLine();
                    firstWordSplit = message.split(" ", 2);

                    if (firstWordSplit[0].toUpperCase().equals("/ASK")){

                        if (currentTurn!=CurrentTurn.ACTIVE){
                            send("[Server:] Wait for your turn.");
                            continue;
                        }

                        if (firstWordSplit.length == 1){
                            send("Wrong command, please use /ask followed by the question.");
                            continue;
                        }

                        gameStart.sendToAll("[" + name + " ASK:] " + firstWordSplit[1]);
                        currentTurn=CurrentTurn.WAITING;

                        if (currentTurn == CurrentTurn.WAITING && gameStart.players.get(currentIndexPlayer==1?0:1).currentTurn == CurrentTurn.WAITING) {
                            gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(CurrentTurn.INACTIVE);
                        }

                        continue;

                    }
                    if (firstWordSplit[0].toUpperCase().equals("/YES") || firstWordSplit[0].toUpperCase().equals("/NO")){
                        if (currentTurn!=CurrentTurn.INACTIVE){
                            send("[Server:] It's not your time to answer.");
                            continue;
                        }
                        currentTurn = CurrentTurn.ACTIVE;
                        gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(CurrentTurn.INACTIVE);
                        gameStart.sendToAll("[" + name + " ANSWER:] " + firstWordSplit[0].substring(1));
                        continue;
                    }
                    if (firstWordSplit[0].toUpperCase().equals("/REMOVE")){
                        if (firstWordSplit.length == 1){
                            send("Wrong command, please use /remove followed by the number.");
                            continue;
                        }
                        Pattern p = Pattern.compile("[0-9]+"); //only accept numbers
                        Matcher m = p.matcher(firstWordSplit[1]);
                        if (m.matches()) {
                            if (Integer.parseInt(firstWordSplit[1]) > names.length || Integer.parseInt(firstWordSplit[1]) < 1) {
                                send("Wrong character's number, please use /remove followed by the number.");
                                continue;
                            }
                            for (int i = 0; i <characters.length ; i++) {
                                if (Integer.parseInt(firstWordSplit[1]) == characters[i].getId()){
                                    showColorBoard(characters[i].getId());
                                }
                            }
                        } else {
                            send("Wrong command, please use /remove followed by the number.");
                        }
                        continue;

                    }
                    if (firstWordSplit[0].toUpperCase().equals("/TRY")) {
                        if (currentTurn!=CurrentTurn.ACTIVE){
                            send("[Server:] Wait for your turn.");
                            continue;
                        }
                        if (firstWordSplit.length == 1){
                            send("Wrong command, please use /try followed by the number.");
                            continue;
                        }
                        Pattern p = Pattern.compile("[0-9]+"); //only accept numbers
                        Matcher m = p.matcher(firstWordSplit[1]);
                        if (!m.matches()) {
                            send("Wrong command, please use /try followed by the number.");
                            continue;
                        }

                        int num = Integer.parseInt(firstWordSplit[1]);
                        if (num == gameStart.players.get(currentIndexPlayer==1?0:1).getNameHolderNumber()){
                            send("[Server:] Your guess is correct.\n Congratulations " + name + "! You won the game ;P");
                            gameStart.players.get(currentIndexPlayer==1?0:1).send("[Server:] Your opponent tried " + gameStart.players.get(currentIndexPlayer==1?0:1).getNameHolder() + " and won the game. Sorry! You have lost :(");
                            for (int i = 0; i <gameStart.players.size() ; i++) {
                                gameStart.players.get(i).in.close();
                                gameStart.players.get(i).out.close();
                                gameStart.players.get(i).socket.close();
                            }
                            break;
                        }
                        send("[Server:] Your guess is incorrect. Keep trying.");
                        gameStart.players.get(currentIndexPlayer==1?0:1).send("[Server:] Your opponent " + name + " tried to guess and failed. It's your turn now.");
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
                out.println((i + 1) + characters[i].getName());
            }

            nameHolderNumber = chooseCharacter();
            nameHolder = characters[nameHolderNumber- 1].getName();
            nameHolder = nameHolder.substring(1);
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

    private void clearScreen(){
        System.out.print("\033[H\033[2J");
    }

    public void showBoard(){
        clearScreen();
        for(int Line = 0 ; Line < 5 ; Line++){
            if(Line == 0) {
                out.println("" + boardGame[Line][0].getIdString() + boardGame[Line][0].getName() + "    " + boardGame[Line][1].getIdString() + boardGame[Line][1].getName() + "     " + boardGame[Line][2].getIdString() + boardGame[Line][2].getName() + "     " + boardGame[Line][3].getIdString() + boardGame[Line][3].getName() + "     " + boardGame[Line][4].getIdString() + boardGame[Line][4].getName());
            }
            if(Line == 1) {
            out.println(" " + boardGame[Line][0].getIdString() + boardGame[Line][0].getName() + "     " + boardGame[Line][1].getIdString() + boardGame[Line][1].getName() + "     " + boardGame[Line][2].getIdString() + boardGame[Line][2].getName() + "   " + boardGame[Line][3].getIdString() + boardGame[Line][3].getName() + "   " + boardGame[Line][4].getIdString() + boardGame[Line][4].getName());
            }
            if(Line == 2) {
                out.println(" " + boardGame[Line][0].getIdString() + boardGame[Line][0].getName() + "    " + boardGame[Line][1].getIdString() + boardGame[Line][1].getName() + "    " + boardGame[Line][2].getIdString() + boardGame[Line][2].getName() + "    " + boardGame[Line][3].getIdString() + boardGame[Line][3].getName() + "     " + boardGame[Line][4].getIdString() + boardGame[Line][4].getName());
            }
            if(Line == 3){
                out.println(" "+ boardGame[Line][0].getIdString() + boardGame[Line][0].getName()+"     " + boardGame[Line][1].getIdString() + boardGame[Line][1].getName()+"   " + boardGame[Line][2].getIdString() + boardGame[Line][2].getName()+"   " + boardGame[Line][3].getIdString() + boardGame[Line][3].getName()+"     " + boardGame[Line][4].getIdString() + boardGame[Line][4].getName());
            }
            if(Line == 4) {
                out.println(" " + boardGame[Line][0].getIdString() + boardGame[Line][0].getName() + "    " + boardGame[Line][1].getIdString() + boardGame[Line][1].getName() + "   " + boardGame[Line][2].getIdString() + boardGame[Line][2].getName() + "  " + boardGame[Line][3].getIdString() + boardGame[Line][3].getName() + "   " + boardGame[Line][4].getIdString() + boardGame[Line][4].getName());
            }
            drawCharacters(Line);
        }

    }

    public void drawCharacters(int line){

        if(line == 1 || line == 2 || line == 4){
            out.println("____________ ____________ ____________ ____________ ____________");
            out.println("|          | |          | |          | |          | |          |");
            out.println("|    []_   | |    []_   | |   |><|   | |    []_   | |   |><|   |");
            out.println("|   (¨,)   | |   (¨,)   | |   (¨,)   | |   (¨,)   | |   (¨,)   |");
            out.println("|  /¨||¨\\  | |  /¨||¨\\  | |  --/\\--  | |  /¨||¨\\  | |  --/\\--  |");
            out.println("|    ||    | |    ||    | |   /__\\   | |    ||    | |   /__\\   |");
            out.println("|   /  \\   | |   /  \\   | |    ||    | |   /  \\   | |    ||    |");
            out.println("|__________| |__________| |__________| |__________| |__________|");
            out.println();
        }
        else{
            out.println("____________ ____________ ____________ ____________ ____________");
            out.println("|          | |          | |          | |          | |          |");
            out.println("|    []_   | |    []_   | |    []_   | |    []_   | |    []_   |");
            out.println("|   (¨,)   | |   (¨,)   | |   (¨,)   | |   (¨,)   | |   (¨,)   |");
            out.println("|  /¨||¨\\  | |  /¨||¨\\  | |  /¨||¨\\  | |  /¨||¨\\  | |  /¨||¨\\  |");
            out.println("|    ||    | |    ||    | |    ||    | |    ||    | |    ||    |");
            out.println("|   /  \\   | |   /  \\   | |   /  \\   | |   /  \\   | |   /  \\   |");
            out.println("|__________| |__________| |__________| |__________| |__________|");
            out.println();
        }
    }

    public void showColorBoard(int id) {
        for (int Line = 0; Line < 5; Line++) {
            for (int Column = 0; Column < 5; Column++) {
                if (id == boardGame[Line][Column].getId()) {
                    boardGame[Line][Column].setSelected(true);
                    boardGame[Line][Column].setRedName();
                }
            }
            showBoard();
        }
    }

    public void startBoard(){
        int counter = 0;
        for(int i=0 ; i<boardGame.length ; i++){
            for(int j=0 ; j<boardGame.length ; j++){
                boardGame[i][j] = characters[counter];
                counter++;
            }
        }
        showBoard();
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

    public int getNameHolderNumber() {
        return nameHolderNumber;
    }

    public String getNameHolder() {
        return nameHolder;
    }
}