package whoiswho;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientMenu {
    private boolean gameStartSelected = false;
    private Scanner scanner;

    public void initialScreen(){
        System.out.println(" ######   ##     ## ########  ######   ######       ##      ## ##     ##  #######   ####### ");
        System.out.println("##    ##  ##     ## ##       ##    ## ##    ##      ##  ##  ## ##     ## ##     ## ##     ##");
        System.out.println("##        ##     ## ##       ##       ##            ##  ##  ## ##     ## ##     ##       ## ");
        System.out.println("##   #### ##     ## ######    ######   ######       ##  ##  ## ######### ##     ##     ###  ");
        System.out.println("##    ##  ##     ## ##             ##       ##      ##  ##  ## ##     ## ##     ##    ##    ");
        System.out.println("##    ##  ##     ## ##       ##    ## ##    ##      ##  ##  ## ##     ## ##     ##          ");
        System.out.println(" ######    #######  ########  ######   ######        ###  ###  ##     ##  #######     ##    ");
        System.out.println("");
        System.out.println("#### ##    ##               ###           #  ######                                         ");
        System.out.println(" ##  ###   ##              ## ##         #  ##    ##                          ##  ##        ");
        System.out.println(" ##  ####  ##         #   ##   ##       #   ##                #               ##  ##        ");
        System.out.println(" ##  ## ## ##       #    ##     ##     #    ##                  #                           ");
        System.out.println(" ##  ##  ####     #      #########    #     ##                    #        ##        ##     ");
        System.out.println(" ##  ##   ###       #    ##     ##   #      ##    ##            #            ##    ##       ");
        System.out.println("#### ##    ##         #  ##     ##  #        ######  #######  #                ####         ");
        System.out.println("");
        System.out.println("");
        System.out.println("");
    }

    public void menuInit() throws IOException {
        System.out.println("============================");
        System.out.println("|   GUESS WHO? IN <A/C_>   |");
        System.out.println("============================");
        System.out.println("| Menu options:            |");
        System.out.println("|        1. Start Game     |");
        System.out.println("|        2. Instructions   |");
        System.out.println("|        3. Credits        |");
        System.out.println("|        4. Exit           |");
        System.out.println("============================");
        System.out.println(" Select option: ");

        Pattern p = Pattern.compile("[1-4]"); //only accept numbers from 1 to 4
        String selection = scanner.nextLine();
        Matcher m = p.matcher(selection);
        if (m.matches()) {
            int option = Integer.parseInt(selection);
            switch (option) {
                case 1:
                    gameStarted();
                    break;
                case 2:
                    instructions();
                    break;
                case 3:
                    credits();
                    break;
                case 4:
                    exit();
                    break;
                default:
                    System.out.println(" Invalid option ");
                    break;
            }
        }
        else{
            System.out.println(" Invalid option ");
        }

    }

    public void gameStarted(){
        System.out.println("============================");
        System.out.println("|   GUESS WHO? IN <A/C_>   |");
        System.out.println("============================");
        System.out.println("|       GAME STARTED       |");
        System.out.println("============================");
        gameStartSelected = true;
    }

    public void instructions() throws IOException {
        System.out.println("============================");
        System.out.println("|   GUESS WHO? IN <A/C_>   |");
        System.out.println("============================");
        System.out.println("| Instructions:            |");
        System.out.println("|1. Choose a character     |");
        System.out.println("|2. Question your opponent |");
        System.out.println("|3. Try to guess his       |");
        System.out.println("|4. The first to guess wins|");
        System.out.println("|Use /ask to question      |");
        System.out.println("|Use /yes or /no to answer |");
        System.out.println("|Use /remove to delete     |");
        System.out.println("|And use /try to guess     |");
        System.out.println("============================");
        menuBack();
    }

    public void credits() throws IOException {
        System.out.println("============================");
        System.out.println("|   GUESS WHO? IN <A/C_>   |");
        System.out.println("============================");
        System.out.println("| Credits:                 |");
        System.out.println("|        João Martins      |");
        System.out.println("|        Renata Faria      |");
        System.out.println("|        Rodrigo Borba     |");
        System.out.println("|        Sofia Melo        |");
        System.out.println("============================");
        menuBack();
    }

    public void exit(){
        System.out.println("============================");
        System.out.println("|   GUESS WHO? IN <A/C_>   |");
        System.out.println("============================");
        System.out.println("|       EXITING GAME       |");
        System.out.println("============================");
        System.exit(0);
    }

    public void menuBack() throws IOException {
        menuInit: while(true){
            System.out.println(" Press 0 to return to the menu: ");

            String line = scanner.nextLine();

            Pattern p = Pattern.compile("[0-9]+"); //only accept numbers
            Matcher m = p.matcher(line);
            if (!m.matches()) {
                System.out.println(" Invalid option ");
                continue;
            }

                int back = Integer.parseInt(line);
                switch(back){
                    case 0:
                        System.out.println(" Returning to the menu... ");
                        break menuInit;
                    default:
                        System.out.println(" Invalid option ");
                        break;
                }

        }
    }

    public boolean isGameStartSelected(){
        return gameStartSelected;
    }

    public void setGameStartSelected(boolean gameStartSelected) {
        this.gameStartSelected = gameStartSelected;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }
}