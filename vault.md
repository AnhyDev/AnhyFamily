The AnhyFamily plugin supports payment for events such as marriage, adoption, or divorce using virtual currency. To enable this payment method, the following option must be selected in the plugin's configuration file:

```yaml
prices:
  # VIRTUAL, ITEM, CRYPTO(not yet implemented)
  currency: VIRTUAL
  # Settings for virtual currency:
  marriage: 0
  divorce: 0
  adoption: 0
```

If `currency: VIRTUAL` is set and the value for a specific service is non-zero, the payment for that service will be made in virtual currency, deducted from the player's virtual account. This functionality is supported by the Vault plugin.

#### Configuring Service Costs in the Configuration File

To modify the payment method, you need to edit the plugin's configuration file, setting the appropriate cost for each service. For example:

```yaml
prices:
  currency: VIRTUAL
  marriage: 100
  divorce: 50
  adoption: 75
```

In this example, players will pay 100 units of virtual currency for marriage, 50 units for divorce, and 75 units for adoption. If the value is set to 0, the service will be provided for free.

After making changes to the configuration file, you must reload the plugin configuration to apply the changes. This can be done using the following console command:

```
/afam reload
```

All service payments are processed automatically when the corresponding event commands are executed.

#### Important Notes

1. **Vault Plugin:** The Vault plugin must be installed and configured correctly for the payment system to work, as it provides access to players' virtual accounts.

2. **Balance Check:** Before executing a paid action, the plugin checks whether the player has sufficient funds in their virtual account. If the balance is insufficient, the action will not be performed, and the player will receive an appropriate message.

3. **Configuration Changes:** After each change to the plugin's configuration file, it is recommended to verify the settings and reload the plugin to ensure all functions work correctly.

#### Sample Configuration File

```yaml
prices:
  currency: VIRTUAL
  marriage: 100
  divorce: 50
  adoption: 75
```