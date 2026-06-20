import org.junit.Assert;
import org.junit.Test;
import repositories.GameManager;
import repositories.GameManagerImpl;
import responses.EarnCoinsResponse;

public class GameCoinsTest {

    private final GameManager gm = GameManagerImpl.getInstance();

    @Test
    public void earnCoinsIncreasesExistingUserBalance() {
        String username = "coins_test_" + System.currentTimeMillis();
        models.User user = gm.registerUser(
                new models.User(0, username, "pass", username + "@test.com", 265, "PLAYER", 1));

        EarnCoinsResponse response = gm.earnCoins(username, 265);

        Assert.assertEquals(username, response.getUserId());
        Assert.assertEquals(265, response.getCoinsEarned());
        Assert.assertEquals(530, response.getTotalCoins());
        Assert.assertEquals(530, gm.getUser(user.getId()).getSaldo(), 0.001);
    }

    @Test
    public void earnCoinsCreatesNewUserWithBalance() {
        String username = "coins_new_" + System.currentTimeMillis();

        EarnCoinsResponse response = gm.earnCoins(username, 50);

        Assert.assertEquals(50, response.getTotalCoins());
        models.User user = gm.getAllUsers().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(user);
        Assert.assertEquals(50, user.getSaldo(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void earnCoinsWithZeroAmountFails() {
        gm.earnCoins("user1", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void earnCoinsWithEmptyUserIdFails() {
        gm.earnCoins("  ", 100);
    }
}
