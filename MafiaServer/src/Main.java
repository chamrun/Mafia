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

        int nOfAllPlayers = 3;

        ServerSocket server = new ServerSocket(5056);
        System.out.println("Now Clients Should connect to:\n" + server + "\n");

        for (int i = 0; i < nOfAllPlayers; i++) {

            god.addActive(server);

        }

        System.out.println("All Players are connected.\n");

        while (god.actives.size() != nOfAllPlayers){
            System.out.println("Waiting For Players to register and get ready...");

            Thread.sleep(5000);
        }

        god.setRandomRoles();
        System.out.println("Roles Are Set.");

        sc.nextLine();

        god.turnFirstNight();

        while (!god.gameIsOver()) {
            god.turnNight();
            god.turnDay();
            god.election();
        }

    }



}
