The AnhyFamily plugin provides a wide range of placeholders for use in other plugins or scripts through the PlaceholderAPI plugin. This allows for the incorporation of player and family information into various game contexts, offering greater flexibility and customization options.

#### Using Placeholders with PlaceholderAPI

To use AnhyFamily placeholders, you need to install the PlaceholderAPI plugin and add the extension `Expansion-anhy.jar`.

#### Gender-related Placeholders

These placeholders are related to the player's gender:

- **%anhy_gender%**  
  Returns the player's gender.

- **%anhy_gender_key%**  
  Returns the language file key for the player's gender.

- **%anhy_gender_lang%**  
  Returns the name of the player's gender in their language.

- **%anhy_gender_hexcolor%**  
  Returns the HEX color code associated with the player's gender.

#### Family-related Placeholders

These placeholders are related to the player's family:

- **%anhy_family_firstname%**  
  Returns the player's first name.

- **%anhy_family_lastname%**  
  Returns the player's surname according to their gender.

- **%anhy_family%**  
  Returns a serialized JSON string of the player's family object, which can be processed for user-friendly usage.

- **%anhy_family_mother%**  
  Returns the UUID string of the player's mother.

- **%anhy_family_father%**  
  Returns the UUID string of the player's father.

- **%anhy_family_spouse%**  
  Returns the UUID string of the player's spouse.

- **%anhy_family_children%**  
  Returns a comma-separated list of UUID strings of the player's children.

- **%anhy_family_info%**  
  Returns detailed information about the player's family in the form of language keys.

- **%anhy_family_info_translated%**  
  Returns detailed information about the player's family, translated into the player's language.

- **%anhy_family_tree%**  
  Returns a textual representation of the player's family tree in the form of language keys.

- **%anhy_family_tree_translated%**  
  Returns a textual representation of the player's family tree, translated into the player's language.