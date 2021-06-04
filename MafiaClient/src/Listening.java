import java.io.DataInputStream;
import java.io.DataOutputStream;
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

                if (serverSays.equals("DAY!") || serverSays.equals("VOTE!") || serverSays.equals("NIGHT!")){
                    out.writeUTF("LISTEN!");
                }
                else {
                    System.out.println(serverSays);
                }

            }
            catch (SocketException e){
                System.out.println("God is disconnected! Retrying in 10 seconds...");

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
