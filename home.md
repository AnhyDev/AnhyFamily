The family home in the AnhyFamily plugin is a location that the married couple and family members with the appropriate permissions can teleport to. Family members include the parents and children of the couple.

#### Commands for Setting and Permissions

1. **/fhome set**  
   Sets the home point at the coordinates of the player who executes the command. The other spouse will receive an interactive message to either confirm or decline the home point setting.

   - **/fhome accept**  
     Confirm the setting of the home point.

   - **/fhome refuse**  
     Refuse the setting of the home point.

The home point can only be changed after a cooldown period specified in the configuration.

#### Personal and Group Permissions

Commands for managing access to the family home are similar to those for the family chat:

1. **/fhome access `<PlayerName>` `<allow|deny|default>`**  
   Sets personal access for a family member. You can allow, deny, or set default access. It is not possible to grant access to a player who is not a family member (i.e., not a parent or child of either spouse).

2. **/fhome default `<children|parents>` `<allow|deny>`**  
   Sets group access for all children or parents. Personal permissions take priority over group permissions. For example, if children are denied access to the home, but one child is given personal permission, that child will have access.

3. **/fhome check `<PlayerName>`**  
   Checks whether the specified player has access to the family home. Both personal and group access are considered.

4. **/fhome defaultcheck `<children|parents>`**  
   Checks the access permission for the group of children or parents to the family home.

All responses to these commands are interactive messages with clickable elements that allow you to change settings without entering commands.

#### Teleporting to the Family Home

1. **/fhome**  
   Teleports to the family home. This command can be executed by the couple and family members who have permission.

2. **/fhome #`<PREFIX>`**  
   Teleports to the family home by prefix. `<PREFIX>` is the unique prefix of the family whose home is being teleported to.

3. **/fhome @`<PlayerName>`**  
   Teleports to the family home by player nickname. `<PlayerName>` is the player whose family home is being teleported to.

#### Configuration Settings

In the plugin configuration, you can specify whether teleportation to the family home is only allowed from the world where it is set or from any world, as well as set the timeout before the home point can be changed:

```yaml
# Use of the /fhome (/familyhome) command
home:
  # Timeout before changing the family home point, specified in minutes
  timeout: 1440
  # Allow teleporting to the family home only in the world where it is set
  world: false
```