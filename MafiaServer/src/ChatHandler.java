import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Chat handler.
 */
public class ChatHandler extends Thread{

    /**
     * The constant Colors to make userNames more visible.
     */
    public static final String PURPLE = "\033[0;35m";
    public static final String RESET = "\033[0m";

    private final God god;
    private final Player player;

    private final DataInputStream in;
    private final DataOutputStream out;

    /**
     * Instantiates a new Chat handler.
     *
     * @param god    the god
     * @param player the player
     */
    public ChatHandler(God god, Player player){
        this.god = god;
        this.player = player;
        this.in = player.in;
        this.out = player.out;
    }

    /**
     * if chatHandler is running, or not.
     */
    boolean running = false;

    @Override
    public void run() {
        running = true;

        try { // if time is up and player didn't give any answer, this part makes him to send "OVER" to end chatroom
            (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    if (running) {
                        try {
                            out.writeUTF("Chat's up");
                            running = false;
                        } catch (IOException e) {
                            System.out.println(player.getUserName() + " disconnected.");
                            god.removePlayer(player);
                        }
                    }
                }
            }, 300000 + (new Random().nextInt(1000)));

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
        catch (IOException e) {
            System.out.println(player.getUserName() + " disconnected.");
            player.isBusy = false;
            running = false;
            if (god.nobodyIsBusy()) {
                god.notifyEverybody("Chatroom is empty.\n");
                god.stopWaiting();
            }
            god.removePlayer(player);
        }
    }

}
