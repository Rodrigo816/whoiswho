package whoiswho;

import java.io.IOException;
public class Menu {
    private PlayerServerHelper toMenu;
    private boolean startGame = false;

    public Menu(PlayerServerHelper toMenu){
        this.toMenu = toMenu;
    }

    public void initialScreen(){
        toMenu.getOut().println(" ######   ##     ## ########  ######   ######       ##      ## ##     ##  #######   ####### ");
        toMenu.getOut().println("##    ##  ##     ## ##       ##    ## ##    ##      ##  ##  ## ##     ## ##     ## ##     ##");
        toMenu.getOut().println("##        ##     ## ##       ##       ##            ##  ##  ## ##     ## ##     ##       ## ");
        toMenu.getOut().println("##   #### ##     ## ######    ######   ######       ##  ##  ## ######### ##     ##     ###  ");
        toMenu.getOut().println("##    ##  ##     ## ##             ##       ##      ##  ##  ## ##     ## ##     ##    ##    ");
        toMenu.getOut().println("##    ##  ##     ## ##       ##    ## ##    ##      ##  ##  ## ##     ## ##     ##          ");
        toMenu.getOut().println(" ######    #######  ########  ######   ######        ###  ###  ##     ##  #######     ##    ");
        toMenu.getOut().println("");
        toMenu.getOut().println("#### ##    ##               ###           #  ######                                         ");
        toMenu.getOut().println(" ##  ###   ##              ## ##         #  ##    ##                          ##  ##        ");
        toMenu.getOut().println(" ##  ####  ##         #   ##   ##       #   ##                #               ##  ##        ");
        toMenu.getOut().println(" ##  ## ## ##       #    ##     ##     #    ##                  #                           ");
        toMenu.getOut().println(" ##  ##  ####     #      #########    #     ##                    #        ##        ##     ");
        toMenu.getOut().println(" ##  ##   ###       #    ##     ##   #      ##    ##            #            ##    ##       ");
        toMenu.getOut().println("#### ##    ##         #  ##     ##  #        ######  #######  #                ####         ");
        toMenu.getOut().println("");
        toMenu.getOut().println("");
        toMenu.getOut().println("");
    }

    public void menuInit() throws IOException {
        toMenu.getOut().println("============================");
        toMenu.getOut().println("|   GUESS WHO? IN <A/C_>   |");
        toMenu.getOut().println("============================");
        toMenu.getOut().println("| Menu options:            |");
        toMenu.getOut().println("|        1. Start Game     |");
        toMenu.getOut().println("|        2. Instructions   |");
        toMenu.getOut().println("|        3. Credits        |");
        toMenu.getOut().println("|        4. Exit           |");
        toMenu.getOut().println("============================");
        toMenu.getOut().println(" Select option: ");

        int selection = Integer.parseInt(toMenu.getIn().readLine());

        switch (selection) {
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
                toMenu.getOut().println(" Invalid option ");
                break;
        }
    }

    public void gameStarted(){
        toMenu.getOut().println("============================");
        toMenu.getOut().println("|   GUESS WHO? IN <A/C_>   |");
        toMenu.getOut().println("============================");
        toMenu.getOut().println("|       GAME STARTED       |");
        toMenu.getOut().println("============================");
        startGame = true;
    }

    public void instructions() throws IOException {
        toMenu.getOut().println("============================");
        toMenu.getOut().println("|   GUESS WHO? IN <A/C_>   |");
        toMenu.getOut().println("============================");
        toMenu.getOut().println("| Instructions:            |");
        toMenu.getOut().println("|1. Choose a character     |");
        toMenu.getOut().println("|2. Question your opponent |");
        toMenu.getOut().println("|3. Try to guess his       |");
        toMenu.getOut().println("|4. The first to guess wins|");
        toMenu.getOut().println("============================");
        menuBack();
    }

    public void credits() throws IOException {
        toMenu.getOut().println("============================");
        toMenu.getOut().println("|   GUESS WHO? IN <A/C_>   |");
        toMenu.getOut().println("============================");
        toMenu.getOut().println("| Credits:                 |");
        toMenu.getOut().println("|        João Martins      |");
        toMenu.getOut().println("|        Renata Faria      |");
        toMenu.getOut().println("|        Rodrigo Borba     |");
        toMenu.getOut().println("|        Sofia Melo        |");
        toMenu.getOut().println("============================");
        menuBack();
    }

    public void exit(){
        toMenu.getOut().println("============================");
        toMenu.getOut().println("|   GUESS WHO? IN <A/C_>   |");
        toMenu.getOut().println("============================");
        toMenu.getOut().println("|       EXITING GAME       |");
        toMenu.getOut().println("============================");
        System.exit(0);
    }

    public void menuBack() throws IOException {
        menuInit: while(true){
            toMenu.getOut().println(" Press 0 to return to the menu: ");

                int back = Integer.parseInt(toMenu.getIn().readLine());
                switch(back){
                    case 0:
                        toMenu.getOut().println(" Returning to the menu... ");
                        break menuInit;
                    default:
                        toMenu.getOut().println(" Invalid option ");
                        break;
                }

        }
    }

    public boolean isStartGame(){
        return startGame;
    }

}