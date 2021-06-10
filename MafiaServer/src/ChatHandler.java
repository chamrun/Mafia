import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ChatHandler extends Thread{

    public static final String PURPLE = "\033[0;35m";
    public static final String RESET = "\033[0m";

    private final God god;
    private final Player player;

    private final DataInputStream in;
    private final DataOutputStream out;
    private final Socket socket;

    public ChatHandler(God god, Player player, Socket socket, DataInputStream in,  DataOutputStream out){
        this.god = god;
        this.player = player;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        try {

            out.writeUTF("\nDay is Started! You Can chat for 5 minutes. Send OVER if you're done.");

            long start = System.currentTimeMillis();
            long end = start + 300000;

            while (true) {
                String clientSays = in.readUTF();

                if (end < System.currentTimeMillis()) {
                    god.notifyEverybody("Chat time is up.");
                    god.stopWaiting();
                    return;
                }

                if (clientSays.equals("OVER")) {
                    out.writeUTF(PURPLE + "You" + RESET + " left chatroom.");
                    player.notifyOthers(PURPLE + player.getUserName() + RESET + " left chatroom.");
                    player.isBusy = false;

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
