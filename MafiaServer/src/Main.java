import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Scanner;


/**
 * The Main class that controls game.
 */
public class Main {

    /**
     * The "sc" will take input.
     */
    static Scanner sc = new Scanner(System.in);

    /**
     * main class to start running.
     *
     * @param args the args
     */
    public static void main(String[] args) {

        while (true) {

            int choice = -1;

            try {
                while (choice < 0 || 2 < choice) {
                    System.out.println("\nWhat to do?\n" +
                            "0. New Game\n" +
                            "1. Saved Games\n" +
                            "2. Exit");
                    choice = Integer.parseInt(sc.nextLine());
                }
            }
            catch (IndexOutOfBoundsException ignored) {}
            catch (NumberFormatException e) {
                System.out.println("You should write a number -_-");
                main(args);
            }

            switch (choice) {
                case 0:
                    newGame();
                    break;

                case 1:
                    loadGame();
                    break;

                case 2:
                    System.out.println("Bye!");
                    System.exit(0);
                    return;

                default:
                    System.out.println(choice + ": Menu/Undefined");
                    break;
            }
        }

    }

    /**
     * Shows saved game,
     * After selecting one, we can delete it or run it and wait for players to join
     */
    private static void loadGame() {

        File directory = new File("savedGames\\");

        Collection<File> files = FileUtils.listFiles(directory,
                new String[] {"txt"}, true); // All saved games

        Backup backup = null;

        try {
            for (File f : files) {

                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);

                try {
                    Backup temp; // All files, one by one, will be showed with some information
                    temp = (Backup) ois.readObject();
                    temp.description();
                    fis.close();
                    ois.close();

                } catch (EOFException ignored){
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("\nEnter title of a saved game to open it ('-1' to go back)");
            String title = sc.nextLine();

            if (title.equals("-1")){
                return;
            }



            for (File f : files) {
                if (f.getName().equalsIgnoreCase(title)
                        || f.getName().equalsIgnoreCase(title + ".txt")) {

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
                            System.out.println("Deleted Successfully.");
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (backup == null){
            System.out.println("No backup with that name =/");
            return;
        }

        ServerSocket server = getServer();

        final God god = new God(server);

        System.out.println("Now Clients Should connect to:\naddr: " + server.getInetAddress() + "\nPort: " + server.getLocalPort() + "\n");

        if (backup.nPlayers() == 0){
            System.out.println(backup.getReport());
            return;
        }

        for (int i = 0; i < backup.nPlayers(); i++) {
            god.addActive(server, backup);
        }

        god.turnFirstNight(backup.getReport());
        startGame(god);
    }

    /**
     * Makes a new God, takes players and start game
     */
    private static void newGame() {

        System.out.println("How many Players? (at least 3)");
        int nOfAllPlayers = sc.nextInt();

        while (nOfAllPlayers < 3) {
            System.out.println("at least 3:");
            nOfAllPlayers = sc.nextInt();
        }

        ServerSocket server = getServer();
        final God god = new God(server);

        System.out.println("Now Clients Should connect to:\n" +
                "addr: " + server.getInetAddress() + "\n" +
                "Port: " + server.getLocalPort() + "\n");

        for (int i = 0; i < nOfAllPlayers; i++) {
            god.addActive(server);
        }

        try {
            Thread.sleep(50);
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

        god.turnFirstNight();
        startGame(god);
    }

    private static ServerSocket getServer() {

        ServerSocket server = null;

        try {
            server = new ServerSocket(5056);
        } catch (BindException e) {
            System.out.println("Address already in use.\nTry Another Port: ");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("unable to use 5056.");
        }

        while (server == null) {
            try {
                server = new ServerSocket(sc.nextInt());
            } catch (BindException e) {
                System.out.println("Address already in use.\nTry Another Port: ");

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("unable to use 5056.");
            }
        }

        return server;

    }

    /**
     * When god and all players are ready, we use this method to turn game.
     * @param god a good god contain all we need to start a game!
     */
    private static void startGame(God god) {

        Command command = new Command(god);
        command.start(); // Keeps listening to us for "SAVE" or "EXIT" command.

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
        command.interrupt();

    }

}
