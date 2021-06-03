import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
/*
        System.out.print("Host (mafia default: 192.0.0.1): ");
        String host = sc.nextLine();
        System.out.print("Host (mafia default: 5056): ");
        int port = sc.nextInt();

 */
        String host = "0.0.0.0";
        int port = 5056;

        while (true) {
            try {
                Socket socket = new Socket(host, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());


                /*
                System.out.println("Name: ");
                String name = sc.nextLine();
                */

                Random r = new Random();

                String name;

                switch (r.nextInt(7)){
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

                    default:
                        name = ":////";
                        break;

                }


                out.writeUTF(name);

                while (in.readUTF().equals("BadName")) {
                    System.out.print("Your chosen name is already used.\nTry another name: ");
                    name = sc.nextLine();
                    out.writeUTF(name);
                }

                System.out.println("You've been registered successfully.\n");
                //sc.nextLine();

                (new Listening(in)).start();


                while (true) {
                    out.writeUTF(sc.nextLine());
                }

            } catch (ConnectException e) {
                System.out.println("Server is not Started. Press Enter to try again.");
                sc.nextLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }











    }
}
