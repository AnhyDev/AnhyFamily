The configuration file for the AnhyFamily plugin allows you to customize various aspects of the plugin, from database settings to family features. Below is a detailed description of each configuration parameter.

#### Basic Settings

- **language**: The default language that will be used if a translation is not found in the desired language.
```yaml
  language: en
```

- **plugin_name**: The name of the plugin that will be displayed to users in the chat.
```yaml
  plugin_name: AnhyFamily
```

#### Database Settings

- **database**: Settings for connecting to the database. Possible database types: 'SQLite' or 'MySQL'.
```yaml
  database:
    type: 'SQLite'
    mysql:
      host: 'db4free.net'
      port: 3306
      database: 'anhyfamily'
      username: 'anhydev'
      password: 'HE5rYZb2hygDf4FW'
      prefix: 'anhy_'
      useSSL: false
      autoReconnect: true
```

#### Pricing Settings

- **prices**: Settings for currency and pricing for various actions in the plugin.
```yaml
  prices:
    currency: VIRTUAL
    marriage: 0
    divorce: 0
    adoption: 0
```

#### Gender Settings

- **gender**: Settings that allow the selection of a non-binary gender and the corresponding rights for adoption and marriage.
```yaml
  gender:
    non_binary: false
    non_binary_adoption: false
    non_binary_marriage: false
```

#### Ceremony Settings

- **ceremonyRadius**: The maximum distance from the priest to the couple. If 0, there are no limits.
```yaml
  ceremonyRadius: 20
```

- **ceremonyHearingRadius**: The radius within which the marriage announcements will be visible in the chat. If 0, all online players will see it.
```yaml
  ceremonyHearingRadius: 200
```

- **marriedSymbol**: The symbol used to denote married players.
```yaml
  marriedSymbol: "⚭"
```

#### Private Ceremony Settings

- **privateCeremony**: The location for holding a private marriage ceremony.
```yaml
  privateCeremony:
    world: "world"
    x: 100
    y: 64
    z: -200
```

#### Family Home Settings

- **home**: Settings for the family home.
```yaml
  home:
    timeout: 1440
    world: false
```

#### Family Chest Settings

- **chest**: Settings for the family chest.
```yaml
  chest:
    command: true
    distance: 0
    world: false
    click: true
    material:
      - CHEST
      - BARREL
    distance_to_home: 20
```

#### Name and Surname Restrictions

- **languages_limitation**: Settings that restrict names and surnames using regular expressions.
```yaml
  languages_limitation: "^(?!.*[-']{2})(?!.*--)(?!.*'')\\p{L}['-]*[\\p{L}]+$"
```

### Configuration File Example

```yaml
#
# ░█████╗░███╗░░██╗██╗░░██╗██╗░░░██╗███████╗░█████╗░███╗░░░███╗██╗██╗░░██╗░░░██╗
# ██╔══██╗████╗░██║██║░░██║╚██╗░██╔╝██╔════╝██╔══██╗████╗░████║██║██║░░╚██╗░██╔╝
# ███████║██╔██╗██║███████║░╚████╔╝░█████╗░░███████║██╔████╔██║██║██║░░░╚████╔╝░
# ██╔══██║██║╚████║██╔══██║░░╚██╔╝░░██╔══╝░░██╔══██║██║╚██╔╝██║██║██║░░░░╚██╔╝░░
# ██║░░██║██║░╚███║██║░░██║░░░██║░░░██║░░░░░██║░░██║██║░╚═╝░██║██║███████╗██║░░░
# ╚═╝░░╚═╝╚═╝░░╚══╝╚═╝░░╚═╝░░░╚═╝░░░╚═╝░░░░░╚═╝░░╚═╝╚═╝░░░░░╚═╝╚═╝╚══════╝╚═╝░░░
#
language: en
plugin_name: AnhyFamily

database:
  type: 'SQLite'
  mysql:
    host: 'db4free.net'
    port: 3306
    database: 'anhyfamily'
    username: 'anhydev'
    password: 'HE5rYZb2hygDf4FW'
    prefix: 'anhy_'
    useSSL: false
    autoReconnect: true

prices:
  currency: VIRTUAL
  marriage: 0
  divorce: 0
  adoption: 0

gender:
  non_binary: false
  non_binary_adoption: false
  non_binary_marriage: false

ceremonyRadius: 20
ceremonyHearingRadius: 200
marriedSymbol: "⚭"

privateCeremony:
  world: "world"
  x: 100
  y: 64
  z: -200

home:
  timeout: 1440
  world: false

chest:
  command: true
  distance: 0
  world: false
  click: true
  material:
    - CHEST
    - BARREL
  distance_to_home: 20

languages_limitation: "^(?!.*[-']{2})(?!.*--)(?!.*'')\\p{L}['-]*[\\p{L}]+$"
```

This configuration file allows you to set up the AnhyFamily plugin according to your needs, providing flexible control over the database, pricing, gender parameters, ceremonies, family locations, and restrictions on names and surnames.