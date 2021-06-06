import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class AskingWhoHandler extends Thread {
    public static final String PURPLE = "\033[0;35m";
    public static final String RESET = "\033[0m";

    God god;
    Player player;

    DataInputStream in;
    DataOutputStream out;
    Socket socket;
    String type;

    int result;

    public AskingWhoHandler(God god, Player player, Socket socket, DataInputStream in, DataOutputStream out, String type){
        this.god = god;
        this.player = player;
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.type = type;
    }

    @Override
    public void run() {


        try {

            long start = System.currentTimeMillis();
            long end = start + 10000;

            switch (type){
                case "VOTE":


                    StringBuilder massage = new StringBuilder("Who do you vote? (Enter number)\n");

                    for (int index = 0; index < god.nActives(); index++) {
                        if (!god.getUserName(index).equals(player.getUserName())) {
                            massage.append(index).append(". ").append(god.getUserName(index)).append("\n\nindex: ");
                        }
                    }

                    out.writeUTF(massage.toString());

                    int index = Integer.parseInt(in.readUTF());

                    while (index < 0 || god.nActives() <= index ||
                            god.getUserName(index).equals(player.getUserName())){

                        out.writeUTF("NOT VALID! Try Again: ");
                        index = Integer.parseInt(in.readUTF());
                    }

                    if (end < System.currentTimeMillis()) {
                        out.writeUTF("Unfortunately you're late and your vote wasn't counted.");
                        god.stopWaiting();
                        return;
                    }

                    player.endVote(index);

                    break;

                default:
                    System.out.println("Unreachable statement in voteHandler");
                    break;
            }


        } catch (SocketException e){
            System.out.println(player.getUserName() + " disconnected.");
            god.removePlayer(player);

            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }

    }
}
