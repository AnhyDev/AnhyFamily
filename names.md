The AnhyFamily plugin allows players to set their own name and surname, which will be used when displaying information about the player, their family, and the family tree. This data can be accessed via placeholders or the API for use in various contexts.

#### Setting the Name and Surname

- **/family firstname `<Firstname>`**  
  Sets the player's first name. The maximum length for a name is 12 characters. For example:
  ```
  /family firstname John
  ```

- **/family surname `<Surname male version[/Surname female version]>`**  
  Sets the player's surname. If the male and female versions of the surname differ, they are separated by a slash. For example:
  ```
  /family surname Smith/Smithson
  ```
  If the surname is the same for both genders, only one version is provided:
  ```
  /family surname Johnson
  ```
  The maximum length for a surname is 15 characters.

  Once a player has set their name or surname, they cannot change it themselves. For this, players with special permissions, known as priests, are needed.

#### Administrative Commands

Commands for forcibly setting a name and surname, available only from the console:

- **/anhyfam forcefirstname `<PlayerName>` `<Firstname>`**  
  Forces the player's first name to be set.

- **/anhyfam forcesurname `<PlayerName>` `<Surname male version[/Surname female version]>`**  
  Forces the player's surname to be set.

#### Commands for Priests

Players with the "family.pastor" permission can suggest name or surname changes to other players:

- **/family suggest firstname `<PlayerName>` `<Firstname>`**  
  Suggests a name change.

- **/family suggest surname `<PlayerName>` `<Surname male version[/Surname female version]>`**  
  Suggests a surname change.

The player can accept the suggestion with the command:
- **/family suggest accept**

Or decline the suggestion with the command:
- **/family suggest refuse**

#### Surname Restrictions

The plugin's configuration allows setting restrictions on the characters that can be used in surnames through regular expressions. Examples:

```yaml
# Only Latin letters, apostrophes, and hyphens are allowed
# languages_limitation: "^(?!.*[-']{2})(?!.*--)(?!.*'')[a-zA-Z'-]+$"
# Only Ukrainian letters, apostrophes, and hyphens are allowed
# languages_limitation: "^(?!.*[-']{2})(?!.*--)(?!.*'')[а-щА-ЩЬьЮюЯяЇїІіЄєҐґ'-]+$"
# Only Russian letters, apostrophes, and hyphens are allowed
# languages_limitation: "^(?!.*[-']{2})(?!.*--)(?!.*'')[а-яА-ЯёЁ'-]+$"
# Letters from any language, apostrophes, and hyphens are allowed
languages_limitation: "^(?!.*[-']{2})(?!.*--)(?!.*'')\\p{L}['-]*[\\p{L}]+$"
```
