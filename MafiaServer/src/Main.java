import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;


public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args){

        System.out.println();



        while (true) {
            System.out.println("What to do?\n" +
                    "0. New Game\n" +
                    "1. Saved Games");

            int choice = sc.nextInt();

            switch (choice) {
                case 0:
                    newGame();

                case 1:
                    loadGame();
                    break;

                default:
                    System.out.println("Undefined. Again: ");
                    break;
            }
        }


    }

    private static void loadGame() {

        File directory = new File("savedGames\\");

        Collection<File> files = FileUtils.listFiles(directory,
                new String[] {"txt"}, true);

        Backup backup = null;

        try {
            for (File f : files) {

            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);

            try {
                Backup temp;
                temp = (Backup) ois.readObject();
                temp.description();
                fis.close();
                ois.close();

            } catch (EOFException ignored){
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }


            }

            System.out.println("\nEnter title of a saved game to open it ('0' to go back)");
            sc.nextLine();
            String note = sc.nextLine();

            if (!note.equals("0")) {

                for (File f : files) {
                    if (f.getName().equalsIgnoreCase(note)
                            || f.getName().equalsIgnoreCase(note + ".txt")) {

                        FileInputStream fis = new FileInputStream(f);
                        ObjectInputStream ois = new ObjectInputStream(fis);

                        try {
                            backup = (Backup) ois.readObject();

                        } catch (ClassNotFoundException | IOException e) {
                            e.printStackTrace();
                        }

                        System.out.println("\n0. Delete\n" +
                                "1. Play");

                        int choice = sc.nextInt();
                        fis.close();
                        ois.close();


                        if (choice == 0) {
                            try {
                                FileUtils.forceDelete(f);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                }
            }




        }
        catch (IOException e) {
            e.printStackTrace();
        }

        final God god = new God();

        ServerSocket server = null;
        try {
            server = new ServerSocket(5056);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Now Clients Should connect to:\n" + server + "\n");

        for (int i = 0; i < backup.nPlayers(); i++) {

            god.addActive(server, backup);

        }



        startGame(god);
    }

    private static void newGame() {
        final God god = new God();

        System.out.println("How many Players? (at least 5)");
        int nOfAllPlayers = 3;/*= sc.nextInt();

        while (nOfAllPlayers < 5) {
            System.out.println("at least 5:");
            nOfAllPlayers = sc.nextInt();
        }
       */

        ServerSocket server = null;
        try {
            server = new ServerSocket(5056);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Now Clients Should connect to:\n" + server + "\n");

        for (int i = 0; i < nOfAllPlayers; i++) {

            god.addActive(server);

        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\nAll Players are connected.\n");

        int nUnready = nOfAllPlayers - god.nActives();

        while (nUnready != 0) {
            System.out.println(nUnready + " Player(s) is (are) not registered yet.");
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nUnready = nOfAllPlayers - god.nActives();
        }

        god.setRandomRoles();
        System.out.println("Roles Are Set.");

        startGame(god);
    }

    private static void startGame(God god) {

        (new Command(god)).start();

        god.turnFirstNight();

        while (true) {


            System.out.println("Day...");
            god.startChatroom();

            if (god.gameIsOver())
                break;

            System.out.println("Election...");
            god.election();

            if (god.gameIsOver())
                break;

            System.out.println("Night...");
            god.turnNight();

            if (god.gameIsOver())
                break;

        }

        System.out.println("... The end.");
        System.exit(0);
    }

}
