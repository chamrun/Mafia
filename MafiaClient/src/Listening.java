import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

public class Listening extends Thread{

    DataInputStream in;
    DataOutputStream out;

    public Listening(DataInputStream in, DataOutputStream out){

        this.in = in;
        this.out = out;

    }

    @Override
    public void run() {
        while (true){
            try {

                String serverSays = in.readUTF();

                if (serverSays.equals("Time's up"))
                    out.writeUTF("-1");
                else if (serverSays.equals("Chat's up"))
                    out.writeUTF("OVER");
                else
                    System.out.println(serverSays);

            }
            catch (EOFException e){
                System.out.println("Game was closed.\nSee you Later!");
                System.exit(0);
            }
            catch (SocketException e){
                System.out.println("God is disconnected :/ ...");
                System.exit(-1);

                return;
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
