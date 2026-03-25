The **/family info** command provides detailed information about a player and their family. This command displays an interactive message where you can choose to view either the player's profile or their family tree. By hovering over the respective option, you'll see the desired information in a tooltip, and by clicking on it, the information will be printed in the chat as a message for the player.

#### Using the Command

- **/family info**  
  If the command is executed without additional parameters, information about the player who executed the command will be displayed.

```
/family info <PlayerName>
```
  Add a player's nickname to get information about the specified player.

### Player Profile

The **/family profile** command (or selecting the corresponding option from the **/family info** command) displays detailed information about the player, including their gender, name, surname, and information about close relatives: spouse, parents, and children.

Example of an interactive profile:

```
=========================================
 (♂) Korvin Ambersky (Korvinius)
-----------------------------------------
 Partner: None 
 Father: Unknown 
 Mother: (♀) Dara Chaos (Dara)
 Children: 
  Son: (♂) Nacio Armarius (NaZZyyOO)
  Daughter: (♀) Lyra Ambersky (Lyra)
=========================================
```

Clicking on any player listed in the profile will bring up the relevant information about that player.

### Family Tree

The **/family tree** command (or selecting the corresponding option from the **/family info** command) displays the player's family tree, including ancestors and descendants. The family tree is also interactive: clicking on anyone listed in the family tree will display that player's family tree.

Example of a family tree:

```
   ┌─ (♀) Lyra Ambersky (Lyra)
     ┌─ (?) (Redgit)
   ┌─ (♂) Nacio Armarius (NaZZyyOO)
 ┌─ Descendants 
  Family Tree (♂) Korvin Ambersky (Korvinius)
 └─ Ancestors 
   └─ (♀) Dara Chaos (Dara)
```

### Command Usage

- **/family profile**  
  Displays the player's profile information. You can add a nickname to get information about another player:

```
  /family profile <PlayerName>
```

- **/family tree**  
  Displays the player's family tree. You can add a nickname to get information about another player's family tree:

```
  /family tree <PlayerName>
```