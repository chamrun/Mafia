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

    private boolean isInChat = false;
    public boolean isAskedYes = false;
    private boolean isSilent = false;

    private boolean isAskedWho = false;
    private int answerOfWho = -1;

    private int nVotes = 0;

    DataInputStream in;
    DataOutputStream out;
    Socket socket;

    public Player(God god, DataInputStream in, DataOutputStream out, Socket socket) {
        this.god = god;
        this.in = in;
        this.out = out;
        this.socket = socket;
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

    public boolean isInChat() {
        return isInChat;
    }

    public int getAnswerOfWho() {
        isAskedWho = false;
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

            while (true) {

                long start = System.currentTimeMillis();
                long end = start + 20000;

                while (isInChat) {

                    String clientSays = in.readUTF();

                    if (clientSays.equals("OVER")) {
                        sendToClient(PURPLE + "You left chatroom.\n" + RESET);
                        notifyOthers(PURPLE + name + RESET + " left chatroom.");
                        isInChat = false;

                        if (god.chatroomIsEmpty()){
                            System.out.println("Chatroom is empty.");
                            synchronized(Player.this) {
                                god.isChatOn = false;
                                Player.this.notify();
                            }
                            notifyOthers("Chat is done.");
                        }

                        break;
                    }

                    if (System.currentTimeMillis() < end) {
                        notifyOthers(PURPLE + name + ": " + RESET + clientSays);
                    }
                    else {
                        sendToClient(PURPLE + "ChatTime is up.\n" + RESET);
                        isInChat = false;

                        synchronized(Player.this) {
                            god.isChatOn = false;
                            Player.this.notify();
                        }

                        break;
                    }
                }

                if (isAskedWho) {

                    out.writeUTF("Index: ");
                    try {
                        int index = Integer.parseInt(in.readUTF());

                        while (index < 0 || god.nActives() <= index){
                            //Some

                            out.writeUTF("NOT VALID! Try Again: ");
                            index = Integer.parseInt(in.readUTF());
                        }

                        if (isAskedWho) {
                            answerOfWho = index;
                        }
                        else {
                            out.writeUTF("Unfortunately you're late and your vote wasn't counted.");
                        }

                    }
                    catch (NumberFormatException e) {
                        System.out.println();
                    }

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



        }
        catch (SocketException e){
            System.out.println(name + " disconnected.");
            god.removePlayer(this);
        }
        catch (IOException | InterruptedException e) {
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

        if (!(role instanceof Mafia)) {
            sendToClient(massage);
            return;
        }


        sendToClient(massage + god.getMafiaList(this));


    }

    public void joinChat() {

        if(isSilent){
            isSilent = false;
        }
        else {
            isInChat = true;
            System.out.println(getUserName() + " Joined Chat.");
            sendToClient("TALK!");
            sendToClient("Day is Started! You Can chat for 5 minutes. Send OVER if you're done.");
        }

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


    public void vote() throws IOException {

        isAskedWho = true;

        sendToClient("TALK!");

        StringBuilder massage = new StringBuilder("Who do you vote? (Enter number)\n");


        for (int index = 0; index < god.nActives(); index++) {
            massage.append(index).append(". ").append(god.getUserName(index)).append("\n");
        }
        sendToClient(massage.toString());

    }

    public int act() {

        isAskedWho = true;

        sendToClient("TALK!");

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

    public void mute() {
        isSilent = true;
    }
}