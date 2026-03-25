When a couple gets married and a family block is created, a family prefix of 6 characters is automatically generated. After this, if desired, the family prefix can be changed once to a custom variant.

#### Family Prefix Requirements

1. **Characters:** The prefix must consist of uppercase Latin letters.
2. **Length:** The prefix must contain between 3 and 5 characters.
3. **Uniqueness:** The prefix must be unique, meaning it should not be used by any other family.

The family prefix serves as a unique identifier for the family. It is used in the family chat and can be integrated into other plugins via placeholders and API.

#### Changing the Family Prefix

To change the family prefix, use the command:

```
/fprefix set <PREFIX>
```

This command can be executed by one of the couple. Afterward, the other family member will receive a request to either approve or reject the new prefix. They can agree or decline by clicking the corresponding option in the interactive message or by using the commands:

- **/fprefix accept**  
  Agree to set the new prefix.

- **/fprefix refuse**  
  Decline to set the new prefix.

#### Example of Command Usage

1. One family member executes the command:
   ```
   /fprefix set ABC
   ```
2. The other family member receives an interactive message with a request for approval.
3. To approve or decline, they can use the commands:
   ```
   /fprefix accept
   ```
   or
   ```
   /fprefix refuse
   ```
