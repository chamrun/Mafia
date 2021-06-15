import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ConcurrentModificationException;


/**
 * The Player and Things he needs
 */
public class Player implements Runnable{

    private static final String PURPLE = "\033[0;35m";
    private static final String RESET = "\033[0m";

    private final God god;

    private String name;

    public Role role;

    public boolean isBusy = false;
    private boolean isSilent = false;
    private int answerOfWho = -1;
    private int nVotes = 0; // number of people who voted this player to be killed

    Socket socket;
    DataInputStream in; // the DataInputStream that receives data from client
    DataOutputStream out; // the DataOutputStream that sends data to client

    ChatHandler chatHandler;
    AskingHandler askingWhoHandler;

    boolean waitingForInput = false;

    public boolean isWaitingForInput() {
        return waitingForInput;
    }

    /**
     * Instantiates a new Player.
     *
     * @param god    the god
     * @param socket the socket that connect client (player) and server (god)
     */
    public Player(God god, Socket socket) {
        this.god = god;
        this.socket = socket;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add vote, when someone voted to this player
     */
    public void addVote(){
        nVotes++;
    }

    /**
     * Reset vote, when election is done
     */
    public void resetVote(){
        nVotes = 0;
    }

    /**
     * Get number of votes.
     *
     * @return the int number of those who voted to this players
     */
    public int getNVote(){
        return nVotes;
    }

    /**
     * Is busy boolean.
     *
     * @return true if busy, false if waiting
     */
    public boolean isBusy() {
        return isBusy;
    }

    /**
     * Gets answer of who, after election or night.
     *
     * @return the answer of who
     */
    public int getAnswerOfWho() {
        isBusy = false;
        int temp = answerOfWho;
        answerOfWho = -1;
        return temp;
    }

    // Registers player and adds him to active players, in a new game.
    @Override
    public void run() {

        try {

            name = PURPLE + in.readUTF() + RESET;

            while (god.nameIsUsed(name)) {
                out.writeUTF("BadName");
                name = PURPLE + in.readUTF() + RESET;
            }

            out.writeUTF("GoodName");

            System.out.println(getUserName() + " registered.\n");
            god.notifyEverybody((god.nActives() + 1) + " actives.");
            god.addPlayer(this);

        }
        catch (ConcurrentModificationException e){
            System.out.println("Player couldn't register successfully.");
        }
        catch (IOException e) {
            System.out.println(getUserName() + " disconnected.");
            god.removePlayer(this);
        }
    }


    /**
     * Registers player and adds him to active players, in a loaded game.
     *
     * @param backup the backup
     */
    public void run(Backup backup) {

        try {

            name = PURPLE + in.readUTF() + RESET;

            while (!backup.nameExists(name)) {
                out.writeUTF("BadName");
                name = PURPLE + in.readUTF() + RESET;
            }

            out.writeUTF("GoodName");

            System.out.println(getUserName() + " registered.\n");
            god.notifyEverybody((god.nActives() + 1) + " actives.");

            role = backup.getRole(name);

            god.addPlayer(this);

        }
        catch (ConcurrentModificationException e){
            System.out.println("Player couldn't register successfully.");
        }
        catch (IOException e) {
            System.out.println(getUserName() + " disconnected.");
            god.removePlayer(this);
        }

    }


    /**
     * Gets user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return name;
    }

    /**
     * Get roleName as a string.
     *
     * @return the string that says what is this player's role
     */
    public String getRoleNAme(){
        return role.getName();
    }

    /**
     * Introduction which players get in first night
     */
    public void introduction() {
        String massage = "Your role is: " + PURPLE + role.getName() + RESET + "\n" + role.description() + "\n";

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

    /**
     * Join chat in start of day.
     */
    public void joinChat() {

        if (isSilent) {
            isSilent = false;
        }
        else {
            isBusy = true;
            System.out.println(getUserName() + " Joined Chat.");

            chatHandler = new ChatHandler(god, this);
            chatHandler.start();
        }
    }


    /**
     * Vote by asking handler
     */
    public void vote(){

        isBusy = true;
        askingWhoHandler = new AskingHandler(god, this, "Vote");
        askingWhoHandler.start();

    }


    /**
     * Sets vote.
     *
     * @param answer the answer
     */
    public void setVote(int answer) {

        answerOfWho = answer;
        sendToClient("Got it.");
        isBusy = false;

        if (god.nobodyIsBusy()){
            god.stopWaiting();
            return;
        }

        sendToClient("Before it's too late, you can write 0 to change your vote.");

        int choice = 1;

        try {
            waitingForInput = true;
            choice = Integer.parseInt(in.readUTF());
            waitingForInput = false;
        }
        catch (IOException ignored){}

        if (choice == 0) {
            isBusy = true;
            vote();
        }

    }


    /**
     * Sets night act.
     *
     * @param answer the answer
     */
    public void setNightAct(int answer) {
        if (role instanceof SimpleMafia){
            god.suggestGodFather(answer);
        }

        answerOfWho = answer;
        sendToClient("Got it.");
        isBusy = false;

        if (god.nobodyIsBusy()){
            god.stopWaiting();
        }
    }


    /**
     * Ask yes or no boolean.
     *
     * @param question the question
     * @return the boolean of answer
     */
    public boolean askYesOrNo(String question){
        sendToClient(question + "[yes, no]");

        isBusy = true;

        AskingHandler askingHandler = new AskingHandler(god, this, "YesOrNo");
        askingHandler.start();


        synchronized(this) {
            while(isBusy) {
                System.out.println("Waiting for " + getUserName() + " to YesOrNo...");
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println(getUserName() + " disconnected.");
                    god.removePlayer(this);
                }
            }
        }


        System.out.println(getUserName() + " says: " + answerIsYes);
        return answerIsYes;

    }

    /**
     * The Answer of asking, is yes.
     * false, when answer is no.
     */
    boolean answerIsYes;

    /**
     * End Asking of yes or no.
     *
     * @param answerIsYes the answer is yes
     */
    public void endYesOrNo(boolean answerIsYes) {

        synchronized(Player.this) {
            isBusy = false;
            Player.this.notify();
        }

        this.answerIsYes = answerIsYes;
        isBusy = false;

    }


    /**
     * Night act, based on role.
     */
    public void nightAct() {

        if (this.role.actQuestion() == null){
            sendToClient("Just wait and try to hold on :D");
            return;
        }

        isBusy = true;

        if (role instanceof Bulletproof) {
            answerOfWho = -2;
            AskingHandler askingHandler = new AskingHandler(god, this, "Bulletproof");
            askingHandler.start();
            return;
        }

        sendToClient(role.actQuestion() + "\n-1: Nobody");

        AskingHandler askingHandler = new AskingHandler(god, this, "Night");
        askingHandler.start();

    }

    /**
     * Notify others.
     *
     * @param massage the massage
     */
    public void notifyOthers(String massage){
        god.notifyEverybody(massage, this);
    }


    /**
     * Send to client.
     *
     * @param playerListens the player listens to this massage
     */
    public void sendToClient(String playerListens){

        try {
            out.writeUTF(playerListens);
        }
        catch (IOException e) {
            System.out.println(getUserName() + " disconnected.");
            god.removePlayer(this);
        }

    }

    /**
     * Mute player for a day, by command of Psychic
     */
    public void mute() {
        isSilent = true;
    }

    /**
     * Suggest watching game, when player dies.
     */
    public void suggestWatching() {

        sendToClient("You're Dead!\nDo You Wanna Watch Game? [yes, no]");

        isBusy = true;

        AskingHandler askingHandler = new AskingHandler(god, this, "Watch");
        askingHandler.start();

    }

    /**
     * End player when we're done with.
     */
    public void end() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}