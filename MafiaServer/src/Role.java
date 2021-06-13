import java.io.Serializable;

/**
 * The interface Role.
 */
interface Role extends Serializable {
    /**
     * Act question string.
     *
     * @return the string
     */
    String actQuestion();

    /**
     * Gets name.
     *
     * @return the name
     */
    String getName();

}

/**
 * The type Mafia.
 */
abstract class Mafia implements Role{

}

/**
 * The type God father.
 */
class GodFather extends Mafia {

    private boolean detectedBefore = false;

    /**
     * Has been detected before boolean.
     *
     * @return the boolean
     */
    public boolean hasBeenDetectedBefore(){
        return detectedBefore;
    }

    /**
     * Detect.
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
}

/**
 * The type Doctor lector.
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
}

/**
 * The type Simple mafia.
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
}


/**
 * The type Citizen.
 */
abstract class Citizen implements Role{
}

/**
 * The type City doctor.
 */
class CityDoctor extends Citizen{

    private boolean savedCityDrBefore = false;

    /**
     * Has saved city dr before boolean.
     *
     * @return the boolean
     */
    public boolean hasSavedCityDrBefore() {
        return savedCityDrBefore;
    }

    /**
     * Save city dr.
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
}


/**
 * The type Bulletproof.
 */
class Bulletproof extends Citizen{

    private boolean shot = false;
    private int nInquiry = 2;

    /**
     * Is shot boolean.
     *
     * @return the boolean
     */
    public boolean isShot() {
        return shot;
    }

    /**
     * Shot.
     */
    public void shot(){
        shot = true;
    }

    /**
     * Inquiry.
     */
    public void inquiry(){
        nInquiry--;
    }

    /**
     * Can inquiry boolean.
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
}

/**
 * The type Sniper.
 */
class Sniper extends Citizen{

    private boolean hasBullet = true;

    /**
     * Has bullet boolean.
     *
     * @return the boolean
     */
    public boolean hasBullet(){
        return hasBullet;
    }

    /**
     * Shot.
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
}





