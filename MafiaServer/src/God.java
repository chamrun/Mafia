import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class God {



    public static final String PURPLE = "\033[0;35m";
    public static final String GREEN = "\033[0;32m";
    public static final String RESET = "\033[0m";

    public static final String COLOR_RESET = "\u001B[0m";
    public static final String COLOR = "\u001B[33m" + "\u001B[40m";


    ArrayList<Player> actives = new ArrayList<>();
    ArrayList<Player> watchers = new ArrayList<>();

    boolean isChatOn = false;
    private boolean booleanAnswer = false;


    public void addActive(ServerSocket server){

        try {
            Socket socket = server.accept();
            System.out.println("New Client Connected.");

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Player player = new Player(in, out, socket);
            player.start();

        }
        catch (SocketException e){
            System.out.println("Client Disconnected.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }



    class Player extends Thread {


        private String name;
        public Role role;
        private boolean isInChat = false;
        private boolean isVoteTime = false;
        public boolean isAskingYes = false;
        private boolean isNightActing = false;

        private int nVotes = 0;

        public void addVote(){
            nVotes++;
        }

        public void resetVote(){
            nVotes = 0;
        }

        public int getNVote(){
            return nVotes;
        }

        DataInputStream in;
        DataOutputStream out;
        Socket socket;

        public Player(DataInputStream in, DataOutputStream out, Socket socket) {
            this.in = in;
            this.out = out;
            this.socket = socket;
        }

        @Override
        public void run() {

            try {

                name = in.readUTF();

                while (nameIsUsed(name)) {
                    out.writeUTF("BadName");
                    name = in.readUTF();
                }

                out.writeUTF("GoodName");

                System.out.println(name + " registered.\n");
                notifyActives((actives.size() + 1) + " actives.");
                actives.add(this);

                while (true) {

                    long start = System.currentTimeMillis();
                    long end = start + 20000;

                    while (isInChat) {

                        String clientSays = in.readUTF();

                        if (clientSays.equals("OVER")) {
                            sendToClient(PURPLE + "You left chatroom.\n" + RESET);
                            notifyOthers(PURPLE + name + " left chatroom." + RESET);
                            isInChat = false;

                            if (chatroomIsEmpty()){
                                System.out.println("Chatroom is empty.");
                                synchronized(God.this) {
                                    isChatOn = false;
                                    God.this.notify();
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

                            synchronized(God.this) {
                                isChatOn = false;
                                God.this.notify();
                            }

                            break;
                        }
                    }

                    if (isVoteTime) {

                        out.writeUTF("Index: ");
                        try {
                            int voteIndex = Integer.parseInt(in.readUTF());

                            while (voteIndex < 0 || actives.size() <= voteIndex
                                    || actives.get(voteIndex).equals(this)){

                                out.writeUTF("NOT VALID! Try Again: ");
                                voteIndex = Integer.parseInt(in.readUTF());
                            }

                            if (isVoteTime) {
                                actives.get(voteIndex).addVote();
                                notifyOthers(name + " voted to: " +
                                        PURPLE + actives.get(voteIndex).getUserName() + RESET);
                            }
                            else {
                                out.writeUTF("Unfortunately you're late and your vote wasn't counted.");
                            }
                        }
                        catch (NumberFormatException e) {
                            System.out.println();
                        }

                    }

                    if (isAskingYes){
                        out.writeUTF("You Have 30 seconds to answer...");
                        Thread.sleep(30000);
                    }

                    if (isNightActing){

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
                actives.remove(this);
            }
            catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public String getUserName() {
            return name;
        }

        public void act(){
            role.act();
        }

        public void introduction() {
            String massage = "Your role is: " + this.role.getName() + "\n";

            if (role instanceof Mayor){
                for (Player h: actives) {
                    if (h.role instanceof CityDoctor) {
                        massage += "Doctor of City is " + h.getUserName() + "\n";
                        sendToClient(massage);
                        return;
                    }
                }
            }

            if (!(role instanceof Mafia)) {
                sendToClient(massage);
                return;
            }

            StringBuilder massageBuilder = new StringBuilder(massage);
            for (Player h: actives) {
                if (h.role instanceof Mafia && !h.equals(this)){
                    massageBuilder.append(h.getUserName()).append(" is ").append(h.role.getName()).append("\n");
                }
            }

            massage = massageBuilder.toString();
            sendToClient(massage);


        }

        public void joinChat() {

            isInChat = true;
            System.out.println(getUserName() + " Joined Chat.");
            sendToClient("TALK!");
            sendToClient("Day is Started! You Can chat for 5 minutes. Send OVER if you're done.");

        }

        public void notifyOthers(String massage){
            for (Player h: actives){
                if (!h.equals(this)){
                    h.sendToClient(massage);

                }
            }
        }


        public void sendToClient(String playerListens){
            try {
                out.writeUTF(playerListens);
            }
            catch (SocketException e){
                System.out.println(name + " disconnected.");
                actives.remove(this);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void vote() throws IOException {

            isVoteTime = true;

            sendToClient("TALK!");

            StringBuilder massage = new StringBuilder("Who do you vote? (Enter number)\n");


            for (int index = 0; index < actives.size(); index++) {
                if (!actives.get(index).equals(this)){
                    massage.append(index).append(". ").append(actives.get(index).getUserName()).append("\n");
                }
            }
            sendToClient(massage.toString());

        }

    }

    private boolean chatroomIsEmpty() {
        for (Player p: actives) {
            if (p.isInChat){
                return false;
            }
        }

        return true;
    }

    public boolean nameIsUsed(String name) {
        for (Player p: actives){
            if (p.getUserName().equals(name)){
                return true;
            }
        }
        return false;
    }


    public void notifyActives(String sayToPlayers) {

        for (Player p: actives) {

            p.sendToClient(sayToPlayers);

        }
    }

    public void setRandomRoles() {

        ArrayList<Role> roles = importantRoles();
        Collections.shuffle(roles);

        for (Player p: actives) {

            p.role = roles.get(0);
            roles.remove(0);

        }

    }


    public ArrayList<Role> importantRoles() {
        int nRoles = actives.size();

        ArrayList<Role> roles = new ArrayList<>();

        roles.add(new GodFather());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new CityDoctor());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new Detective());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new Sniper());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new DoctorLector());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new SimpleCitizen());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new Mayor());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new Psychic());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new SimpleMafia());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new Bulletproof());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        while (nRoles != 0){
            if (nRoles % 3 == 0){
                roles.add(new SimpleMafia());
            }
            else {
                roles.add(new SimpleCitizen());
            }

            nRoles--;
        }

        return roles;
    }

    public void turnFirstNight() {
        for (Player p: actives){
            p.introduction();
        }
    }

    public void turnDay() throws InterruptedException {

        isChatOn = true;

        System.out.println("Day started.");

        for (Player p: actives) {

            p.joinChat();

        }

        synchronized(this) {
            while(isChatOn) {
                System.out.println("Waiting for chat to end...");
                wait();
            }

            System.out.println("Chat is done.");
        }

    }

    public void election() throws InterruptedException {


        for (Player p: actives) {

            try {
                p.vote();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        Thread.sleep(10000);

        for (Player p: actives) {
            p.isVoteTime = false;
        }

        Player toDie = null;
        int maxVote = 0;

        for (Player p: actives) {
            System.out.println(p.getUserName() + "(" + p.getNVote() + ")...");

            if (maxVote == p.getNVote()){
                toDie = null;
            }

            if (maxVote < p.getNVote()){
                toDie = p;
                maxVote = p.getNVote();
            }
        }

        if (toDie == null){
            System.out.println("No one has enough vote.");
            return;
        }

        if (mayorCancels()) {
            System.out.println("Mayor canceled election.");
            return;
        }

        System.out.println("Gonna kill " + toDie.getUserName());

        kill(toDie);

    }

    private void kill(Player toDie) {

        actives.remove(toDie);
        notifyActives(PURPLE + toDie.name + RESET + " Died!");

        toDie.sendToClient("You're Dead!\nDo You Wanna Watch Game? [yes, no]");

        if (saysYes(toDie)){
            watchers.add(toDie);
        }
    }

    private boolean mayorCancels(){

        for (Player p: actives) {
            if (p.role instanceof Mayor) {
                p.sendToClient("Do You Want to Cancel Election? [yes, no]");
                return saysYes(p);
            }
        }

        return false;
    }

    private boolean saysYes(Player player){
        player.isAskingYes = true;
        player.sendToClient("TALK!");

        try {

            String answer = player.in.readUTF();

            while (!(answer.equalsIgnoreCase("yes"))
                && !(answer.equalsIgnoreCase("no"))){

                player.sendToClient("Invalid! Write \"yes\" or \"no\".");
                answer = player.in.readUTF();

            }

            if (answer.equalsIgnoreCase("yes")) {
                return true;
            }
            else if (answer.equalsIgnoreCase("no")){
                return false;
            }
        }
        catch (SocketException e){
            System.out.println("Mayor disconnected.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }



    public void turnNight() {
        for (Player p: actives){
            p.act();
        }
    }

    public boolean gameIsOver() {

        /*

        int nMafia = 0, nCitizen = 0;

        for (Handler h: actives) {

            if (h.role instanceof Mafia){
                nMafia++;
            }
            else {
                nCitizen++;
            }

        }

        if (nMafia == 0){
            displayFinalResult(false);
            return true;
        }

        if (nCitizen <= nMafia){

            displayFinalResult(true);
            return true;
        }

         */


        return false;

    }

    private void displayFinalResult(boolean mafiaWon) {

        if (mafiaWon){
            System.out.println();
        }
        else {
            System.out.println();
        }

    }
}

