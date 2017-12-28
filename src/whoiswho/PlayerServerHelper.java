package whoiswho;
import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            String[] messageSplit;
            String message;
            String firstWord;
            String secondWord="";


            while (true){

                if(gameStart.players.get(0).isInit() && gameStart.players.get(1).isInit()){
                    message = in.readLine();
                    int currentIndexPlayer = gameStart.players.indexOf(this);
                    // if your replace this two lines with a single line regex I will be happy :)
                    messageSplit= message.split(" ");
                    firstWord = messageSplit[0];
                    if (messageSplit.length > 1){
                        secondWord = "";
                        for (int i = 1; i <messageSplit.length ; i++) {
                            secondWord += " " + messageSplit[i];
                        }
                    }
                    System.out.println(firstWord + "  ");
                    System.out.println(secondWord + "  ");
                    if (firstWord.toUpperCase().equals("/ASK")){
                        if (currentTurn!=1){
                            send("Bitch please don't cheat its not your turn");
                            continue;
                        }

                        gameStart.sendToAll(name + ": "+ message);
                        currentTurn=3;
                        continue;

                    }
                    if (firstWord.toUpperCase().equals("/YES") || message.toUpperCase().equals("/NO") || message.toUpperCase().equals("/DON\'T KNOW")){
                        if (currentTurn!=0){
                            send("NIGAA NOOO CHEAT");
                            continue;
                        }
                        currentTurn = 1;
                        gameStart.players.get(currentIndexPlayer==1?0:1).setCurrentTurn(0);
                        gameStart.sendToAll(name + ": "+ message);
                        continue;
                    }
                    if (firstWord.toUpperCase().equals("/TRY")) {
                        if (currentTurn!=1){
                            send("Bitch please don't cheat its not your turn");
                            continue;
                        }
                        if (secondWord == null){
                            send("Wrong comand please use /try fallowed by the name");
                            continue;
                        }


                        if (secondWord.toUpperCase().equals(" "+gameStart.players.get(currentIndexPlayer==1?0:1).getNameHolder().toUpperCase())){
                            send("Your guess "+secondWord+ "is correct.\n Congratulations "+name+ "You won the game.");
                            gameStart.players.get(currentIndexPlayer==1?0:1).send("Your opponent" + name + " tryed " +secondWord + ". You have lost the game.");
                            for (int i = 0; i <gameStart.players.size() ; i++) {
                                gameStart.players.get(i).in.close();
                                gameStart.players.get(i).out.close();
                                gameStart.players.get(i).socket.close();
                            }
                            break;
                        }
                        send("Your guess "+secondWord+ " is incorrect. Muahaha keep trying.");
                        gameStart.players.get(currentIndexPlayer==1?0:1).send("Your opponent" + name + " tryed " +secondWord + " and failed. Its your turn now");
                        currentTurn=3;
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
    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }
    public String getNameHolder() {
        return nameHolder;
    }

}