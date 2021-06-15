import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * The God contains all sensitive information of a running game.
 */
public class God {


    /**
     * The constant PURPLE to color names. for beautifier and cleaner console.
     */
    public static final String PURPLE = "\033[0;35m";
    public static final String RESET = "\033[0m";

    private final ServerSocket server;

    private final ArrayList<Player> actives;
    private final ArrayList<Player> watchers;
    /**
     * a flag to know wait or run. it's true when god is waiting for chat, vote or night to end.
     */
    boolean waiting;

    /**
     * it's used in the end of game.
     */
    private String finalReport;

    /**
     * Instantiates a new God.
     */
    public God(ServerSocket server) {
        this.server = server;
        actives = new ArrayList<>();
        watchers = new ArrayList<>();
        waiting = false;
    }

    /**
     * Adds an active Client to game, when it game is gonna start.
     *
     * @param server the server that's gonna accept new Client.
     */
    public void addActive(ServerSocket server){

        try {

            Socket socket = server.accept();
            System.out.println("New Client Connected.");

            Player player = new Player(this, socket);
            player.run();

        }
        catch (IOException e) {
            System.out.println("Client Disconnected.");
        }

    }

    /**
     * Adds active Clients, when we are loading a backup.
     *
     * @param server the server
     * @param backup the backup
     */
    public void addActive(ServerSocket server, Backup backup) {

        try {

            Socket socket = server.accept();
            System.out.println("New Client Connected.");

            Player player = new Player(this, socket);
            player.run(backup);

        }
        catch (IOException e) {
            System.out.println("Client Disconnected.");
        }

    }


    /**
     * Add player to Arraylist of activePlayers.
     *
     * @param player the new player which is completed.
     */
    public void addPlayer(Player player) {
        actives.add(player);
    }

    /**
     * Removes a player that has been killed or disconnected.
     *
     * @param player the player we should say goodbye to!
     */
    public void removePlayer(Player player) {
        actives.remove(player);
        player.end();
    }

    /**
     * Sets random roles.
     */
    public void setRandomRoles() {

        ArrayList<Role> roles = importantRoles();
        Collections.shuffle(roles);

        for (Player p: actives) {
            p.role = roles.get(0);
            roles.remove(0);
        }

    }

    /**
     * based on the number of players,
     * @return an arrayList of roles to be given to players.
     */
    private ArrayList<Role> importantRoles() {
        int nRoles = actives.size();

        ArrayList<Role> roles = new ArrayList<>();


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

        roles.add(new GodFather());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new Sniper());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new SimpleCitizen());
        nRoles--;
        if (nRoles == 0){
            return roles;
        }

        roles.add(new DoctorLector());
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

    /**
     * Turn first night and sends players their roles.
     */
    public void turnFirstNight() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Player p: actives){
            p.introduction();
            stringBuilder.append(p.getUserName()).append(" :: ").append(p.role.getName()).append("\n");
        }

        finalReport = stringBuilder.toString();
        System.out.println(finalReport);
    }

    /**
     * Turn first night and sends players their roles.
     */
    public void turnFirstNight(String finalReport) {
        this.finalReport = finalReport;

        for (Player p: actives){
            p.introduction();
        }
        System.out.println(finalReport);
    }

    /**
     * Start chatroom in the day.
     */
    public void startChatroom() {
        waiting = true;

        for (Player p: actives) {
            p.joinChat();
        }

        synchronized(this) {
            while(waiting) {
                System.out.println("Waiting for chat to end...");
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Chat is done.");
        }
    }


    /**
     * Stop waiting when chat, election or night is done.
     */
    public void stopWaiting() {

        synchronized(God.this) {
            waiting = false;
            God.this.notify();
        }
    }

    /**
     * Election.
     */
    public void election() {
        waiting = true;

        for (Player p: actives) {
            p.vote(30000);
        }

        keepWaiting();
        notifyEverybody("Election is done.");

        for(Player p: actives){
            if (p.isWaitingForInput()){
                p.sendToClient("Time's up");
            }
        }

        for (Player p: actives) {
            int answer = p.getAnswerOfWho();

            if (answer != -1) {

                try {
                    Player target = actives.get(answer);
                    target.addVote();
                    p.notifyOthers(p.getUserName() + " voted to: " + PURPLE + target.getUserName() + RESET);
                }
                catch (IndexOutOfBoundsException e){
                    System.out.println(p.getUserName() + " didn't vote.");
                }

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

        for (Player p: actives){
            p.resetVote();
        }

        if (toDie == null){
            System.out.println("No one has enough vote.");
            notifyEverybody("No one has enough vote.\n");
            return;
        }

        if (mayorCancels()) {
            notifyEverybody("Mayor canceled election.\n");
            System.out.println("Mayor canceled election.");
            return;
        }

        System.out.println("Gonna kill " + toDie.getUserName());

        kill(toDie);
    }

    private void keepWaiting() {
        synchronized(this) {
            while(waiting) {
                System.out.println("Waiting for acts to end...");
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Acts are done.");
        }
    }

    /**
     * Turn night.
     * Everyone acts, based on his role.
     * than results will be shown and night will end.
     */
    public void turnNight() {
        waiting = true;

        Player killed = null;
        Player lectorSaved = null;
        Player cityDrSaved = null;
        Player silent = null;
        Player sniped = null;
        Player onDetect;
        boolean inquiry = false;


        for (Player p: actives) {
            p.nightAct();
        }

        keepWaiting();
        notifyEverybody("Night is done.");

        for (Player p: actives){
            int answer = p.getAnswerOfWho();

            if (answer != -1) {

                    switch (p.getRoleNAme()) {
                    case "GodFather":
                        killed = actives.get(answer);
                        break;

                    case "Doctor Lector":
                        lectorSaved = actives.get(answer);
                        break;

                    case "City Doctor":
                        cityDrSaved = actives.get(answer);
                        break;

                    case "Psychic":
                        silent = actives.get(answer);
                        break;

                    case "Sniper":
                        if (((Sniper)p.role).hasBullet()) {
                            sniped = actives.get(answer);
                            ((Sniper)p.role).shot();
                        }
                        break;

                    case "Detective":
                        onDetect = actives.get(answer);
                        if (onDetect != null) {
                            detectionResult(onDetect);
                        }
                        break;

                    case "Bulletproof":
                        System.out.println(p.getUserName() + "...");
                        if (((Bulletproof) p.role).canInquiry()) {
                            System.out.println("Yes:1:...");
                            inquiry = p.answerIsYes;
                            if (inquiry) {
                                System.out.println("Yes:2:...");
                                ((Bulletproof) p.role).inquiry();
                            }
                        }

                        break;

                    default:
                        System.out.println(p.getRoleNAme() + "Unreachable statement in night.");
                        break;
                }
            }
        }
        System.out.println("In: " + inquiry);

        if (cityDrSaved != null && cityDrSaved.role instanceof CityDoctor){

            if (((CityDoctor)cityDrSaved.role).hasSavedCityDrBefore()){
                cityDrSaved = null;
            }
            else {
                ((CityDoctor)cityDrSaved.role).saveCityDr();
            }

        }


        if (killed != null && !killed.equals(cityDrSaved)){
            if (killed.role instanceof Bulletproof){
                if (((Bulletproof)killed.role).isShot()){
                    kill(killed);
                }
                else {
                    ((Bulletproof)killed.role).shot();
                }
            }
            else {
                kill(killed);
            }
        }


        if (sniped != null){

            if (sniped.role instanceof Citizen) {
                kill(getPlayer("Sniper"));
            }
            else if (!sniped.equals(lectorSaved)){
                kill(sniped);
            }
        }

        if (silent != null){
            silent.mute();
            notifyEverybody(silent.getUserName() + " is silent today =)", silent);
            silent.sendToClient("You've been silenced last night! Wait till end of chat...");
        }

        if (inquiry){
            int nMafia = 0;
            int nCitizen = 0;

            for (Player p: actives) {
                if (p.role instanceof Mafia){
                    nMafia++;
                }
                else {
                    nCitizen++;
                }
            }

            notifyEverybody("Alive citizens: " + nCitizen + "\nAlive Mafias: " + nMafia);

        }

    }

    /**
     * Notify all of active players and watchers..
     *
     * @param massage the massage that we wanna say to everybody.
     */
    public void notifyEverybody(String massage) {

        for (Player p : actives) {
            p.sendToClient(massage);
        }

        for (Player p: watchers) {
            p.sendToClient(massage);
        }

    }

    /**
     * Notify everybody except the player which massage is about.
     *
     * @param massage the massage
     * @param except  the person who won't get this massage
     */
    public void notifyEverybody(String massage, Player except) {

        try {
            for (Player p: actives){
                if (!p.equals(except)){
                    p.sendToClient(massage);
                }
            }

            for (Player p: watchers) {
                p.sendToClient(massage);
            }

        }
        catch (ConcurrentModificationException ignored){}
    }

    /**
     * When someone ends an action. we call this method to check if anyone is bury or no
     * to stop waiting if everyone's done.
     *
     * @return true if everybody is waiting, false if there's anyone still busy.
     */
    public boolean nobodyIsBusy() {

        for (Player p: actives) {
            if (p.isBusy()){
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a name has been used before or not.
     *
     * @param name the name we wanna check
     * @return true if name is used
     *         false if name id not used before
     */
    public boolean nameIsUsed(String name) {
        if (name.equals("")){
            return true;
        }
        for (Player p: actives){
            if (p.getUserName().equals(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * Get user name string.
     *
     * @param index the index of player
     * @return the name of player
     */
    public String getUserName(int index){
        if (index == -1){
            return "";
        }
        return actives.get(index).getUserName();
    }

    /**
     * @param role we give this method a role
     * @return and if that role is in game, returns it to us.
     */
    private Player getPlayer(String role){

        for (Player p: actives) {
            if (p.role.getName().equals(role)) {
                return p;
            }
        }

        return null;
    }

    /**
     * kills player
     * @param toDie the player who's taking last breathes
     */
    private void kill(Player toDie) {

        try {

            actives.remove(toDie);
            notifyEverybody(PURPLE + toDie.getUserName() + RESET + " Died!\n");
            watchers.add(toDie);

            if (!gameIsOver())
                toDie.suggestWatching();
        }
        catch (NullPointerException e){
            System.out.println("Player has been killed before!");
        }
    }

    /**
     * asks mayor if wants to cancel election or no.
     * @return answer of mayor.
     */
    private boolean mayorCancels(){


        Player mayor = getPlayer("Mayor");

        if (mayor == null){
            return false;
        }

        return mayor.askYesOrNo("Do You Want to Cancel Election?");

    }

    /**
     * says to detection that his selected one, is citizen or mafia.
     * @param onDetect the person who detective asked about.
     */
    private void detectionResult(Player onDetect) {
        System.out.println("Detecting on " + onDetect.getUserName() + "...");

        Player detective = getPlayer("Detective");

        if (detective == null){
            return;
        }

        if (onDetect.role instanceof GodFather) {
            if (((GodFather) onDetect.role).hasBeenDetectedBefore()) {
                detective.sendToClient(onDetect.getUserName() + " is Mafia.");
            }
            else {
                detective.sendToClient(onDetect.getUserName() + " is Citizen.");
                ((GodFather) onDetect.role).detect();
            }
        }
        else {
            if (onDetect.role instanceof Mafia) {
                detective.sendToClient(onDetect.getUserName() + " is Mafia.");
            }
            else {
                detective.sendToClient(onDetect.getUserName() + " is Citizen.");
            }
        }
    }

    /**
     * @return the number od active players.
     */
    public int nActives(){
        return actives.size();
    }

    /**
     * Gets mafia list.
     *
     * @param requester the requester, to be checked and make sure that he's a mafia
     * @return the mafia list
     */
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

    /**
     * Who is city doctor string.
     *
     * @param requester the requester, cuz should be the Mayor.
     * @return result that who is city Doctor.
     */
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

    /**
     * Game is over boolean.
     *
     * @return the game is over or not.
     */
    public boolean gameIsOver() {

        int nMafia = 0, nCitizen = 0;

        for (Player p: actives) {

            if (p.role instanceof Mafia){
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

        return false;

    }

    /**
     * Shows final result of game to all of players and watchers.
     * @param mafiaWon if mafia won, ir not (== or citizens are won)
     */
    private void displayFinalResult(boolean mafiaWon) {

        if (mafiaWon){
            System.out.println("Mafia Won.");
            notifyEverybody("\nMafia won the city!");
        }
        else {
            System.out.println("City Won.");
            notifyEverybody("\nCitizens are won!");
        }

        System.out.println(finalReport);
        notifyEverybody(finalReport);

        endGame();
    }

    /**
     * End game and closes all players, their sockets and these kind of stuff.
     */
    public void endGame() {

        int n = actives.size();
        for (int i = 0; i < n; i++){
            removePlayer(actives.get(0));
        }

        n = watchers.size();
        for (int i = 0; i < n; i++){
            removePlayer(watchers.get(0));
        }

        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Suggest god father who to kill
     *
     * @param indexOfAnswer the index of suggestion, sent by SimpleMafia
     */
    public void suggestGodFather(int indexOfAnswer) {
        if (indexOfAnswer == -1) {  // Nothing to do if there's no suggestion
            return;
        }

        Player godFather = getPlayer("GodFather");
        if (godFather == null){
            return;
        }

        godFather.sendToClient("Simple mafia suggests you to kill " + actives.get(indexOfAnswer).getUserName());
    }

    /**
     * Get backup to be saved.
     *
     * @param title the title we chose for backup
     * @return the backup
     */
    public Backup getBackUp(String title){
        Backup backup = new Backup(title, finalReport);

        for (Player p: actives){
            backup.addToMap(p.getUserName(), p.role);
        }

        return backup;
    }


}

