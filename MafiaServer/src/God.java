import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class God {



    public static final String PURPLE = "\033[0;35m";
    public static final String RESET = "\033[0m";

    /*
    public static final String GREEN = "\033[0;32m";
    public static final String COLOR_RESET = "\u001B[0m";
    public static final String COLOR = "\u001B[33m" + "\u001B[40m";
     */

    ArrayList<Player> actives = new ArrayList<>();
    ArrayList<Player> watchers = new ArrayList<>();

    boolean waiting = false;


    public void addActive(ServerSocket server){

        try {

            Socket socket = server.accept();
            System.out.println("New Client Connected.");

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Player player = new Player(this, in, out, socket);
            player.start();

        }
        catch (SocketException e){
            System.out.println("Client Disconnected.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void notifyWatchers(String sayToPlayers) {

        for (Player p: watchers) {

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


    private ArrayList<Role> importantRoles() {
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

    public void startChatroom() throws InterruptedException {
        waiting = true;

        for (Player p: actives) {

            p.joinChat();

        }

        synchronized(this) {
            while(waiting) {
                System.out.println("Waiting for chat to end...");
                wait();
            }

            System.out.println("Chat is done.");
        }

    }

    public void stopWaiting() {

        synchronized(God.this) {
            waiting = false;
            God.this.notify();
        }

    }

    public void election() throws InterruptedException {
        waiting = true;

        for (Player p: actives) {

            try {
                p.vote();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        synchronized(this) {
            while(waiting) {
                System.out.println("Waiting for Voting to end...");
                wait();
            }

            System.out.println("Election is done.");
        }

        for (Player p: actives) {
            if (p.getAnswerOfWho() != -1) {
                System.out.println(p.getAnswerOfWho() != -1);
                System.out.println("v:" + p.getAnswerOfWho());

                Player target = actives.get(p.getAnswerOfWho());
                target.addVote();
                p.notifyOthers(p.getUserName() + " voted to: " + PURPLE + target.getUserName() + RESET);

            }
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
            notifyActives("Mayor canceled election.");
            System.out.println("Mayor canceled election.");
            return;
        }

        System.out.println("Gonna kill " + toDie.getUserName());

        kill(toDie);

    }

    public void notifyActives(String sayToPlayers) {

        for (Player p : actives) {

            p.sendToClient(sayToPlayers);

        }

    }

    public boolean nobodyIsBusy() {

        for (Player p: actives) {
            if (p.isBusy()){
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

    public String getUserName(int index){
        return actives.get(index).getUserName();
    }

    private Player getPlayer(String role){

        for (Player p: actives) {
            if (p.role.getName().equals(role)) {
                return p;
            }
        }

        return null;
    }

    private void kill(Player toDie) {

        actives.remove(toDie);
        notifyActives(PURPLE + toDie.getUserName() + RESET + " Died!");

        toDie.sendToClient("You're Dead!\nDo You Wanna Watch Game? [yes, no]");

        if (saysYes(toDie)){
            watchers.add(toDie);
        }
    }

    private boolean mayorCancels(){


        Player mayor = getPlayer("Mayor");

        if (mayor == null){
            return false;
        }

        mayor.sendToClient("Do You Want to Cancel Election? [yes, no]");
        return saysYes(mayor);

    }

    private boolean saysYes(Player player){

        player.isBusy = true;
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

    public void turnNight() throws InterruptedException {

        Player killed = null;
        Player lectorSaved = null;
        Player cityDrSaved = null;
        Player silent = null;
        Player sniped = null;
        Player onDetect = null;


        for (Player p: actives){
            p.act();
        }


        Thread.sleep(10000);


        for (Player p: actives){
            switch (p.getRoleNAme()){
                case "GodFather":
                    killed = actives.get(p.getAnswerOfWho());
                    break;

                case "Doctor Lector":
                    lectorSaved = actives.get(p.getAnswerOfWho());
                    break;

                case "City Doctor":
                    cityDrSaved = actives.get(p.getAnswerOfWho());
                    break;

                case "Psychic":
                    silent = actives.get(p.getAnswerOfWho());
                    break;

                case "Sniper":
                    sniped = actives.get(p.getAnswerOfWho());
                    if (sniped.role instanceof Citizen){
                        kill(p);
                    }
                    break;

                case "Detective":
                    onDetect = actives.get(p.getAnswerOfWho());
                    break;

                default:
                    p.sendToClient("You Don't have to act now. /n" +
                            "just wait for the night to end and try to hold on :D");
                    break;
            }
        }

        if (killed != null && !killed.equals(cityDrSaved)){
            if (killed.role instanceof Bulletproof){
                if (((Bulletproof)killed.role).isShot){
                    kill(killed);
                }
                else {
                    ((Bulletproof)killed.role).isShot = true;
                }
            }
            else {
                kill(killed);
            }
        }

        if (sniped != null && !sniped.equals(lectorSaved)){
            kill(sniped);
        }

        if (silent != null){
            silent.mute();
        }

        if (onDetect != null) {
            detectionResult(onDetect);
        }

    }

    private void detectionResult(Player onDetect) {

        Player detective = getPlayer("Detective");

        if (detective == null){
            return;
        }

        if (onDetect.role instanceof GodFather) {
            if (((GodFather) onDetect.role).hasBeenDetectedBefore) {
                detective.sendToClient(": Mafia");
            }
            else {
                ((GodFather) onDetect.role).hasBeenDetectedBefore = false;
            }
        }
        else {
            if (onDetect.role instanceof Mafia) {
                detective.sendToClient(onDetect.getUserName() + " is Mafia");
            }
            else {
                detective.sendToClient(onDetect.getUserName() + " is Citizen");
            }
        }
    }


    public boolean gameIsNotOver() {

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
            return false;
        }

        if (nCitizen <= nMafia){

            displayFinalResult(true);
            return false;
        }

         */


        return true;

    }

    private void displayFinalResult(boolean mafiaWon) {

        if (mafiaWon){
            System.out.println("Mafia Won.");
        }
        else {
            System.out.println("City Won.");
        }

    }

    public void send(Player from, String massage) {
        for (Player p: actives){
            if (!p.equals(from)){
                p.sendToClient(massage);
            }
        }

        notifyWatchers(massage);
    }

    public int nActives(){
        return actives.size();
    }

    public String getMafiaList(Player requester) {
        if (requester.role instanceof Citizen){
            return "Access Denied.";
        }

        else {
            StringBuilder massageBuilder = new StringBuilder();
            for (Player p: actives) {
                if (p.role instanceof Mafia && !p.equals(requester)){
                    massageBuilder.append(p.getUserName()).append(" is ").append(p.role.getName()).append("\n");
                }
            }
            return massageBuilder.toString();
        }
    }

    public String whoIsCityDoctor(Player requester){

        if (!(requester.role instanceof Mayor)){
            return "Access denied.";
        }

        Player cityDoctor = getPlayer("City Doctor");

        if (cityDoctor == null){
            return "No doctor was found in the city.";
        }

        return "Doctor of City is " + getPlayer("City Doctor") + "\n";
    }

    public void addPlayer(Player player) {
        actives.add(player);
    }

    public void removePlayer(Player player) {
        actives.remove(player);
    }


}

