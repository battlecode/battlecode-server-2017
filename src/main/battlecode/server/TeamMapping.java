package battlecode.server;

import battlecode.common.Team;

/**
 * This class is used to hold information regarding mapping team ids
 * to Team enum
 */
public class TeamMapping {

    private final String teamAName;
    private final String teamBName;
    private final byte teamAID;
    private final byte teamBID;

    public TeamMapping(String teamAName, String teamBName){
        this.teamAName = teamAName;
        this.teamBName = teamBName;
        this.teamAID = 1;
        this.teamBID = 2;
    }

    public TeamMapping(GameInfo gameInfo){
        this(gameInfo.getTeamA(), gameInfo.getTeamB());
    }

    public String getTeamAName(){
        return teamAName;
    }

    public String getTeamBName(){
        return teamBName;
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
