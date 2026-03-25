In the AnhyFamily plugin, players can adopt other players. By default, only a male and female couple can adopt, but this can be changed in the configuration:

```yaml
# Allow adoption for non-binary and undefined genders
non_binary_adoption: false
```

#### Adoption Requirements

1. **Adopters:** Adoption can be initiated by a pair of players who do not necessarily have to be a married couple. These can be players with their own families or those who are not married at all. There are no restrictions on the marital status of the adopters.
2. **Family Ties:** Adoption is not possible if there are already family ties (such as ancestors or descendants) between any of the adopters and the child.
3. **Adoption Fee:** If specified in the configuration, a fee may be charged for adoption.

```yaml
prices:
  # Virtual money, items, cryptocurrency (not yet implemented)
  currency: VIRTUAL
  marriage: 0
  divorce: 0
  adoption: 0
```
The adoption fee is deducted from the player when they propose adoption using the command `/adoption invite <PlayerName>`. If there are insufficient funds, the adoption proposal will not be sent. If the adoption does not proceed, such as if the prospective adoptee declines, the fee is not refunded.

#### Adoption Process

1. **Adoption Invitation:** Both future parents must execute the command:
   ```
   /adoption invite `<PlayerName>`
   ```
   where `<PlayerName>` is the player being adopted. The adoptee receives a message with a prompt to accept or decline.

2. **Acceptance or Decline:** The prospective adoptee can accept or decline the adoption using the commands:
   ```
   /adoption accept
   ```
   or
   ```
   /adoption decline
   ```

3. **Canceling the Proposal:** If one of the adopters wants to withdraw their proposal, they can use the command:
   ```
   /adoption cancel
   ```

4. **Finalizing the Adoption:** The adoption will only be completed after both parents send the proposal and the adoptee accepts it.

#### Forced Adoption

Forced adoption is available through the console or to players with administrative rights. In this case, adoption is done by a single player rather than a pair.

- **Command:**
  ```
  /anhyfam forceadopt `<adoptedPlayer>` `<adopterPlayer>`
  ```
  where `adoptedPlayer` is the player being adopted, and `adopterPlayer` is the adopter. Forced adoption is only possible if the child does not already have parents or does not have a father or mother.

#### After Adoption

- If the parents or the child have their own families, they will be added to each other's family objects.
- Information about the new family will be saved in the corresponding family objects, including access to the family chat, home, and chest.