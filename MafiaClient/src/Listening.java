import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;

public class Listening extends Thread{

    DataInputStream in;

    public Listening(DataInputStream in){

        this.in = in;

    }

    @Override
    public void run() {
        while (true){
            try {

                System.out.println(in.readUTF() + "\n");

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
