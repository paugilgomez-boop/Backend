package orm.dao;

import models.TeamMember;

import java.util.List;

public interface TeamMemberDAO {

    TeamMember addTeamMember(TeamMember member);

    List<TeamMember> getMembersByTeam(String team);

    TeamMember getMemberByName(String name);

    List<TeamMember> getAllMembers();

    boolean isEmpty();

    void clear();
}
