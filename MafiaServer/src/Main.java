import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Main {

    private static God god = new God();

    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner sc = new Scanner(System.in);

        /*
        System.out.println("How many Players? (at least 5)");
        int nOfAllPlayers = sc.nextInt();

        while (nOfAllPlayers < 5){
            System.out.println("at least 5");
            nOfAllPlayers = sc.nextInt();
        }
         */

        int nOfAllPlayers = 2;

        ServerSocket server = new ServerSocket(5056);
        System.out.println("Now Clients Should connect to:\n" + server + "\n");

        for (int i = 0; i < nOfAllPlayers; i++) {

            god.addActive(server);

        }

        Thread.sleep(1000);
        System.out.println("\nAll Players are connected.\n");

        int nUnready = nOfAllPlayers - god.actives.size();

        while (nUnready != 0){
            System.out.println(nUnready + " Player(s) is (are) not registered yet.");
            Thread.sleep(5000);
            nUnready = nOfAllPlayers - god.actives.size();
        }

        god.setRandomRoles();
        System.out.println("Roles Are Set.");//nPress Enter to start Game!");

        //sc.nextLine();

        god.turnFirstNight();

        while (!god.gameIsOver()) {

            //god.turnDay();
            System.out.println("ELECTION");
            //sc.nextLine();
            god.election();
            System.out.println("NIGHT");
            sc.nextLine();
            god.turnNight();

        }

        System.out.println("END");

    }



}
