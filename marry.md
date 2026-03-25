The AnhyFamily plugin provides advanced functionality that allows players to get married, forming families within the Minecraft game. By default, only male and female players can marry, but this can be changed in the plugin configuration:

```yaml
# Allow same-sex marriages as well as for non-binary and undefined genders
non_binary_marriage: false
```

#### Marriage Requirements

1. **Name and Surname:** Both partners must have a name and surname set.
2. **Family Ties:** The partners cannot be related (not ancestors or descendants of each other).
3. **Payment for the Ceremony:** If specified in the configuration, a payment will be required for the marriage ceremony. Payment methods can include virtual currency, items, or cryptocurrency (the latter is not yet implemented).

```yaml
prices:
  # Virtual money, items, cryptocurrency (not yet implemented)
  currency: VIRTUAL
  marriage: 0
  divorce: 0
  adoption: 0
```
If a payment is required for the marriage ceremony, the specified amount will be deducted from each partner during the ceremony. If either partner lacks sufficient funds, the ceremony will be canceled.

#### Types of Marriage

There are two types of marriage: public and private.

##### Public Marriage

A public ceremony is conducted with the participation of a priest and can take place anywhere.

- **Command for the priest:**  
  ```
  /marry public <PlayerName1> <PlayerName2> [0|1|2]
  ```
  The last numerical argument (optional) determines the shared surname:
  - `0` - each partner keeps their own surname.
  - `1` - the second player takes the surname of the first.
  - `2` - the first player takes the surname of the second.
  - If nothing is specified, it defaults to `1`.

- **Process:**  
  After the command is entered by the priest, both partners receive a prompt to agree or decline. If both agree, the marriage takes place; if either declines, the ceremony is canceled.

- **Chat Messages:**  
  The priest's messages and the marriage vows can be seen by either everyone on the server or only those within a certain radius of the ceremony.

```yaml
# Maximum distance from the priest to the partners. If 0, there are no limits.
ceremonyRadius: 20
# Radius within which the marriage messages will be visible in chat. If 0, all online players will see it.
ceremonyHearingRadius: 200
```

##### Private Marriage

A private ceremony involves only the partners and takes place in a specified location.

- **Location Configuration:**

```yaml
  privateCeremony:
    world: "world"
    x: 100
    y: 64
    z: -200
```
  The partners must be within 10 blocks of the specified coordinates.

- **Command for the partners:**
  ```
  /marry private <PlayerName>
  ```
  The player specified in the command receives a prompt to confirm or decline the ceremony. If they agree, the marriage takes place; if they decline, the ceremony is canceled.

- **Chat Messages:**  
  Only players nearby will learn about the ceremony from the chat.

#### After Marriage

After marriage, a family object is created, which stores information about:

- The family's home location.
- The family chest.
- The list of parents and children of both partners.
- Access to the family chat, home, chest, and other rights.

The family object exists as long as the family exists, that is, until a divorce occurs.