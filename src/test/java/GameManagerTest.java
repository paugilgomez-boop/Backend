import models.Admin;
import models.Inventory;
import models.Item;
import models.Player;
import models.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import repositories.GameManager;
import repositories.GameManagerImpl;

import java.util.List;
import java.util.NoSuchElementException;

public class GameManagerTest {

    private GameManager gm;

    @Before
    public void setUp() {
        gm = GameManagerImpl.getInstance();
    }

    @After
    public void tearDown() {
        gm.clear();
    }

    @Test
    public void testRegisterPlayerAndAdmin() {
        Player player = gm.registerPlayer(new Player("P1", "player1", "1234", "player@mail.com", 100, 1));
        Admin admin = gm.registerAdmin(new Admin("A1", "admin1", "admin", "admin@mail.com", 0, "ALL"));

        Assert.assertEquals("P1", player.getId());
        Assert.assertEquals("A1", admin.getId());
        Assert.assertTrue(gm.getUser("P1") instanceof Player);
        Assert.assertTrue(gm.getUser("A1") instanceof Admin);
    }

    @Test
    public void testLogin() {
        gm.registerPlayer(new Player("P1", "player1", "1234", "player@mail.com", 100, 1));

        User user = gm.login("player1", "1234");

        Assert.assertEquals("P1", user.getId());
    }

    @Test(expected = NoSuchElementException.class)
    public void testLoginWithInvalidCredentials() {
        gm.registerPlayer(new Player("P1", "player1", "1234", "player@mail.com", 100, 1));

        gm.login("player1", "bad-password");
    }

    @Test
    public void testAddUpdateDeleteItem() {
        gm.addItem(new Item("I1", "Sword", "Basic sword", "WEAPON", 25));

        Assert.assertEquals(1, gm.getAllItems().size());
        Assert.assertEquals("Sword", gm.getItem("I1").getName());

        gm.updateItem("I1", new Item("I1", "Fire Sword", "Burning sword", "WEAPON", 40));

        Assert.assertEquals("Fire Sword", gm.getItem("I1").getName());
        Assert.assertEquals(40, gm.getItem("I1").getPrice(), 0.001);

        gm.deleteItem("I1");

        Assert.assertEquals(0, gm.getAllItems().size());
    }

    @Test
    public void testBuyItemAddsInventoryAndDiscountsBalance() {
        gm.registerPlayer(new Player("P1", "player1", "1234", "player@mail.com", 100, 1));
        gm.addItem(new Item("I1", "Potion", "Small heal", "CONSUMABLE", 10));

        Inventory inventory = gm.buyItem("P1", "I1", 3);

        Assert.assertEquals("P1", inventory.getPlayerId());
        Assert.assertEquals("I1", inventory.getItemId());
        Assert.assertEquals(3, inventory.getQuantity());
        Assert.assertEquals(70, gm.getUser("P1").getSaldo(), 0.001);
    }

    @Test
    public void testBuySameItemIncreasesQuantity() {
        gm.registerPlayer(new Player("P1", "player1", "1234", "player@mail.com", 100, 1));
        gm.addItem(new Item("I1", "Potion", "Small heal", "CONSUMABLE", 10));

        gm.buyItem("P1", "I1", 2);
        gm.buyItem("P1", "I1", 3);

        List<Inventory> inventory = gm.getInventoryByPlayer("P1");

        Assert.assertEquals(1, inventory.size());
        Assert.assertEquals(5, inventory.get(0).getQuantity());
        Assert.assertEquals(50, gm.getUser("P1").getSaldo(), 0.001);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuyItemWithoutEnoughBalance() {
        gm.registerPlayer(new Player("P1", "player1", "1234", "player@mail.com", 5, 1));
        gm.addItem(new Item("I1", "Potion", "Small heal", "CONSUMABLE", 10));

        gm.buyItem("P1", "I1", 1);
    }
}
