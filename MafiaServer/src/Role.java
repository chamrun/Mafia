interface Role {
    void act();
    String getName();

}

abstract class Mafia implements Role {

}

class GodFather extends Mafia{

    @Override
    public void act() {

    }

    @Override
    public String getName() {
        return "GodFather";
    }
}

class DoctorLector extends Mafia{

    @Override
    public void act() {
        // healing Someone
    }

    @Override
    public String getName() {
        return "Doctor Lector";
    }
}

class SimpleMafia extends Mafia{

    @Override
    public void act() {
        // healing Someone
    }

    @Override
    public String getName() {
        return "Simple Mafia";
    }
}


abstract class Citizen implements Role{
}

class CityDoctor extends Citizen{

    @Override
    public void act() {

    }

    @Override
    public String getName() {
        return "City Doctor";
    }
}

class Mayor extends Citizen{


    public boolean cancelElection(){
        if (true) {
            return true;
        }

        else return false;
    }

    @Override
    public void act() {
    }

    @Override
    public String getName() {
        return "Mayor";
    }
}

class Detective extends Citizen{

    @Override
    public void act() {
    }

    @Override
    public String getName() {
        return "Detective";
    }
}


class Psychic extends Citizen{


    @Override
    public void act() {
    }

    @Override
    public String getName() {
        return "Psychic";
    }
}


class Bulletproof extends Citizen{

    boolean isShot = false;


    @Override
    public void act() {
    }

    @Override
    public String getName() {
        return "Bulletproof";
    }
}

class Sniper extends Citizen{

    boolean hasBullet = true;

    @Override
    public void act() {
    }

    @Override
    public String getName() {
        return "Sniper";
    }
}

class SimpleCitizen extends Citizen{


    @Override
    public void act() {
    }

    @Override
    public String getName() {
        return "Simple Citizen";
    }
}





