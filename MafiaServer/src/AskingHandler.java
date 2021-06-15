import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Asking handler.
 * It can ask:
 *  0. Who to Vote?
 *  1. Who to act on night?
 *  2. Yes Or No? (Canceling Election by Mayor etc...)
 *  3. the Bulletproof wants to ask inquiry?
 *  4. Wanna watch game?
 */
public class AskingHandler extends Thread {

    private final God god;
    private final Player player;

    final private DataInputStream in;
    private final DataOutputStream out;
    private final String type;

    /**
     * The Index of answer for
     *  0. Who to Vote?
     *  1. Who to act on night?
     *  2. Yes Or No? (Canceling Election by Mayor etc...)
     *
     */
    int indexOfAnswer = -2;
    /**
     * The Answer for
     *  0. the Bulletproof wants to ask inquiry?
     *  1. Wanna watch game?
     */
    String answer;

    /**
     * the time we have to vote.
     */
    private long duration;
    private long start; // the Time we start to vote

    /**
     * Instantiates a new Asking handler.
     *
     * @param god    the god
     * @param player the player who's been asked
     * @param type   the type of question
     */
    public AskingHandler(God god, Player player, String type){
        this.god = god;
        this.player = player;
        this.in = player.in;
        this.out = player.out;
        this.type = type;
    }

    public AskingHandler(God god, Player player, String type, long duration){
        this.god = god;
        this.player = player;
        this.in = player.in;
        this.out = player.out;
        this.type = type;
        this.duration = duration;
    }

    @Override
    public void run() {

        try {

            start = System.currentTimeMillis();
            // the Time we can't vote anymore.
            long end = start + duration;


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
                    }, duration + 100); // if there won't be a answer in time, asked Client to say something to free DataInputStream

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

                    player.setVote(indexOfAnswer, start);

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

                    player.setNightAct(indexOfAnswer);

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
            player.setVote(-1, start);
            player.setNightAct(-1);
        }
        catch (IOException e) {
            System.out.println(player.getUserName() + " disconnected.");
            god.removePlayer(player);
        }

    }
}
