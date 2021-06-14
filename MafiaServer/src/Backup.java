import java.io.Serializable;
import java.util.HashMap;

/**
 * The Backup, saves all we need to continue an old game.
 */
public class Backup implements Serializable {
    private final String title;
    private final HashMap<String, Role> map;

    /**
     * Instantiates a new Backup.
     *
     * @param title the title we choose for this backup
     */
    public Backup(String title){
        this.title = title;
        map = new HashMap<>();
    }

    /**
     * Add to map.
     *
     * @param name the name of player
     * @param role the role of player
     */
    public void addToMap(String name, Role role){
        map.put(name, role);
    }

    /**
     * Description of backup
     */
    public void description() {
        System.out.print(title + ": " + map.size() + " players: ");
        for (String n: map.keySet()){
            System.out.print(n + " | ");
        }
        System.out.println();
    }

    /**
     * @return Number of players in saved game.
     */
    public int nPlayers(){
        return map.size();
    }

    /**
     * Name exists boolean.
     *
     * @param name the name we're looking for
     * @return the boolean: true if name is, false if isn't
     */
    public boolean nameExists(String name){

        for (String n: map.keySet()){

            if (name.equals(n))
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
        return map.get(name);
    }

}
