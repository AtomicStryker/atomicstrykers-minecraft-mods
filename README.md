
This violently short readme applies to 1.17 and newer, and only to mods which have been updated to the respective versions of minecraft.
There is outdated/unmaintained mods in the repository also, be aware.

In the respective mod folder, run "gradlew build" and verify it succeeds.
If it does not, the mod might lack the basic setup for the newer MC versions, to be done in gradle.build and by replacing the gradle wrapper to one taken from a matching forge MDK.

run "gradlew genIntellijRuns" to get IDEA project files for the project, and starting client and server for testing and debugging.

Use Intellij to open the folder or the gradle.build (Open as project).


In the ideal case all that is required to upgrade to a newer MC version is changing the variables "mcversion", "version" and "mappings channel:" and also updating the "minecraft" dependency pointing to a relevant forge version. Forge builds can be found here: https://files.minecraftforge.net/net/minecraftforge/forge/
