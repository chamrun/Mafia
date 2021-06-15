import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Addr: (mafia default[1]: 0.0.0.0): ");
        String host = sc.nextLine();
        if (host.equals("1")){
            host = "0.0.0.0";
        }

        System.out.print("port (mafia default[1]: 5056): ");
        int port = sc.nextInt();sc.nextLine();

        if (port == 1){
            port = 5056;
        }

        while (true) {
            try {
                Socket socket = new Socket(host, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                System.out.print("Name: ");
                String name = sc.nextLine();//getRandomName();

                out.writeUTF(name);

                while (in.readUTF().equals("BadName")) {
                    System.out.print("Invalid name.\nTry Again: ");
                    name = sc.nextLine();
                    out.writeUTF(name);
                }

                System.out.println("Hello " + name + "!\nYou've been registered successfully.\n" +
                        "You can write Exit to leave, but please don't =(\n");
                //sc.nextLine();

                (new Listening(in, out)).start();


                while (true) {

                    String myMassage = sc.nextLine();

                    if (myMassage.equals("Exit")){
                        System.out.println("Goodbye!");
                        in.close();
                        out.close();
                        socket.close();
                        System.exit(0);
                    }

                    out.writeUTF(myMassage);

                }

            } catch (ConnectException e) {
                System.out.println("Server seems wrong. Press Enter to try again.");
                sc.nextLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getRandomName() {

        Random r = new Random();

        String name;

        switch (r.nextInt(10)){
            case 0:
                name = "Saber";
                break;

            case 1:
                name = "ChamRun";
                break;

            case 2:
                name = "Ali";
                break;

            case 3:
                name = "Reza";
                break;

            case 4:
                name = "Sam";
                break;

            case 5:
                name = "Steve";
                break;

            case 6:
                name = "Mohammad";
                break;

            case 7:
                name = "NoOne";
                break;

            case 8:
                name = "Ahmad";
                break;

            case 9:
                name = "Edd";
                break;

            case 10:
                name = "Calvin";
                break;

            default:
                name = ":////";
                break;

        }

        return name;

    }


}
