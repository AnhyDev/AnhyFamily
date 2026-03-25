The AnhyFamily plugin allows players to hug other players, provided this feature is allowed in the settings. There are both family and personal permissions for hugging, which allow players to customize the hugging feature according to their preferences.

#### How to Hug Other Players

To hug another player, several conditions must be met:

1. **Player Position:** The player must be in a specific position, with their head tilted forward, meaning their view should be directed downwards at an angle between 0 and 70 degrees.
2. **Player State:** The player must be sneaking (crouching).
3. **Distance:** The distance to the player you want to hug must be less than 2 blocks.
4. **Right-Click:** You need to right-click on the player you want to hug.

If all these conditions are met, the hug will be successfully executed, provided hugging is allowed by the settings.

#### Family Permissions

Family permissions are managed through commands similar to those in previous sections:

1. **/fhugs access `<PlayerName>` `<allow|deny|default>`**  
   Sets personal access for a family member. You can allow, deny, or set default access.

2. **/fhugs default `<children|parents>` `<allow|deny>`**  
   Sets group access for all children or parents. Personal permissions take priority over group permissions.

3. **/fhugs check `<PlayerName>`**  
   Checks whether the specified player has access to hugs. Both personal and group access are considered.

4. **/fhugs defaultcheck `<children|parents>`**  
   Checks the hug permission for the group of children or parents.

All responses to these commands are interactive messages with clickable elements that allow you to change settings without entering commands.

#### Personal Permissions

The ability to hug is not limited to family members. There are also personal permissions for any player that each player can set or deny:

1. **/fhugs allow `<PlayerName>`**  
   Allows hugs for a specific player.

2. **/fhugs deny `<PlayerName>`**  
   Denies hugs for a specific player.

Personal permissions and denials have the highest priority, even over family personal permissions. This means you can deny hugs even to a spouse.

#### Global Permissions

1. **/fhugs allowall `<true|false>`**  
   Sets a global permission to allow hugs for all players. If set to `true`, hugs are allowed for all players, and this has a higher priority than family permissions. When set to `true`, `denyall` is automatically set to `false`.

2. **/fhugs denyall `<true|false>`**  
   Sets a global ban on hugs for all players. If set to `true`, hugs are denied for all players except those who have personal or family permissions. When set to `true`, `allowall` is automatically set to `false`.

#### Other Commands

1. **/fhugs remove `<PlayerName>`**  
   Removes a player from the personal permissions and denials list.

2. **/fhugs list**  
   Shows a list of personal permissions and denials.

3. **/fhugs globalstatus**  
   Displays the current status of the global hug permission (allowall, denyall).

#### Using the Command Without Arguments

If you execute the **/fhugs** command without arguments while looking at another player, you will receive a message indicating whether that player has allowed hugs.

#### Consequences of a Failed Hug Attempt

If a player attempts to hug another player who has not allowed it, they will receive minor damage and be knocked back a few blocks, simulating a slap.

### Example Command Usage

- **Allow hugs for a specific player:**
  ```
  /fhugs allow <PlayerName>
  ```

- **Deny hugs for a specific player:**
  ```
  /fhugs deny <PlayerName>
  ```

- **Set a global hug permission for everyone:**
  ```
  /fhugs allowall true
  ```

- **Set a global hug ban for everyone:**
  ```
  /fhugs denyall true
  ```

- **Check the global hug permission status:**
  ```
  /fhugs globalstatus
  ```

- **Check if hugs are allowed while looking at another player:**
  ```
  /fhugs
  ```