 Hotbar Inventory Transfer

A Fabric mod for Minecraft 1.21.10 that allows you to quickly transfer items from your hotbar to your inventory using a customizable keybind.

 Features

- Press a key (default: R) while holding a hotbar item to instantly move it to your inventory
- No need to open your inventory screen
- Fully customizable keybind in Minecraft's Controls settings
- Lightweight and client-side only

 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.10
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api) version 0.138.4+1.21.10 or later
3. Place this mod's JAR file in your `.minecraft/mods` folder
4. Launch Minecraft with the Fabric profile

 Building from Source

 Prerequisites
- Java 21 or higher
- Git (optional)

 Setup

1. Create the project structure:
```
inventoryswap/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── inventoryswap/
│       │               └── InventorySwapMod.java
│       └── resources/
│           ├── fabric.mod.json
│           └── assets/
│               └── inventoryswap/
│                   └── lang/
│                       └── en_us.json
├── build.gradle
├── gradle.properties
└── settings.gradle
```

2. Create settings.gradle:
```groovy
pluginManagement {
&nbsp;   repositories {
&nbsp;       maven {
&nbsp;           name = 'Fabric'
&nbsp;           url = 'https://maven.fabricmc.net/'
&nbsp;       }
&nbsp;       gradlePluginPortal()
&nbsp;   }
}
```

3. Place all the provided files in their correct locations:

- `InventorySwapMod.java` → `src/main/java/com/example/inventoryswap/`
- `fabric.mod.json` → `src/main/resources/`
- `en_us.json` → `src/main/resources/assets/inventoryswap/lang/`
- `build.gradle` → root directory
- `gradle.properties` → root directory

4. Build the mod:
```bash
./gradlew build
```

The compiled JAR will be in `build/libs/inventoryswap-1.0.0.jar`

 🎮 How to Use

1. Hold any item in your hotbar
2. Press R (or your configured key)
3. The item instantly moves to your inventory - stacking with matching items first, then finding empty slots

 🔧 Technical Details

- Minecraft Version: 1.21.10
- Mod Loader: Fabric
- Fabric Loader: 0.17.3 or higher
- Required: Fabric API 0.138.4+1.21.10 or later
- Side: Client-side (can be installed on client only in multiplayer)

 License

MIT License - Feel free to modify and redistribute

 Notes

- If your inventory is full, the swap won't happen
- The mod only affects the client-side, so it works on any server
- Compatible with most other inventory mods


