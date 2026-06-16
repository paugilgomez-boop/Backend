package orm.dao;

import models.TeamMember;
import orm.FactorySession;
import orm.Session;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class TeamMemberDAOImpl implements TeamMemberDAO {

    final static Logger logger = Logger.getLogger(TeamMemberDAOImpl.class);

    @Override
    public TeamMember addTeamMember(TeamMember member) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.save(member);
            logger.info("Team member " + member.getName() + " guardado correctamente");
            return member;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<TeamMember> getMembersByTeam(String team) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            HashMap<String, Object> params = new HashMap<>();
            params.put("team", team);

            @SuppressWarnings("unchecked")
            List<TeamMember> members = (List<TeamMember>) (List<?>) session.findAll(TeamMember.class, params);
            return members;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return getMembersByTeam("porxinos").isEmpty();
    }

    @Override
    public void clear() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.getConnection().prepareStatement("DELETE FROM TeamMember").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error borrando miembros de equipos", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
