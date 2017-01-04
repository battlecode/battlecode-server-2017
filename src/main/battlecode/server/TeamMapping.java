package battlecode.server;

import battlecode.common.Team;

/**
 * This class is used to hold information regarding mapping team ids
 * to Team enum
 */
public class TeamMapping {

    private final String teamAName;
    private final String teamAPackage;
    private final byte teamAID;

    private final String teamBName;
    private final String teamBPackage;
    private final byte teamBID;

    public TeamMapping(String teamAName, String teamAPackage,
                       String teamBName, String teamBPackage){
        this.teamAName = teamAName;
        this.teamAPackage = teamAPackage;
        this.teamAID = 1;

        this.teamBName = teamBName;
        this.teamBPackage = teamBPackage;
        this.teamBID = 2;
    }

    public TeamMapping(GameInfo gameInfo){
        this(
                gameInfo.getTeamAName(), gameInfo.getTeamAPackage(),
                gameInfo.getTeamBName(), gameInfo.getTeamBPackage()
        );
    }

    public String getTeamAName(){
        return teamAName;
    }

    public String getTeamBName(){
        return teamBName;
    }

    public String getTeamAPackage() {
        return teamAPackage;
    }

    public String getTeamBPackage() {
        return teamBPackage;
    }

    public byte getTeamAID(){
        return teamAID;
    }

    public byte getTeamBID(){
        return teamBID;
    }

    public Team getTeamFromID(int id) {
        if(id == teamAID){
            return Team.A;
        }else if(id == teamBID){
            return Team.B;
        }else{
            return Team.NEUTRAL;
        }
    }

    public byte getIDFromTeam(Team t){
        if(t == Team.A){
            return teamAID;
        }else if(t == Team.B){
            return teamBID;
        }else{
            return -1;
        }
    }

}
