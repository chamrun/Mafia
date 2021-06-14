import java.io.Serializable;

/**
 * The interface Role.
 */
interface Role extends Serializable {
    /**
     * Act question string.
     *
     * @return the string that we'll be asked when role is acting in night
     */
    String actQuestion();

    /**
     * Gets name.
     *
     * @return the name of role
     */
    String getName();

    String description();
}

/**
 * The type Mafia.
 */
abstract class Mafia implements Role{

}

/**
 * The type GodFather.
 * He Can kill one person every night.
 */
class GodFather extends Mafia {

    private boolean detectedBefore = false;

    /**
     * Has been detected before boolean.
     *
     * @return true: if detective asked about this person before
     *         false: if player hasn't been on detect before
     */
    public boolean hasBeenDetectedBefore(){
        return detectedBefore;
    }

    /**
     * We call this method when detective asks about godfather for the first time.
     */
    public void detect(){
        detectedBefore = true;
    }

    @Override
    public String actQuestion() {
        return "Who do you wanna kill?";
    }

    @Override
    public String getName() {
        return "GodFather";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}

/**
 * The type Doctor lector.
 * He can save one person every night.
 */
class DoctorLector extends Mafia{

    @Override
    public String actQuestion() {
        return "Which Mafia, Do you wanna save, from sniperShot?";
    }

    @Override
    public String getName() {
        return "Doctor Lector";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}

/**
 * The type Simple mafia.
 * suggests godfather who to kill.
 */
class SimpleMafia extends Mafia{

    @Override
    public String actQuestion() {
        return "Who do you suggest to be killed by Godfather??";
    }

    @Override
    public String getName() {
        return "Simple Mafia";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}


/**
 * The type Citizen.
 */
abstract class Citizen implements Role{
}

/**
 * The type City doctor.
 * Saves one person every night.
 */
class CityDoctor extends Citizen{

    private boolean savedCityDrBefore = false;

    /**
     * Has doctor saved his self before, or not
     *
     * @return the boolean
     */
    public boolean hasSavedCityDrBefore() {
        return savedCityDrBefore;
    }

    /**
     * doctor is saving his self...
     */
    public void saveCityDr(){
        savedCityDrBefore = true;
    }

    @Override
    public String actQuestion() {
        return "Who do you save from GodfatherShot?";
    }

    @Override
    public String getName() {
        return "City Doctor";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}

/**
 * The type Mayor.
 */
class Mayor extends Citizen{

    @Override
    public String actQuestion() {
        return null;
    }

    @Override
    public String getName() {
        return "Mayor";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}

/**
 * The type Detective.
 */
class Detective extends Citizen{

    @Override
    public String actQuestion() {
        return "Whose role you want to know?";
    }

    @Override
    public String getName() {
        return "Detective";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}


/**
 * The type Psychic.
 */
class Psychic extends Citizen{


    @Override
    public String actQuestion() {
        return "Who do you wanna mute for a day?";
    }

    @Override
    public String getName() {
        return "Psychic";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}


/**
 * The type Bulletproof.
 */
class Bulletproof extends Citizen{

    private boolean shot = false;
    private int nInquiry = 2;

    /**
     * Is bulletproof shot before, or not.
     *
     * @return the boolean
     */
    public boolean isShot() {
        return shot;
    }

    /**
     * Shot bulletproof!
     */
    public void shot(){
        shot = true;
    }

    /**
     * Bulletproof asks Inquiry.
     */
    public void inquiry(){
        nInquiry--;
    }

    /**
     * Can bulletproof ask for inquiry?
     *
     * @return the boolean
     */
    public boolean canInquiry(){
        return 0 < nInquiry;
    }

    @Override
    public String actQuestion() {
        return "Do you wanna know how many Mafias and Citizens are in game?";
    }

    @Override
    public String getName() {
        return "Bulletproof";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}

/**
 * The type Sniper.
 */
class Sniper extends Citizen{

    private boolean hasBullet = true;

    /**
     * Has sniper any bullets?
     *
     * @return the boolean
     */
    public boolean hasBullet(){
        return hasBullet;
    }

    /**
     * Sniper shots!
     */
    public void shot(){
        hasBullet = false;
    }

    @Override
    public String actQuestion() {
        return "Who do you wanna kill?";
    }

    @Override
    public String getName() {
        return "Sniper";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}

/**
 * The type Simple citizen.
 */
class SimpleCitizen extends Citizen{


    @Override
    public String actQuestion() {
        return null;
    }

    @Override
    public String getName() {
        return "Simple Citizen";
    }

    @Override
    public String description() {
        return "GodFather";
    }
}





