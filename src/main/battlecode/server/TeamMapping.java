package battlecode.server;

import battlecode.common.Team;

/**
 * This class is used to hold information regarding mapping team ids
 * to Team enum
 */
public class TeamMapping {

    private final int teamAID;
    private final int teamBID;

    public TeamMapping(int teamA, int teamB){
        this.teamAID = teamA;
        this.teamBID = teamB;
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

    public int getIDFromTeam(Team t){
        if(t == Team.A){
            return teamAID;
        }else if(t == Team.B){
            return teamBID;
        }else{
            return -1;
        }
    }

}
