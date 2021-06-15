import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;


/**
 * The Command that god writes through game
 */
public class Command extends Thread{
    /**
     * The God.
     */
    God god;

    private boolean running;

    /**
     * Instantiates a new Command.
     *
     * @param god the god
     */
    public Command(God god){
        this.god = god;
        System.out.println("\nCommands: Save, Close, Menu, StopCommanding, Exit");
        running = true;
    }

    Scanner sc = new Scanner(System.in);

    String[] args = new String[0];

    @Override
    public void run() {

        while (true){

            String command = "NaN";

            if (running) {
                command = sc.nextLine();
            }


            switch (command) {
                case "Save":
                    save();
                    break;

                case "Close":
                    god.endGame();
                    Main.main(args);
                    break;

                case "Menu":
                    Main.main(args);
                    break;

                case "Exit":
                    System.out.println("Bye!");
                    System.exit(0);
                    break;

                case "StopCommanding":
                    System.out.println("Ok :/");
                    return;

                case "NaN":
                    System.out.println("CommandLine was closed.");
                    return;

                default:
                    System.out.println("Undefined Command.");
            }
        }
    }

    @Override
    public void interrupt() {
        running = false;
    }

    private void save(){
        System.out.println("Write a name: ");
        String title = sc.nextLine();

        File file = new File("savedGames\\" + title + ".txt");

        try {
            FileUtils.touch(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileOutputStream fos = new FileOutputStream("savedGames\\" + title + ".txt")){
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(god.getBackUp(title));
            fos.close();
            oos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Game was saved.");

    }

}
