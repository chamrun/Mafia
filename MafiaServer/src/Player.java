import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Player extends Thread {

    public static final String PURPLE = "\033[0;35m";
    public static final String RESET = "\033[0m";

    private God god;

    private String name;
    public Role role;



    public boolean isBusy = false;

    private boolean isSilent = false;

    private int answerOfWho = -1;

    private int nVotes = 0;

    DataInputStream in;
    DataOutputStream out;
    Socket socket;

    ChatHandler chatHandler;


    public Player(God god, DataInputStream in, DataOutputStream out, Socket socket) {
        this.god = god;
        this.in = in;
        this.out = out;
        this.socket = socket;

        chatHandler = new ChatHandler(god, this, socket, in, out);
    }

    public void addVote(){
        nVotes++;
    }

    public void resetVote(){
        nVotes = 0;
    }

    public int getNVote(){
        return nVotes;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public int getAnswerOfWho() {
        isBusy = false;
        int temp = answerOfWho;
        answerOfWho = -1;
        return temp;
    }

    @Override
    public void run() {

        try {

            name = in.readUTF();

            while (god.nameIsUsed(name)) {
                out.writeUTF("BadName");
                name = in.readUTF();
            }

            out.writeUTF("GoodName");

            System.out.println(name + " registered.\n");
            god.notifyActives((god.nActives() + 1) + " actives.");
            god.addPlayer(this);

            /*
            while (true) {

                long start = System.currentTimeMillis();
                long end = start + 20000;

                if (isAskedWho) {



                }

                if (isAskedYes){
                    out.writeUTF("You Have 30 seconds to answer...");
                    Thread.sleep(30000);
                }

                while (true){
                    if (in.readUTF().equals("LISTEN!")){
                        break;
                    }
                }

            }


             */


        }
        catch (SocketException e){
            System.out.println(name + " disconnected.");
            god.removePlayer(this);
        }
        catch (IOException e /*| InterruptedException e*/) {
            e.printStackTrace();
        }
    }

    public String getUserName() {
        return name;
    }

    public String getRoleNAme(){
        return role.getName();
    }

    public void introduction() {
        String massage = "Your role is: " + PURPLE + this.role.getName() + RESET + "\n";

        if (role instanceof Mayor) {
            massage += god.whoIsCityDoctor(this);
            sendToClient(massage);
            return;
        }

        if (role instanceof Citizen) {
            sendToClient(massage);
            return;
        }

        sendToClient(massage + god.getMafiaList(this));

    }

    public void joinChat() {

        if (isSilent) {
            isSilent = false;
        }
        else {
            isBusy = true;
            System.out.println(getUserName() + " Joined Chat.");

            chatHandler.start();
        }
    }


    public int vote() throws IOException {

        isBusy = true;

        AskingWhoHandler askingWhoHandler = new AskingWhoHandler(god, this, socket, in, out, "VOTE");
        askingWhoHandler.start();

        return answerOfWho;

    }

    public int act() {

        isBusy = true;

        //sendToClient("TALK!");

        StringBuilder massage = new StringBuilder(role.actQuestion() + "\n");


        for (int index = 0; index < god.nActives(); index++) {
            massage.append(index).append(". ").append(god.getUserName(index)).append("\n");
        }
        sendToClient(massage.toString());

        /*
        if (answerOfWho != -1) {
            answerOfWho.addVote();
            answerOfWho = -1;
        }

         */

        return answerOfWho;


    }

    public void notifyOthers(String massage){
        god.send(this, massage);
    }


    public void sendToClient(String playerListens){

        try {
            out.writeUTF(playerListens);
        }
        catch (SocketException e){
            System.out.println(name + " disconnected.");
            god.removePlayer(this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void mute() {
        isSilent = true;
    }

    public void endVote(int answer) {
        answerOfWho = answer;
        isBusy = false;
        if (god.nobodyIsBusy()){
            god.stopWaiting();
        }
    }
}