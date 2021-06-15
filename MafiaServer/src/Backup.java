import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * The Backup, saves all we need to continue an old game.
 */
public class Backup implements Serializable {
    private final String title;
    private final HashMap<String, Role> map;
    private final String finalReport;
    private final String date;
    int numberOfPlayers;

    /**
     * Instantiates a new Backup.
     *
     * @param title the title we choose for this backup
     */
    public Backup(String title, String finalReport){
        this.title = title;
        this.finalReport = finalReport;
        map = new HashMap<>();

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        this.date = formatter.format(date);
        numberOfPlayers = 0;
    }

    /**
     * Add to map.
     *
     * @param name the name of player
     * @param role the role of player
     */
    public void addToMap(String name, Role role){
        map.put(name, role);
        numberOfPlayers = map.size();
    }

    /**
     * Description of backup
     */
    public void description() {
        System.out.print("\033[1m" + title + "\033[0m: " + date + ": " + map.size() + " players: ");
        for (String n: map.keySet()){
            System.out.print(n + " | ");
        }
        System.out.println();
    }

    /**
     * @return Number of players in saved game.
     */
    public int nPlayers(){
        return numberOfPlayers;
    }

    /**
     * Name exists boolean.
     *
     * @param name the name we're looking for
     * @return the boolean: true if name is, false if isn't
     */
    public boolean nameExists(String name){

        for (String exitingName: map.keySet()){
            if (name.equals(exitingName))
                return true;

        }

        return false;
    }

    /**
     * Get role of a player.
     *
     * @param name the name of player
     * @return the role of player with that name
     */
    public Role getRole(String name){
        Role tempRole = map.get(name);
        map.remove(name);
        return tempRole;
    }

    public String getReport() {
        return finalReport;
    }
}
