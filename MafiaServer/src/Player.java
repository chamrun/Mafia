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
            god.notifyEverybody((god.nActives() + 1) + " actives.");
            god.addPlayer(this);

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
        return PURPLE + name + RESET;
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


    public void vote() throws IOException {

        isBusy = true;

        AskingHandler askingWhoHandler = new AskingHandler(god, this, socket, in, out, "Vote");
        askingWhoHandler.start();

    }

    public void setAnswerOfWho(int answer) {
        answerOfWho = answer;
        sendToClient("Got it.");
        isBusy = false;
        if (god.nobodyIsBusy()){
            god.stopWaiting();
        }
    }

    public boolean askYesOrNo(String question){
        sendToClient(question + "[yes, no]");

        isBusy = true;

        AskingHandler askingHandler = new AskingHandler(god, this, socket, in, out, "YesOrNo");
        askingHandler.start();

        // Should We Wait Here?

        return answerIsYes;

    }

    boolean answerIsYes;

    public void endYesOrNo(boolean answerIsYes) {
        System.out.println("answerIsYes: " + answerIsYes);
        this.answerIsYes = answerIsYes;
        isBusy = false;
    }

    public void nightAct() {

        if (this.role.actQuestion() == null){
            sendToClient("Just wait and try to hold on :D");
            return;
        }

        isBusy = true;

        sendToClient(role.actQuestion());

        AskingHandler askingHandler = new AskingHandler(god, this, socket, in, out, "Night");
        askingHandler.start();


    }

    public void notifyOthers(String massage){
        god.notifyEverybody(massage, this);
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


}