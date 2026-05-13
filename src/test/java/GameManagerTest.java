import models.Inventory;
import models.Item;
import models.Purchase;
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
    public void testRegisterUsersWithPermissions() {
        User player = gm.registerUser(new User(1, "player1", "1234", "player@mail.com", 100, "PLAYER", 1));
        User admin = gm.registerUser(new User(2, "admin1", "admin", "admin@mail.com", 0, "ADMIN", 0));

        Assert.assertEquals(1, player.getId());
        Assert.assertEquals(2, admin.getId());
        Assert.assertEquals("PLAYER", gm.getUser(1).getPermissions());
        Assert.assertEquals("ADMIN", gm.getUser(2).getPermissions());
    }

    @Test
    public void testLogin() {
        gm.registerUser(new User(1, "player1", "1234", "player@mail.com", 100, "PLAYER", 1));

        User user = gm.login("player1", "1234");

        Assert.assertEquals(1, user.getId());
    }

    @Test(expected = NoSuchElementException.class)
    public void testLoginWithInvalidCredentials() {
        gm.registerUser(new User(1, "player1", "1234", "player@mail.com", 100, "PLAYER", 1));

        gm.login("player1", "bad-password");
    }

    @Test
    public void testAddUpdateDeleteItem() {
        gm.addItem(new Item(1, "Sword", "Basic sword", "WEAPON", 25));

        Assert.assertEquals(1, gm.getAllItems().size());
        Assert.assertEquals("Sword", gm.getItem(1).getName());

        gm.updateItem(1, new Item(1, "Fire Sword", "Burning sword", "WEAPON", 40));

        Assert.assertEquals("Fire Sword", gm.getItem(1).getName());
        Assert.assertEquals(40, gm.getItem(1).getPrice(), 0.001);

        gm.deleteItem(1);

        Assert.assertEquals(0, gm.getAllItems().size());
    }

    @Test
    public void testBuyItemAddsInventoryAndDiscountsBalance() {
        gm.registerUser(new User(1, "player1", "1234", "player@mail.com", 100, "PLAYER", 1));
        gm.addItem(new Item(1, "Potion", "Small heal", "CONSUMABLE", 10));

        Purchase purchase = gm.buyItem(1, 1, 3);
        List<Inventory> inventory = gm.getInventoryByUser(1);

        Assert.assertEquals(1, purchase.getUserId());
        Assert.assertEquals(1, purchase.getItemId());
        Assert.assertEquals(30, purchase.getTotalPrice(), 0.001);
        Assert.assertEquals(1, inventory.size());
        Assert.assertEquals(1, inventory.get(0).getUserId());
        Assert.assertEquals(1, inventory.get(0).getItemId());
        Assert.assertEquals(3, inventory.get(0).getQuantity());
        Assert.assertEquals(70, gm.getUser(1).getSaldo(), 0.001);
    }

    @Test
    public void testBuySameItemIncreasesQuantity() {
        gm.registerUser(new User(1, "player1", "1234", "player@mail.com", 100, "PLAYER", 1));
        gm.addItem(new Item(1, "Potion", "Small heal", "CONSUMABLE", 10));

        gm.buyItem(1, 1, 2);
        gm.buyItem(1, 1, 3);

        List<Inventory> inventory = gm.getInventoryByUser(1);
        List<Purchase> purchases = gm.getPurchasesByUser(1);

        Assert.assertEquals(1, inventory.size());
        Assert.assertEquals(5, inventory.get(0).getQuantity());
        Assert.assertEquals(2, purchases.size());
        Assert.assertEquals(50, gm.getUser(1).getSaldo(), 0.001);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuyItemWithoutEnoughBalance() {
        gm.registerUser(new User(1, "player1", "1234", "player@mail.com", 5, "PLAYER", 1));
        gm.addItem(new Item(1, "Potion", "Small heal", "CONSUMABLE", 10));

        gm.buyItem(1, 1, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuyUnavailableItem() {
        gm.registerUser(new User(1, "player1", "1234", "player@mail.com", 100, "PLAYER", 1));
        gm.addItem(new Item(1, "Potion", "Small heal", "CONSUMABLE", 10, false, "potion_small"));

        gm.buyItem(1, 1, 1);
    }
}
