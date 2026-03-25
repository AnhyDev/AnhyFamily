The AnhyFamily plugin provides the ability to sever family relationships between players. This process can include divorce, breaking ties with individual family members, or cutting off all ties with children or parents. No consent from other players is required.

#### Divorce

- **/family divorce**  
  This command divorces the spouses, destroying the family object and removing all cross-family ties with other family objects of the spouses. If a player took the other spouse's surname during marriage, it will be reverted to their previous surname.

  If the plugin configuration specifies a payment for divorce, it will be deducted from the player's account when the command is executed. If the player does not have enough funds, the divorce will not proceed.

#### Severing Family Relationships

- **/family separate `<PlayerName>`**  
  Identifies who `<PlayerName>` is (spouse, child, or parent). This command fully severs the relationship with the specified player, including mutual removal from each other's family objects if they exist for both players.

- **/family separate children**  
  Completely severs relationships with all children, including mutual removal from family objects if they exist for all involved players.

- **/family separate parents**  
  Completely severs relationships with all parents, including mutual removal from family objects if they exist for all involved players.

#### Forced Family Relationship Clearance

- **/anhyfam clearfamily player**  
  This command is available from the console or to a player with administrative rights. It fully severs and clears all family ties of the player **player**.

#### Payment for Actions

Certain actions may incur a fee if specified in the configuration:

```yaml
prices:
  # Virtual money, items, cryptocurrency (not yet implemented)
  currency: VIRTUAL
  marriage: 0
  divorce: 0
  adoption: 0
```
