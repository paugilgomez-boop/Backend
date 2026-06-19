package responses;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GameUpgradePurchaseResponse {

    private boolean success;
    private String userId;
    private int damageLevel;
    private int rangeLevel;
    private int attackSpeedLevel;
    private double remainingCurrency;

    public GameUpgradePurchaseResponse() {
    }

    public GameUpgradePurchaseResponse(boolean success, String userId, int damageLevel, int rangeLevel,
                                       int attackSpeedLevel, double remainingCurrency) {
        this.success = success;
        this.userId = userId;
        this.damageLevel = damageLevel;
        this.rangeLevel = rangeLevel;
        this.attackSpeedLevel = attackSpeedLevel;
        this.remainingCurrency = remainingCurrency;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public double getRemainingCurrency() {
        return remainingCurrency;
    }

    public void setRemainingCurrency(double remainingCurrency) {
        this.remainingCurrency = remainingCurrency;
    }
}
