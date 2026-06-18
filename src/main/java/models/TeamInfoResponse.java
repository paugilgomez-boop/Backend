package models;

import java.util.List;

public class TeamInfoResponse {
    private String team;
    private List<TeamMember> members;

    public TeamInfoResponse() {
    }

    public TeamInfoResponse(String team, List<TeamMember> members) {
        this.team = team;
        this.members = members;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public List<TeamMember> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members;
    }
}
