package orm.dao;

import models.Item;

import java.util.List;

public interface ItemDAO {
    Item addItem(Item item);

    Item getItem(int id);

    Item updateItem(int id, Item item);

    void deleteItem(Item item);

    List<Item> getItems();

    boolean isEmpty();

    void clear();
}
