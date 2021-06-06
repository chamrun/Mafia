import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class AskingHandler extends Thread {
    public static final String PURPLE = "\033[0;35m";
    public static final String RESET = "\033[0m";

    God god;
    Player player;

    DataInputStream in;
    DataOutputStream out;
    Socket socket;
    String type;

    int result;

    public AskingHandler(God god, Player player, Socket socket, DataInputStream in, DataOutputStream out, String type){
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
                case "Vote":

                    StringBuilder massage = new StringBuilder("Who do you vote? (Enter number)\n");

                    for (int i = 0; i < god.nActives(); i++) {
                        if (!god.getUserName(i).equals(player.getUserName())) {
                            massage.append(i).append(". ").append(god.getUserName(i)).append("\n\nindex: ");
                        }
                    }

                    out.writeUTF(massage.toString());

                    int indexOfAnswer = Integer.parseInt(in.readUTF());

                    while (indexOfAnswer < 0 || god.nActives() <= indexOfAnswer ||
                            god.getUserName(indexOfAnswer).equals(player.getUserName())){

                        out.writeUTF("NOT VALID! Try Again: ");
                        indexOfAnswer = Integer.parseInt(in.readUTF());
                    }

                    if (end < System.currentTimeMillis()) {
                        out.writeUTF("Unfortunately you're late and your vote wasn't counted.");
                        god.stopWaiting();
                        return;
                    }

                    player.setAnswerOfWho(indexOfAnswer);

                    break;

                case "Night":

                    massage = new StringBuilder();

                    for (int i = 0; i < god.nActives(); i++) {
                        massage.append(i).append(". ").append(god.getUserName(i)).append("\n\nindex: ");
                    }

                    out.writeUTF(massage.toString());

                    indexOfAnswer = Integer.parseInt(in.readUTF());

                    while (indexOfAnswer < 0 || god.nActives() <= indexOfAnswer){
                        out.writeUTF("NOT VALID! Try Again: ");
                        indexOfAnswer = Integer.parseInt(in.readUTF());
                    }

                    player.setAnswerOfWho(indexOfAnswer);

                    break;

                case "YesOrNo":
                    String answer = in.readUTF();

                    while (!(answer.equalsIgnoreCase("yes"))
                            && !(answer.equalsIgnoreCase("no"))){

                        player.sendToClient("Invalid! Write \"yes\" or \"no\".");
                        answer = player.in.readUTF();

                    }

                    if (answer.equalsIgnoreCase("yes")) {
                        player.endYesOrNo(true);
                        return;
                    }
                    else if (answer.equalsIgnoreCase("no")){
                        player.endYesOrNo(false);
                        return;
                    }

                    break;


                default:
                    System.out.println("Undefined type :/");
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
