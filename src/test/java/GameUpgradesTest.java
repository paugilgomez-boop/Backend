import org.junit.Assert;
import org.junit.Test;
import repositories.GameManager;
import repositories.GameManagerImpl;
import responses.GameUpgradePurchaseResponse;
import responses.GameUpgradesResponse;

import java.util.NoSuchElementException;

public class GameUpgradesTest {

    private final GameManager gm = GameManagerImpl.getInstance();

    @Test
    public void getUpgradesForUnknownUserReturnsZeros() {
        GameUpgradesResponse response = gm.getUpgradesByUsername("usuario_inexistente_xyz");

        Assert.assertEquals("usuario_inexistente_xyz", response.getUserId());
        Assert.assertEquals(0, response.getDamageLevel());
        Assert.assertEquals(0, response.getRangeLevel());
        Assert.assertEquals(0, response.getAttackSpeedLevel());
    }

    @Test
    public void getUpgradesForExistingUserWithoutPurchasesReturnsZeros() {
        GameUpgradesResponse response = gm.getUpgradesByUsername("user1");

        Assert.assertEquals("user1", response.getUserId());
        Assert.assertEquals(0, response.getDamageLevel());
        Assert.assertEquals(0, response.getRangeLevel());
        Assert.assertEquals(0, response.getAttackSpeedLevel());
    }

    @Test
    public void purchaseUpgradeIncrementsLevelAndDiscountsBalance() {
        String username = "upgrade_test_" + System.currentTimeMillis();
        gm.registerUser(new models.User(0, username, "pass", username + "@test.com", 500, "PLAYER", 1));

        GameUpgradePurchaseResponse first = gm.purchaseUpgrade(username, "damage");
        Assert.assertTrue(first.isSuccess());
        Assert.assertEquals(1, first.getDamageLevel());
        Assert.assertEquals(400, first.getRemainingCurrency(), 0.001);

        GameUpgradesResponse levels = gm.getUpgradesByUsername(username);
        Assert.assertEquals(1, levels.getDamageLevel());
        Assert.assertEquals(0, levels.getRangeLevel());
        Assert.assertEquals(0, levels.getAttackSpeedLevel());
    }

    @Test
    public void buyItemFromShopUpdatesUnityUpgradeLevels() {
        String username = "upgrade_shop_" + System.currentTimeMillis();
        models.User user = gm.registerUser(
                new models.User(0, username, "pass", username + "@test.com", 500, "PLAYER", 1));

        gm.buyItem(user.getId(), GameManagerImpl.DAMAGE_UPGRADE_ITEM_ID, 2);

        GameUpgradesResponse levels = gm.getUpgradesByUsername(username);
        Assert.assertEquals(2, levels.getDamageLevel());
        Assert.assertEquals(0, levels.getRangeLevel());
        Assert.assertEquals(0, levels.getAttackSpeedLevel());
    }

    @Test(expected = NoSuchElementException.class)
    public void purchaseUpgradeForUnknownUserFails() {
        gm.purchaseUpgrade("no_existe_" + System.currentTimeMillis(), "damage");
    }
}
