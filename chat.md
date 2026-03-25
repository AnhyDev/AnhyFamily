The family chat in the AnhyFamily plugin allows the couple and family members with appropriate permissions to communicate with each other. The commands for managing access and using the family chat are only available to the couple.

#### Commands for Managing Access

1. **/fchat access `<PlayerName>` `<allow|deny|default>`**  
   Sets personal access for a family member. You can allow, deny, or set default access. It is not possible to grant access to a player who is not a family member (i.e., not a parent or child of either spouse).

2. **/fchat default `<children|parents>` `<allow|deny>`**  
   Sets group access for all children or parents. Personal permissions take priority over group permissions. For example, if children are denied access to the chat, but one child is given personal permission, that child will have access.

3. **/fchat check `<PlayerName>`**  
   Checks whether the specified player has access to the family chat. Both personal and group access are considered.

4. **/fchat defaultcheck `<children|parents>`**  
   Checks the access permission for the group of children or parents to the family chat.

All responses to these commands are interactive messages with clickable elements that allow you to change settings without entering commands.

#### Using the Family Chat

1. **/fchat `<message text>`**  
   Sends a message to your own family chat. The message is visible to all family members who have access to the chat.

2. **/fchat #`<PREFIX>` `<message text>`**  
   Sends a message to a family chat by prefix. `<PREFIX>` is the unique prefix of the family whose chat the message is being sent to. A player can only send messages to family chats they have access to.

3. **/fchat @`<PlayerName>` `<message text>`**  
   Sends a message to a family chat by player nickname. `<PlayerName>` is the player whose family chat the message is being sent to. This is an alternative to using a prefix.

4. **/fchat `<message text>` @`<PlayerName>`**  
   Tags a player in the family chat. The player `<PlayerName>` will receive an audio and visual notification that they have been tagged.

#### Interactive Elements

All elements of messages in the family chat, except the message text itself, are interactive. This allows certain commands and other actions to be inserted into the command line with simple clicks on these elements.

#### Default Access

The couple has access to their own family chat by default, and this access cannot be revoked. This ensures a constant communication channel between the couple, regardless of other access settings for family members.