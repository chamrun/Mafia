import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AskingHandler extends Thread {

    private final God god;
    private final Player player;

    final private DataInputStream in;
    private final DataOutputStream out;
    private final String type;

    int indexOfAnswer = -2;
    String answer;

    public AskingHandler(God god, Player player, String type){
        this.god = god;
        this.player = player;
        this.in = player.in;
        this.out = player.out;
        this.type = type;
    }

    @Override
    public void run() {

        try {

            long start = System.currentTimeMillis();
            long end = start + 30000;


            switch (type){
                case "Vote":

                    (new Timer()).schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (indexOfAnswer == -2) {
                                try {
                                    out.writeUTF("Time's up");
                                } catch (IOException e) {
                                    System.out.println(player.getUserName() + " disconnected.");
                                    god.removePlayer(player);
                                }
                            }
                        }
                    }, 31000);

                    StringBuilder massage = new StringBuilder("\nWho do you vote? (Enter number)\n-1: Nobody\n");

                    for (int i = 0; i < god.nActives(); i++) {
                        if (!god.getUserName(i).equals(player.getUserName())) {
                            massage.append(i).append(": ").append(god.getUserName(i)).append("\n");
                        }
                    }
                    massage.append("\nindex: ");

                    out.writeUTF(massage.toString());

                    indexOfAnswer = Integer.parseInt(in.readUTF());

                    while (indexOfAnswer < -1 || god.nActives() <= indexOfAnswer
                            || god.getUserName(indexOfAnswer).equals(player.getUserName())){

                        out.writeUTF("NOT VALID! Try Again: ");
                        indexOfAnswer = Integer.parseInt(in.readUTF());
                    }

                    if (end < System.currentTimeMillis()) {
                        out.writeUTF("Unfortunately you're late and you can't vote.");
                        god.stopWaiting();
                        return;
                    }

                    player.setAnswerOfWho(indexOfAnswer);

                    break;

                case "Night":

                    massage = new StringBuilder();

                    for (int i = 0; i < god.nActives(); i++) {
                        massage.append(i).append(": ").append(god.getUserName(i)).append("\n");
                    }

                    massage.append("\nindex: ");
                    out.writeUTF(massage.toString());

                    indexOfAnswer = Integer.parseInt(in.readUTF());

                    while (indexOfAnswer < -1 || god.nActives() <= indexOfAnswer){
                        out.writeUTF("NOT VALID! Try Again: ");
                        indexOfAnswer = Integer.parseInt(in.readUTF());
                    }

                    player.setAnswerOfWho(indexOfAnswer);

                    break;

                case "YesOrNo":

                    answer = in.readUTF();

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

                case "Bulletproof":

                    out.writeUTF(player.role.actQuestion() + " [yes, no]");
                    answer = in.readUTF();

                    while (!(answer.equalsIgnoreCase("yes"))
                            && !(answer.equalsIgnoreCase("no"))){

                        player.sendToClient("Invalid! Write \"yes\" or \"no\".");
                        answer = player.in.readUTF();

                    }

                    if (answer.equalsIgnoreCase("yes")) {
                        player.answerIsYes = true;
                    }
                    else if (answer.equalsIgnoreCase("no")){
                        player.answerIsYes = false;
                    }

                    player.sendToClient("Got it.");

                    player.isBusy = false;
                    if (god.nobodyIsBusy()){
                        god.stopWaiting();
                    }
                    break;


                case "Watch":
                    answer = in.readUTF();

                    while (!(answer.equalsIgnoreCase("yes"))
                            && !(answer.equalsIgnoreCase("no"))){

                        player.sendToClient("Invalid! Write \"yes\" or \"no\".");
                        answer = player.in.readUTF();

                    }

                    if (answer.equalsIgnoreCase("yes")) {
                        player.sendToClient("Watch and enjoy :D");
                        return;
                    }
                    else if (answer.equalsIgnoreCase("no")){
                        player.sendToClient("GoodBye!");
                        player.end();
                        return;
                    }

                    break;


                default:
                    System.out.println(type + ": ERR: Undefined askingType.");
                    break;
            }


        }
        catch (NumberFormatException e){
            System.out.println("invalid input in askingHandler. Needed number.");
            player.setAnswerOfWho(-1);
        }
        catch (IOException e) {
            System.out.println(player.getUserName() + " disconnected.");
            god.removePlayer(player);
        }

    }
}
