package responses;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GameUpgradesResponse {

    private String userId;
    private int damageLevel;
    private int rangeLevel;
    private int attackSpeedLevel;

    public GameUpgradesResponse() {
    }

    public GameUpgradesResponse(String userId, int damageLevel, int rangeLevel, int attackSpeedLevel) {
        this.userId = userId;
        this.damageLevel = damageLevel;
        this.rangeLevel = rangeLevel;
        this.attackSpeedLevel = attackSpeedLevel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getDamageLevel() {
        return damageLevel;
    }

    public void setDamageLevel(int damageLevel) {
        this.damageLevel = damageLevel;
    }

    public int getRangeLevel() {
        return rangeLevel;
    }

    public void setRangeLevel(int rangeLevel) {
        this.rangeLevel = rangeLevel;
    }

    public int getAttackSpeedLevel() {
        return attackSpeedLevel;
    }

    public void setAttackSpeedLevel(int attackSpeedLevel) {
        this.attackSpeedLevel = attackSpeedLevel;
    }
}
