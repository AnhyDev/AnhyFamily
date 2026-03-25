The AnhyFamily plugin allows using items as a payment method for events such as marriage, adoption, or divorce. This functionality becomes available if the following option is selected in the plugin's configuration file:

```yaml
prices:
  # VIRTUAL, ITEM, CRYPTO(not yet implemented)
  currency: ITEM
```

When this option is set, the plugin will look for information about the cost of services in items. The item prices are stored in the `item_prices.yml` file within the plugin folder. This file is loaded into memory when the server starts. If the file is missing, the item cost defaults to zero, meaning the services are provided for free. Similarly, if there is no entry for a specific service in the file, that service is also considered free. Only those services with a corresponding entry in the file will have the specified item cost.

#### Commands for Managing Item Prices

A player with administrative permissions can add, modify, or remove items as service prices using the following commands:

- **/anhyfam item add `<key>`**  
  Adds or changes the item used for service payments, where `<key>` corresponds to a specific event (e.g., `marriage`, `divorce`, `adoption`). The player must hold the item in hand when executing the command.

- **/anhyfam item clear**  
  Clears the `item_prices.yml` file, removing all entries for service item prices.

- **/anhyfam item get `<key>`**  
  Retrieves the item corresponding to the specified key into the player's inventory or drops it if the inventory is full.

- **/anhyfam item remove `<key>`**  
  Removes the item corresponding to the specified key from the `item_prices.yml` file.

#### Examples of Command Usage

1. **Adding or Modifying an Item**

   An administrator holds the item in hand and executes the command:
   ```
   /anhyfam item add marriage
   ```
   After successfully executing the command, the item will be added or updated for the `marriage` key.

2. **Clearing the Item File**

   To clear all entries for service item prices, execute the command:
   ```
   /anhyfam item clear
   ```
   Successfully executing the command will delete the `item_prices.yml` file.

3. **Retrieving an Item by Key**

   To retrieve the item corresponding to a specific key, execute the command:
   ```
   /anhyfam item get divorce
   ```
   If an item exists for the `divorce` key, it will be added to your inventory or dropped nearby if the inventory is full.

4. **Removing an Item by Key**

   To remove the item corresponding to a specific key, execute the command:
   ```
   /anhyfam item remove adoption
   ```
   After successfully executing the command, the item will be removed from the `item_prices.yml` file.