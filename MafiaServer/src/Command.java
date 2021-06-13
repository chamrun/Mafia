import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;


public class Command extends Thread{
    God god;

    public Command(God god){
        this.god = god;
    }

    Scanner sc = new Scanner(System.in);

    @Override
    public void run() {

        while (true){
            String command = sc.nextLine();

            switch (command) {
                case "SAVE":
                    save();
                    break;


                case "EXIT":
                    god.endGame();
                    break;

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

            oos.writeObject(god);
            fos.close();
            oos.close();

        } catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("Game was saved.");

    }

}
