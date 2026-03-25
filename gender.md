The AnhyFamily plugin allows players to set and manage their gender, which enhances integration into family relationships and interactions within the game. Below are the details on choosing and using gender in the plugin.

#### Commands for Setting Gender
- **/gender** (aliases: /identity, /gen)  
  The primary command for managing the player's gender.

##### Subcommands:
- **/gender info**  
  Displays the current gender of the player who executed the command. If a player’s nickname is added, it shows the gender of the specified player.

- **/gender set [male|female|non_binary]**  
  Sets the corresponding gender for the player. Male and female genders are available by default, while the non-binary gender is only available if allowed in the plugin configuration.

#### Impact of Gender
A player's gender helps with identification and adds depth to the gameplay. Using placeholders that provide both the gender symbol and its name, you can customize chat prefixes.

Additionally, by default, marriages are only allowed between a male and a female, and similarly, adoption is typically only allowed for male-female couples. These restrictions can be lifted by modifying the plugin configuration to allow marriages and adoptions between players of any gender, including same-sex, non-binary, and undefined.

#### Configuration Example
```yaml
gender:
  # Allow the selection of a non-binary gender
  non_binary: false
  # Allow adoption for non-binary and undefined genders
  non_binary_adoption: false
  # Allow marriages for non-binary and undefined genders
  non_binary_marriage: false
```

#### Administrative Commands
- **/anhyfam forcegender `<PlayerName>` `<gender>`**  
  Forces a gender change for the player. This command is only available from the console.

- **/anhyfam genderreset `<PlayerName>`**  
  Resets the player's gender to "undefined." This command is only available from the console.

#### Integration with Other Plugins
Player gender can be used in other plugins via API or placeholders, expanding its application possibilities. This allows for an even more interactive and dynamic gaming experience.