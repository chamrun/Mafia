import java.io.Serializable;
import java.util.HashMap;

public class Backup implements Serializable {
    private String title;
    private HashMap<String, Role> map;

    public Backup(String title){
        this.title = title;
        map = new HashMap<>();
    }

    public void addToMap(String name, Role role){
        map.put(name, role);
    }

    public void description() {
        System.out.print(title + ": " + map.size() + " players: ");
        for (String n: map.keySet()){
            System.out.print(n + " | ");
        }
        System.out.println();
    }

    public int nPlayers(){
        return map.size();
    }

    public boolean nameExists(String name){

        for (String n: map.keySet()){

            if (name.equals(n))
                return true;

        }

        return false;
    }

    public Role getRole(String name){
        return map.get(name);
    }

}
