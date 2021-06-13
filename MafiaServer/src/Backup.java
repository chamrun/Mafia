import java.io.Serializable;
import java.util.HashMap;

/**
 * The type Backup.
 */
public class Backup implements Serializable {
    private final String title;
    private final HashMap<String, Role> map;

    /**
     * Instantiates a new Backup.
     *
     * @param title the title
     */
    public Backup(String title){
        this.title = title;
        map = new HashMap<>();
    }

    /**
     * Add to map.
     *
     * @param name the name
     * @param role the role
     */
    public void addToMap(String name, Role role){
        map.put(name, role);
    }

    /**
     * Description.
     */
    public void description() {
        System.out.print(title + ": " + map.size() + " players: ");
        for (String n: map.keySet()){
            System.out.print(n + " | ");
        }
        System.out.println();
    }

    /**
     * N players int.
     *
     * @return the int
     */
    public int nPlayers(){
        return map.size();
    }

    /**
     * Name exists boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean nameExists(String name){

        for (String n: map.keySet()){

            if (name.equals(n))
                return true;

        }

        return false;
    }

    /**
     * Get role role.
     *
     * @param name the name
     * @return the role
     */
    public Role getRole(String name){
        return map.get(name);
    }

}
