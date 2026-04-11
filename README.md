[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://www.youtube.com/watch?v=xvFZjo5PgG0)

# Hotbar Inventory Transfer

![Mod Icon](src/main/resources/assets/hotbarinventorytransfer/icon.png)

## Installation

* [Modrinth](https://modrinth.com/mod/hotbar-inventory-transfer)
* [CurseForge](https://www.curseforge.com/minecraft/mc-mods/hotbar-inventory-transfer)

## Support
  
If you encounter bugs or wish to contribute:
* [Report any problems you find.](https://github.com/armaninyow/Hotbar-Inventory-Transfer/discussions/categories/issues)
* [Share your ideas for new features.](https://github.com/armaninyow/Hotbar-Inventory-Transfer/discussions/categories/suggestions)

## Changelog
<details>
  <summary></summary>
  
### 3.0.0—1.21.x
* Added multi-version support covering Minecraft 1.21 through 1.21.11
* Switched from custom .ogg sound files to vanilla bundle sounds (item.bundle.insert and item.bundle.insert_fail), removing the need for bundled audio assets
* Removed sound playback on 1.21 and 1.21.1 since bundle sounds do not exist in those versions
* Moved "Inventory full!" overlay text up by 4 pixels to avoid overlapping the held item tooltip
* Removed exclamation mark from "Inventory full" overlay message
### 2.0.0—1.21.11
* Updated to Minecraft 1.21.11
### 1.3.0—1.21.10
* Added sound that plays when the inventory is full
* Moved the "Inventory full!" tooltip up to prevent it from overlapping the vanilla Held Item Tooltip
* Added random pitch variation between 75% and 150% to the transfer sounds
* Reduced volume of transfer sounds from 100% to 80%
### 1.2.0—1.21.10
* Added sound effects when you successfully transfer items from your hotbar to your inventory
* Added a red "Inventory full!" message that appears above your hotbar when the transfer fails because your inventory is full
### 1.1.0—1.21.10
* Refreshed the mod's visual identity with a new icon
* Updated development environment and dependencies to target Minecraft version 1.21.10
* Updated yarn_mappings (1.21.10+build.3), loader_version (0.18.4), and loom_version (1.14-SNAPSHOT) to the latest standards
* Refactored code across all files to follow official FabricMC formatting and naming conventions
* Optimized keybinding registration and client-side tick events for better performance and readability
### 1.0.0—1.21.10
* Initial Release
</details>

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://www.youtube.com/watch?v=xvFZjo5PgG0)
