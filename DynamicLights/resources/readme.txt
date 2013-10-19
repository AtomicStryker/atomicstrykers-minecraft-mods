
Dynamic Lights now modular!

BurningEntitiesLightSource -> Living Entities that burst into flames! Multithreaded! *expensive*
ChargingCreeperLightSource -> Creepers about to explode light up! *cheap*
DroppedItemsLightSource -> Dropped Items in the world. Also when they're on fire. Multithreaded! *expensive*
PlayerSelfLightSource -> Handheld Items and Armor on yourself. Also when you're on fire. *cheap*
PlayerOthersLightSource -> Handheld Items and Armor on others. Also when they're on fire. Multithreaded! *fairly cheap*

To remove modules you dont want or cant afford (your machine is an asthmatic train wreck, or a Mac),
simply remove (delete) them from the mod .jar /atomicstryker/dynamiclights/client/modules folder.
Yes, the mod will continue to work. If you delete all modules, the mod won't do anything.

CAUTION: Each has it's own config! (or none) There is no global config file!
You can set an important performance setting (update interval) for each module in their config.

There is a global on/off button which you can find and rebind in the Options "Control" menu. It defaults to "L".


To install:

Dynamic Lights is now a FML coremod. That means drag and drop installation! Simply put the .jar found in /setup/coremods/ into your .minecraft/coremods/ folder!
Minecraft 1.6.2 and later: Coremods now go into the /mods/ folder along with everything else! NOT the coremods folder.

Out of the box support for Optifine!



How the advanced config syntax works:
[DynamicLights_thePlayer, DynamicLights_dropItems and DynamicLights_otherPlayers configs ONLY, other configs state their syntax in comments]

* Possible ID setups:
* X := simple ID X, wildcards metadata
* X-Y := simple ID X and metadata Y
* X-Y-Z := simple ID X, metadata range Y to Z
* A-B-C-D := ID range A to B, meta range C to D

There is a default value used as "light strength setting", you can specify one by appending "=x" to the ID part where x is the numeric minecraft light level
Note Dynamic Lights might ignore low light settings


Valid Entry examples:

50
Torch BlockID, 50, will use the default light value (15)

35-* (equals 35)
Wool BlockID, covers all wool metadata/colors

35-2=12
Wool BlockID, 35, magenta subtype (meta 2), will use a light value of 12

35-2-5
Wool BlockID, accepts metadata range [2..5]

314-317-*-*=15
Item ID range [314..317] covering golden armor, wildcarded meta/damage which means any value goes
Also unnecessarily specifying the default light value of 15, you could leave =15 out aswell



You can also substitute block/item IDs with their untranslated String values, useful for mod items
tile.cloth is the same as 35, so tile.cloth-2-5=10 is valid
item.helmetGold-item.leggingsGold-*-* is valid too
To get untranslated String values off mods ask their creators or use a Java decompiler

Values that cannot be mapped to anything will be logged and ignored 