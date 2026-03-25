The family chest is a virtual chest with 54 slots where the family and its members can store items and have shared access. Family members include the parents and children of the couple. Only one player can use the chest at a time; another player will gain access only after the first player closes the chest.

#### Commands for Setting and Permissions

1. **/fchest set**  
   Sets the chest at the coordinates of the block the player is looking at. Only certain blocks specified in the configuration can be used as the family chest. The other spouse will receive an interactive message to confirm or refuse the chest setup.

2. **/fchest accept**  
   Confirm the chest setup.

3. **/fchest refuse**  
   Refuse the chest setup.

It's important to note that the family chest block cannot be destroyed. To break the chest block, you must first move the chest to another block at different coordinates by using the `/fchest set` command at the new location.

#### Personal and Group Permissions

Commands for managing access to the family chest are similar to those for the family chat and home:

1. **/fchest access `<PlayerName>` `<allow|deny|default>`**  
   Sets personal access for a family member. You can allow, deny, or set default access. It is not possible to grant access to a player who is not a family member (i.e., not a parent or child of either spouse).

2. **/fchest default `<children|parents>` `<allow|deny>`**  
   Sets group access for all children or parents. Personal permissions take priority over group permissions. For example, if children are denied access to the chest, but one child is given personal permission, that child will have access.

3. **/fchest check `<PlayerName>`**  
   Checks whether the specified player has access to the family chest. Both personal and group access are considered.

4. **/fchest defaultcheck `<children|parents>`**  
   Checks the access permission for the group of children or parents to the family chest.

All responses to these commands are interactive messages with clickable elements that allow you to change settings without entering commands.

#### Using the Family Chest

1. **/fchest**  
   Opens the family chest. This command can be executed by the couple and family members who have permission.

2. **/fchest #`<PREFIX>`**  
   Opens the family chest by prefix. `<PREFIX>` is the unique prefix of the family whose chest is being accessed.

3. **/fchest @`<PlayerName>`**  
   Opens the family chest by player nickname. `<PlayerName>` is the player whose family chest is being accessed.

4. **Clicking on the Block**  
   The family chest can also be opened by clicking on it if this is not disabled in the configuration.

#### Configuration Settings

In the plugin configuration, you can set up how the chest is opened, the interaction distance, and world limitations:

```yaml
# Using the family chest
chest:
  # Allow the command (/fchest) to open the chest
  command: true
  # Distance for the command (0 - no limit)
  distance: 0
  # If unlimited, then only in this world
  world: false
  # Allow opening the chest by clicking
  click: true
  # List of blocks in "Material" format that can be used as a chest
  material:
    - CHEST
    - BARREL
  # Distance from the chest to the home point for setting and interaction
  distance_to_home: 20
```

This configuration allows you to customize how the family chest works, including the blocks that can be used, the distance restrictions, and how players can interact with the chest.