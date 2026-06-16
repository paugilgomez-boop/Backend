package orm.dao;

import models.TeamMember;

import java.util.List;

public interface TeamMemberDAO {

    TeamMember addTeamMember(TeamMember member);

    List<TeamMember> getMembersByTeam(String team);

    boolean isEmpty();

    void clear();
}
