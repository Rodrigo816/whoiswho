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
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";


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
                        gameStart.players.get(currentIndexPlayer==1?0:1).startBoard();
                        startBoard();
                        continue;
                    }
                    if (firstWordSplit[0].toUpperCase().equals("/REMOVE") && currentTurn==CurrentTurn.INACTIVE){
                        if (firstWordSplit.length == 1){
                            send("Wrong command please use /remove followed by the name");
                            continue;
                        }
                        for (int i = 0; i <characters.length ; i++) {
                            if (firstWordSplit[1].equals(characters[i].getName()) || Integer.parseInt(firstWordSplit[1]) == characters[i].getId()){
                                showColorBoard(characters[i].getId());
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

    private void clearScreen(){
        System.out.print("\033[H\033[2J");
    }

    public void showBoard(int Line){
        clearScreen();
        if(Line != 0) {
            out.println("" + boardGame[0][0].getId() + ":" + boardGame[0][0].getName() + "    " + boardGame[0][1].getId() + ":" + boardGame[0][1].getName() + "     " + boardGame[0][2].getId() + ":" + boardGame[0][2].getName() + "     " + boardGame[0][3].getId() + ":" + boardGame[0][3].getName() + "     " + boardGame[0][4].getId() + ":" + boardGame[0][4].getName());
        }
        if(Line != 1) {
            out.println(" " + boardGame[1][0].getId() + ":" + boardGame[1][0].getName() + "     " + boardGame[1][1].getId() + ":" + boardGame[1][1].getName() + "     " + boardGame[1][2].getId() + ":" + boardGame[1][2].getName() + "   " + boardGame[1][3].getId() + ":" + boardGame[1][3].getName() + "   " + boardGame[1][4].getId() + ":" + boardGame[1][4].getName());
        }
        if(Line != 2) {
            out.println(" " + boardGame[2][0].getId() + ":" + boardGame[2][0].getName() + "    " + boardGame[2][1].getId() + ":" + boardGame[2][1].getName() + "    " + boardGame[2][2].getId() + ":" + boardGame[2][2].getName() + "    " + boardGame[2][3].getId() + ":" + boardGame[2][3].getName() + "     " + boardGame[2][4].getId() + ":" + boardGame[2][4].getName());
        }
        if(Line != 3){
            out.println(" "+ boardGame[3][0].getId() + ":"+ boardGame[3][0].getName()+"     " + boardGame[3][1].getId() + ":"+ boardGame[3][1].getName()+"   " + boardGame[3][2].getId() + ":"+ boardGame[3][2].getName()+"   " + boardGame[3][3].getId() + ":"+ boardGame[3][3].getName()+"     " + boardGame[3][4].getId() + ":"+ boardGame[3][4].getName());
        }
        if(Line != 4) {
            out.println(" " + boardGame[4][0].getId() + ":" + boardGame[4][0].getName() + "    " + boardGame[4][1].getId() + ":" + boardGame[4][1].getName() + "   " + boardGame[4][2].getId() + ":" + boardGame[4][2].getName() + "  " + boardGame[4][3].getId() + ":" + boardGame[4][3].getName() + "   " + boardGame[4][4].getId() + ":" + boardGame[4][4].getName());
        }
        drawCharacters(Line);
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
        clearScreen();
        for (int Line = 0; Line < 5; Line++) {
            for (int Column = 0; Column < 5; Column++) {
                if (id == boardGame[Line][Column].getId()) {
                    boardGame[Line][Column].setSelected(true);
                    colorName(Line,Column);
                    continue;
                }
            }
            showBoard(Line);
            drawCharacters(Line);
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
        for(int Line = 0 ; Line < 5 ; Line++){
            if(Line == 0) {
                out.println("" + boardGame[Line][0].getId() + ":" + boardGame[Line][0].getName() + "    " + boardGame[Line][1].getId() + ":" + boardGame[Line][1].getName() + "     " + boardGame[Line][2].getId() + ":" + boardGame[Line][2].getName() + "     " + boardGame[Line][3].getId() + ":" + boardGame[Line][3].getName() + "     " + boardGame[Line][4].getId() + ":" + boardGame[Line][4].getName());
            }
            if(Line == 1) {
                out.println(" " + boardGame[Line][0].getId() + ":" + boardGame[Line][0].getName() + "     " + boardGame[Line][1].getId() + ":" + boardGame[Line][1].getName() + "     " + boardGame[Line][2].getId() + ":" + boardGame[Line][2].getName() + "   " + boardGame[Line][3].getId() + ":" + boardGame[Line][3].getName() + "   " + boardGame[Line][4].getId() + ":" + boardGame[Line][4].getName());
            }
            if(Line == 2) {
                out.println(" " + boardGame[Line][0].getId() + ":" + boardGame[Line][0].getName() + "    " + boardGame[Line][1].getId() + ":" + boardGame[Line][1].getName() + "    " + boardGame[Line][2].getId() + ":" + boardGame[Line][2].getName() + "    " + boardGame[Line][3].getId() + ":" + boardGame[Line][3].getName() + "     " + boardGame[Line][4].getId() + ":" + boardGame[Line][4].getName());
            }
            if(Line == 3){
                out.println(" "+ boardGame[Line][0].getId() + ":"+ boardGame[Line][0].getName()+"     " + boardGame[Line][1].getId() + ":"+ boardGame[Line][1].getName()+"   " + boardGame[Line][2].getId() + ":"+ boardGame[Line][2].getName()+"   " + boardGame[Line][3].getId() + ":"+ boardGame[Line][3].getName()+"     " + boardGame[Line][4].getId() + ":"+ boardGame[Line][4].getName());
            }
            if(Line == 4) {
                out.println(" " + boardGame[Line][0].getId() + ":" + boardGame[Line][0].getName() + "    " + boardGame[Line][1].getId() + ":" + boardGame[Line][1].getName() + "   " + boardGame[Line][2].getId() + ":" + boardGame[Line][2].getName() + "  " + boardGame[Line][3].getId() + ":" + boardGame[Line][3].getName() + "   " + boardGame[Line][4].getId() + ":" + boardGame[Line][4].getName());
            }
            drawCharacters(Line);
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





    public void colorName(int line, int col){

        switch (col){
            case 0:
                if(line == 0) {
                    out.println("" + ANSI_RED + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + ANSI_RESET + "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "     " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "     " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "     " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 1) {
                    out.println(" " +ANSI_RED + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + ANSI_RESET +"     " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "     " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "   " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "   " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 2) {
                    out.println(" " +ANSI_RED + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + ANSI_RESET +"    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "    " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "    " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "     " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 3){
                    out.println(" "+ ANSI_RED +boardGame[line][0].getId() + ":"+ boardGame[line][0].getName()+ANSI_RESET +"     " + boardGame[line][1].getId() + ":"+ boardGame[line][1].getName()+"   " + boardGame[line][2].getId() + ":"+ boardGame[line][2].getName()+"   " + boardGame[line][3].getId() + ":"+ boardGame[line][3].getName()+"     " + boardGame[line][4].getId() + ":"+ boardGame[line][4].getName());
                }
                if(line == 4) {
                    out.println(" " + ANSI_RED +boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + ANSI_RESET +"    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "   " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "  " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "   " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                break;
            case 1:
                if(line == 0) {
                    out.println("" + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " +ANSI_RED + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() +ANSI_RESET + "     " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "     " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "     " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 1) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "     " +ANSI_RED + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + ANSI_RESET +"     " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "   " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "   " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 2) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + ANSI_RED +boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + ANSI_RESET +"    " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "    " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "     " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 3){
                    out.println(" "+ boardGame[line][0].getId() + ":"+ boardGame[line][0].getName()+"     " + ANSI_RED +boardGame[line][1].getId() + ":"+ boardGame[line][1].getName()+ANSI_RESET +"   " + boardGame[line][2].getId() + ":"+ boardGame[line][2].getName()+"   " + boardGame[line][3].getId() + ":"+ boardGame[line][3].getName()+"     " + boardGame[line][4].getId() + ":"+ boardGame[line][4].getName());
                }
                if(line == 4) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + ANSI_RED +boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + ANSI_RESET +"   " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "  " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "   " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                break;
            case 2:
                if(line == 0) {
                    out.println("" +  boardGame[line][0].getId() + ":" + boardGame[line][0].getName() +  "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "     " +ANSI_RED + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + ANSI_RESET +"     " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "     " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 1) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "     " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "     " +ANSI_RED + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + ANSI_RESET +"   " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "   " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 2) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "    " +ANSI_RED + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + ANSI_RESET +"    " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "     " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 3){
                    out.println(" "+ boardGame[line][0].getId() + ":"+ boardGame[line][0].getName()+"     " + boardGame[line][1].getId() + ":"+ boardGame[line][1].getName()+"   " + ANSI_RED +boardGame[line][2].getId() + ":"+ boardGame[line][2].getName()+ANSI_RESET +"   " + boardGame[line][3].getId() + ":"+ boardGame[line][3].getName()+"     " + boardGame[line][4].getId() + ":"+ boardGame[line][4].getName());
                }
                if(line == 4) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "   " +ANSI_RED + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + ANSI_RESET +"  " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "   " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                break;
            case 3:
                if(line == 0) {
                    out.println("" +  boardGame[line][0].getId() + ":" + boardGame[line][0].getName() +  "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "     " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "     " + ANSI_RED +boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + ANSI_RESET +"     " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 1) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "     " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "     " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "   " + ANSI_RED +boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + ANSI_RESET +"   " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 2) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "    " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "    " + ANSI_RED +boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + ANSI_RESET +"     " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                if(line == 3){
                    out.println(" "+ boardGame[line][0].getId() + ":"+ boardGame[line][0].getName()+"     " + boardGame[line][1].getId() + ":"+ boardGame[line][1].getName()+"   " + boardGame[line][2].getId() + ":"+ boardGame[line][2].getName()+"   " +ANSI_RED + boardGame[line][3].getId() + ":"+ boardGame[line][3].getName()+ANSI_RESET +"     " + boardGame[line][4].getId() + ":"+ boardGame[line][4].getName());
                }
                if(line == 4) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "   " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "  " + ANSI_RED +boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + ANSI_RESET +"   " + boardGame[line][4].getId() + ":" + boardGame[line][4].getName());
                }
                break;
            case 4:
                if(line == 0) {
                    out.println("" + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "     " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "     " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "     " + ANSI_RED +boardGame[line][4].getId() + ":" + boardGame[line][4].getName()+ANSI_RESET);
                }
                if(line == 1) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "     " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "     " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "   " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "   " + ANSI_RED +boardGame[line][4].getId() + ":" + boardGame[line][4].getName()+ANSI_RESET);
                }
                if(line == 2) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "    " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "    " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "     " + ANSI_RED +boardGame[line][4].getId() + ":" + boardGame[line][4].getName()+ANSI_RESET);
                }
                if(line == 3){
                    out.println(" "+ boardGame[line][0].getId() + ":"+ boardGame[line][0].getName()+"     " + boardGame[line][1].getId() + ":"+ boardGame[line][1].getName()+"   " + boardGame[line][2].getId() + ":"+ boardGame[line][2].getName()+"   " + boardGame[line][3].getId() + ":"+ boardGame[line][3].getName()+"     " + ANSI_RED +boardGame[line][4].getId() + ":"+ boardGame[line][4].getName()+ANSI_RESET);
                }
                if(line == 4) {
                    out.println(" " + boardGame[line][0].getId() + ":" + boardGame[line][0].getName() + "    " + boardGame[line][1].getId() + ":" + boardGame[line][1].getName() + "   " + boardGame[line][2].getId() + ":" + boardGame[line][2].getName() + "  " + boardGame[line][3].getId() + ":" + boardGame[line][3].getName() + "   " +ANSI_RED + boardGame[line][4].getId() + ":" + boardGame[line][4].getName()+ANSI_RESET);
                }
                break;
            default:
                break;
        }
    }
}