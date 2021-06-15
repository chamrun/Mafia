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

    /**
     * Instantiates a new Command.
     *
     * @param god the god
     */
    public Command(God god){
        this.god = god;
        System.out.println("\nCommands: Save, Close, StopCommanding");
    }

    Scanner sc = new Scanner(System.in);

    @Override
    public void run() {

        while (true){
            String command = sc.nextLine();

            switch (command) {
                case "Save":
                    save();
                    break;

                case "Close":
                    god.endGame();
                    String[] args = new String[0];
                    Main.main(args);
                    break;

                case "StopCommanding":
                    System.out.println("Ok :/");
                    return;

                default:
                    System.out.println("Undefined Command.");
            }
        }
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
