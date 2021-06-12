import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ChatHandler extends Thread{

    public static final String PURPLE = "\033[0;35m";
    public static final String RESET = "\033[0m";

    private final God god;
    private final Player player;

    private final DataInputStream in;
    private final DataOutputStream out;
    private final Socket socket;

    public ChatHandler(God god, Player player){
        this.god = god;
        this.player = player;
        this.socket = player.socket;
        this.in = player.in;
        this.out = player.out;
    }

    boolean running = false;

    @Override
    public void run() {
        running = true;

        try {
            (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    if (running) {
                        try {
                            out.writeUTF("Chat's up");
                            running = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 3100 + (new Random().nextInt(1000)));

            out.writeUTF("\nDay is Started! You Can chat for 5 minutes. Send OVER if you're done.");


            while (player.isBusy) {
                String clientSays = in.readUTF();

                if (clientSays.equals("OVER")) {
                    out.writeUTF(PURPLE + "You" + RESET + " left chatroom.");
                    player.notifyOthers(PURPLE + player.getUserName() + RESET + " left chatroom.");
                    player.isBusy = false;
                    running = false;

                    if (god.nobodyIsBusy()) {
                        god.notifyEverybody("Chatroom is empty.\n");
                        god.stopWaiting();
                    }

                    return;
                }

                player.notifyOthers(PURPLE + player.getUserName() + ": " + RESET + clientSays);
            }
        }
        catch (SocketException e){
            System.out.println(player.getUserName() + " disconnected.");
            god.removePlayer(player);

            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
