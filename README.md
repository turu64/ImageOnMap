ImageOnMap
==========

## This repository was hard-forked by OKOCRAFT

Repo for ImageOnMap, a paper plugin.

This repository was hard-forked by OKOCRAFT to fix bugs and support the latest version.

A lot of modifications have been made to the internal code. Make sure to back up your server before use. 

If you are find any bugs, please feel free to report bugs to [issues](https://github.com/okocraft/ImageOnMap/issues).

### Limitations

- Only works on [Paper server](https://github.com/PaperMC/Paper) or its forked server.
- **Minecraft 1.21.4+** required (as of version 5.1.0).
- Java 17 or later required.
- Migration feature from older than version v3 has been removed.

The plugin Jar is available on [releases](https://github.com/okocraft/ImageOnMap/releases).

---

### Features

ImageOnMap allows you to load a picture from the Internet to a Minecraft map.

- Loads an image from a URL onto a map. PNG, JPEG and static GIF are supported.
- These images will be saved on your server and reloaded at restart.
- Big pictures will be cut automatically into several parts, to be rendered over multiple maps so they can cover whole
  walls! As example a 1024x1024 picture will be cut in 16 maps.
- Your image will be centered.
- You can put your map in an item frame, or in multiple ones at once—ImageOnMap handles the placement for you!
- **Economy integration with Vault** (optional) — charge players based on map size with confirmation dialog, refunds on deletion, and bypass permissions for staff.

This plugin is a free software licenced under the [CeCILL-B licence](https://cecill.info/licences/Licence_CeCILL-B_V1-en.html)
(BSD-style in French law).


## Quick guide

- Ensure that you have a free slot in your inventory, as ImageOnMap will give you a map.
- Type `/tomap URL`, where URL is a link to the picture you want to render (see the section below).
- Enjoy your picture! You can place it in an item frame to make a nice poster if you want.


## Commands and Permissions

### `/tomap <url>`

Renders an image and gives a map to the player with it.

- This command can only be used by a player.
- The link must be complete, do not forget that the chat limit is 240 characters.
- You can use an URL shortener like tinyURL or bitly.
- If you want a picture in one map, type resize after the link.
- Permission: `imageonmap.new` (or `imageonmap.userender`—legacy, but will be kept in the plugin).


### `/maps [PlayerName]`

Opens a GUI to see, retrieve and manage the user's maps, can be used to see other user's maps.

- This command can only be used by a player.
- Opens a GUI listing all the maps in a paginated view.
- A book is displayed too to see some usage statistics (maps created, quotas).
- An user can retrieve a map by left-clicking it, or manage it by right-clicking.
- Maps can be renamed (for organization), deleted (but they won't render in game anymore!), or partially retrieved (for posters maps containing more than one map).
- Permissions: `imageonmap.explore`,`imageonmap.list`, plus `imageonmap.get`, `imageonmap.rename` and `imageonmap.delete` for actions into the GUI, for moderation usage`imageonmap.exploreother`, `imageonmap.listother`,`imageonmap.getother`,`imageonmap.deleteother`.

### `/givemap PlayerName [PlayerFrom]:<MapName>`

Give to a player a map from a player map store (if not specified will take from the player map store).
- Can be used by a command block and by server console (but you need to specify PlayerFrom)
Examples:
    - `/givemap Vlammar "A very cool map name"` Will give the map named "A very cool map name" to the player Vlammar
    - `/givemap Vlammar AmauryPi:"A very cool map name"` Same as above but will use AmauryPi's map store instead of the one of the player that runs the command
- Permission: `imageonmap.give`

### `/maptool update [PlayerName]:<MapName> <new url> [stretched|covered] `

Update a specified map (the field PlayerName is optional, by default it will look in the command sender store) 
- Can be used by a command block and by server console, if so the field Playername becomes mandatory
Examples:
    - `/maptool update "A very cool map name" https://www.numerama.com/wp-content/uploads/2020/09/never-gonna-give-you-up-clip-1024x581.jpg ` Will update the map named "A very cool map name"
    - `/maptool update AmauryPi:"A very cool map name" https://www.numerama.com/wp-content/uploads/2020/09/never-gonna-give-you-up-clip-1024x581.jpg covered` Will update AmauryPi's map and set it to covered
- Permissions: `imageonmap.update`, `imageonmap.updateother`

### `/maptool quota`

Displays the player's current map quota usage and remaining maps.

- This command can only be used by a player.
- Shows the current number of maps, map limit, remaining maps, and usage percentage.
- If the player has unlimited quota (via `imageonmap.mapquota.unlimited` or permission-based quotas), it will display "unlimited" instead.
- Permission: `imageonmap.list` (same as explore/list commands)

### `/maptool maxsize`

Displays the player's maximum map size limit in blocks.

- This command can only be used by a player.
- Shows the maximum map size (width × height in blocks) the player can create.
- If the player has unlimited size (via `imageonmap.bypassmaxsize` or permission-based limits), it will display "unlimited" instead.
- Permission: `imageonmap.list` (same as explore/list commands)

### `/maptool <new|list|get|delete|explore|update|give|rename|migrate|quota|maxsize>`

Main command to manage the maps. The less used in everyday usage, too.

- The commands names are pretty obvious.
- `/maptool new` is an alias of `/tomap`.
- `/maptool explore` is an alias of `/maps`.
- `/maptool give` is an alias of `/givemap`.
- `/maptool update` allow to update a specific map.
- `/maptool quota` displays the player's current map quota usage.
- `/maptool maxsize` displays the player's maximum map size limit.
- `/maptool migrate` migrates the old maps when you upgrade from IoM <= 2.7 to IoM 3.0. You HAVE TO execute this command to retrieve all maps when you do such a migration.
- the followings commands come with an extra permission `imageonmap.CMDNAMEother`:
  - `/maptool list|get|delete|explore|update`
- Permissions:
  - `imageonmap.new` for `/maptool new`;
  - `imageonmap.list` for both `/maptool list` and `/maptool explore`;
  - `imageonmap.get` for `/maptool get`;
  - `imageonmap.delete` for `/maptool delete`;
  - `imageonmap.administrative` for `/maptool migrate`.
  - `imageonmap.explore` for `/maptool explore`;
  - `imageonmap.update` for `/maptool update`;
  - `imageonmap.give` for `/maptool give`;
  - `imageonmap.list` for `/maptool quota`;
  - `imageonmap.list` for `/maptool maxsize`.
  

### About the permissions

All permissions are by default granted to everyone, with the exception of `imageonmap.administrative`, `imageonmap.give` and the ones that used the suffix `other` . We believe that in most cases, servers administrators want to give the availability to create images on maps to every player.
Negate a permission using a plugin manager to remove it, if you want to restrict this possibility to a set of users.

You can grant `imageonmap.*` to users, as this permission is a shortcut for all _user_ permissions (excluding `imageonmap.administrative` , `imageonmap.give` and every permission with the prefix `other` that are intended for moderation usage).

#### Economy-related permissions

- `imageonmap.bypasscost` — Allows players to create and update maps without paying the economy cost. Default: `op`

#### Map quota permissions

These permissions allow you to override the `map-player-limit` config setting on a per-player basis:

- `imageonmap.mapquota.unlimited` — Allows unlimited map creation, bypassing the configured limit. Default: `op`
- `imageonmap.mapquota.<number>` — Sets a custom map limit for the player (e.g., `imageonmap.mapquota.50` allows 50 maps). If a player has multiple quota permissions, the highest value is used. Default: `false`

**Examples:**
- Give a VIP player 100 maps: Grant `imageonmap.mapquota.100`
- Give admins unlimited maps: Grant `imageonmap.mapquota.unlimited`
- Default players use the `map-player-limit` value from config.yml


## Configuration

```yaml
# Plugin language. Empty: system language.
# Available: en-US (default, fallback), fr-FR, ru-RU, de-DE, zh-CN, ja-JP.
lang:


# Allows collection of anonymous statistics on plugin environment and usage
# The statistics are publicly visible here: http://mcstats.org/plugin/ImageOnMap
collect-data: true


# Images rendered on maps consume Minecraft maps ID, and there are only 32 767 of them.
# You can limit the maximum number of maps a player, or the whole server, can use with ImageOnMap.
# 0 means unlimited.
map-global-limit: 0
map-player-limit: 0


# Maximum map size in blocks (width x height). Players need imageonmap.bypassmaxsize to bypass this limit.
# For example, a 10x10 map = 100 blocks total. 0 means unlimited.
# Permissions like imageonmap.maxsize.<number> can override this value per player.
max-map-size: 100


# Maximum size in pixels for an image to be. 0 is unlimited.
limit-map-size-x: 0
limit-map-size-y: 0


# Should the full image be saved when a map is rendered?
save-full-image: false


# Economy settings (requires Vault plugin)
economy:
  # Enable economy integration
  enabled: false

  # Cost per map block (e.g., 2x2 map = 4 blocks = 4000 with cost-per-map: 1000)
  cost-per-map: 1000.0

  # Whether to refund players when they delete a map
  refund-on-delete: true

  # Percentage of the original cost to refund (0.0 to 1.0)
  # 0.5 = 50% refund, 1.0 = 100% refund
  refund-percentage: 0.5

  # Locale for economy messages (e.g., en-US, ja-JP)
  # Users can copy locale/message_en-US.yml to message_ja-JP.yml and translate
  locale: en-US
```

### Economy System (Vault Integration)

ImageOnMap supports optional economy integration via Vault. When enabled:

- **Players are charged** based on map size (e.g., 2×2 map = 4 blocks = 4000 currency with default settings)
- **Confirmation dialog** shows map size (with dimensions), cost, and player balance before creation
- **Automatic refunds** when players delete maps (configurable percentage)
- **Bypass permission** (`imageonmap.bypasscost`) for staff/OPs to create maps for free
- **User-editable messages** via locale files (`locale/message_en-US.yml`, `locale/message_ja-JP.yml`, etc.)

To enable economy features:
1. Install [Vault](https://www.spigotmc.org/resources/vault.34315/) and an economy plugin (e.g., EssentialsX)
2. Set `economy.enabled: true` in config.yml
3. Customize costs, refund settings, and locale as needed
4. Optionally translate messages by copying `locale/message_en-US.yml` to other locales

### Map Size Limit System

ImageOnMap allows you to restrict the maximum map size (in blocks) that players can create:

- **Default limit** set via `max-map-size` in config.yml (default: 100 blocks)
- **Size calculation**: Width × Height (e.g., 10×10 map = 100 blocks)
- **Per-player limits** via permissions:
  - `imageonmap.bypassmaxsize` - Unlimited map size (default: OP)
  - `imageonmap.maxsize.<number>` - Specific limit (e.g., `imageonmap.maxsize.200` allows up to 200 blocks)
  - If multiple maxsize permissions are present, the highest value is used
- **Check limit**: Use `/maptool maxsize` to see your current limit

Example permission setup:
```yaml
# LuckPerms example
luckperms:
  user:
    regular_player:
      permissions:
        - imageonmap.new: true
        # Uses default config value (100 blocks)

    vip_player:
      permissions:
        - imageonmap.new: true
        - imageonmap.maxsize.200: true
        # Can create maps up to 200 blocks

    admin:
      permissions:
        - imageonmap.new: true
        - imageonmap.bypassmaxsize: true
        # Unlimited map size
```

## Changelog

### 5.1.0 — Economy & Minecraft 1.21.4 Update

This version adds Vault economy integration and updates to Minecraft 1.21.4 compatibility.

**New Features:**
- **Vault Economy Integration** — Optional economy system with configurable costs based on map size
  - Charge players per map block (e.g., 2×2 map = 4 blocks)
  - Interactive confirmation dialog showing map size (with dimensions), cost, and balance
  - Automatic refunds on map deletion with configurable percentage
  - Bypass permission for staff/OPs (`imageonmap.bypasscost`)
- **User-Editable Locale System** — Economy messages can be translated via YAML files
  - Create `locale/message_<locale>.yml` files (e.g., `message_ja-JP.yml`)
  - Includes full Japanese translation (`message_ja-JP.yml`)
  - Supports color codes and placeholders like existing i18n system
- **Map Size Limit System** — Restrict maximum map size (width × height) per player
  - Default limit configurable via `max-map-size` in config.yml (default: 100 blocks)
  - Per-player limits via `imageonmap.maxsize.<number>` permissions
  - Bypass permission for unlimited size (`imageonmap.bypassmaxsize`)
  - Check current limit with `/maptool maxsize` command
  - Prevents money loss by checking size before charging
- **Map Size Display** — Confirmation shows dimensions: "4 blocks (2×2)"
- **Per-Player Map Quotas** — Override map-player-limit via permissions
  - `imageonmap.mapquota.<number>` for specific limits
  - `imageonmap.mapquota.unlimited` for unlimited maps
  - Check current quota with `/maptool quota` command
- **Config Migration** — Automatically adds new config options on plugin update

**Technical Changes:**
- Updated Paper API from 1.19.3 to 1.21.4
- Fixed `Enchantment.DURABILITY` → `Enchantment.UNBREAKING` for 1.21.4
- Added `LocaleManager` class for YAML-based translations
- Added `VaultEconomyManager` class for economy operations
- Ensured all Bukkit API calls run on main thread

**Configuration:**
- New `economy` section in config.yml with 5 settings
- New `max-map-size` config option (default: 100 blocks)
- New permissions:
  - `imageonmap.bypasscost` (default: op) - Free map creation
  - `imageonmap.bypassmaxsize` (default: op) - Unlimited map size
  - `imageonmap.maxsize.<number>` (default: false) - Custom size limit
  - `imageonmap.mapquota.<number>` (default: false) - Custom map quota
  - `imageonmap.mapquota.unlimited` (default: op) - Unlimited map quota
- New locale files in `locale/` directory
- New commands: `/maptool quota` and `/maptool maxsize`

**Requirements:**
- Minecraft 1.21.4+ (Paper server)
- Vault plugin (optional, for economy features)
- Economy plugin (e.g., EssentialsX, if using economy)

### 5.0.1 — Hard Forked by Okocraft Team

This version removed NMS dependence code and old stuff. And, now only supports paper server.

- Removed QuartzLib dependency and copied it in src directly to maintain easily.
- Removed MANY redundant code and nms code in copied QuartzLib.
- Removed migration feature from v3
- Fixed bug that maps already placed in world do not render image.

### 3.0 — The From-Scratch Update

The 3.0 release is a complete rewrite of the original ImageOnMap plugin, now based on QuartzLib, which adds many feature and fixes many bugs.

This new version is not compatible with the older ones, so your older maps will not be loaded. Run the `/maptool migrate` command (as op or in console) in order to get them back in this new version.

You will find amongst the new features:

- New Splatter maps, making it easy to deploy and remove big posters in one click !
- No more item tags when maps are put in item frames !
- Internationalization support (only french and english are supported for now, contributions are welcome)
- Map Quotas (for players and the whole server)
- A new map Manager (based on an inventory interface), to list, rename, get and delete your maps
- Improvements on the commands system (integrated help and autocompletion)
- Asynchronous maps rendering (your server won't freeze anymore when rendering big maps, and you can queue multiple map
  renderings !)
- UUID management (which requires to run `/maptool migrate`)

### 3.1 — The Permissions Update

- Fixed permissions support by adding a full set of permissions for every action of the plugin.

### 4.0 — Subtle Comfort

This version is a bit light in content, but we have unified part of the plugin (splatter map) and we prepared upcoming
changes with required zLib features. The next update should be bigger and will add more stuff : thumbnail, optimization,
possibility to deploy and place item frames in creative mode, creating interactive map that can run a command if you
click on a specific frame…
             
**This version is only compatible with Minecraft 1.15+.** Compatibility for 1.14 and below is dropped for now, but in
the future we will try to bring it back. Use 4.0 pre1 for now, if you require 1.13.2 – 1.14.4 compatibility. As for the
upcoming Minecraft 1.16 version, an update will add compatibility soon after its release.

- **You can now place a map on the ground or on a ceiling.**
- Languages with non-english characters now display correctly (fixed UTF-8 encoding bug).
- Splatter maps no longer throw an exception when placed.
- When a player place a splatter map, other players in the same area see it entirely, including the bottom-left corner.
- Added Russian and German translations (thx to Danechek and squeezer).

### 4.1 — Moderation Update

*4.1 — Moderation Update* gives mods or admins commands to see maps for other players, to give maps, but also to update maps already placed in the world. You can also use ImageOnMap commands from the console or from command blocks, opening a whole new realm of automation around ImageOnMap (read: commands executed from skripts and data-packs should work).

We also fixed some bugs that were reported by lots of people.

**This version is only compatible with Minecraft 1.15+.**

- Added `/maptool update` to change the image attached to a map.
- Added `/givemap` to give a map of a specific player to another.
- All command can now be executed for other players. As example, `/maps username` will allow you to see all `username` maps.
- You can now use ImageOnMap commands from command blocks or from the console.
- Commands now support map name between quotes: "A nice name to have for a map".
- Added `/maptool rename` if you don't want or cannot use the GUI.
- Size limit (in frame).
- Bug fixes & optimizations.
  - Various optimizations.
  - Fixed AWT memory leak—can happen if you do a lot of 50x50 renders.
  - `/maptool list` no longer throw an exception.
  - Fixed an issue where the bottom left map don't render like it should.
  - Fixed the gui rename issue that was the cause of inventory loss.
  
### 4.1.1 — Moderation Update Too

This version fix bugs introduced with 4.1, and others fixed on QuartzLib side,
as this update the QuartzLib version we use to 0.0.3.

- Fixed update message sent even if the plugin is updated.
- Fixed weird update message (raw JSON instead of formatted text).   
- Fixed rare inventory bugs, causing items to be duplicated or barrier blocks to be obtained.
- Fixed possibility to get XP from the glowing effect applied on maps.
- Fixed `/maps` not working on Java 15+ due to JavaScript engine being unavailable.
- Fixed plugin not working on Minehut host due to Javascript engines being restricted.

### 4.1.2 — Moderation Update Three

This version fixes some small console spam bugs and improves the performance of the translations of the plugin.

- Updates to [QuartzLib 0.0.4](https://github.com/zDevelopers/QuartzLib/blob/master/CHANGELOG.md#quartzlib-004),
  where all these improvements were made.

### 5.0.1 — Hard Forked by Okocraft Team

This version removed NMS dependence code and old stuff. And, now only supports paper server.

- Removed QuartzLib dependency and copied it in src directly to maintain easily.
- Removed MANY redundant code and nms code in copied QuartzLib.
- Removed migration feature from v3
- Fixed bug that maps already placed in world do not render image.

## Data collection

We use metrics to collect [basic information about the usage of this plugin](https://bstats.org/plugin/bukkit/ImageOnMap).
This is 100% anonymous (you can check the source code or the network traffic), but can of course be disabled by setting
`collect-data` to false in `config.yml`.
