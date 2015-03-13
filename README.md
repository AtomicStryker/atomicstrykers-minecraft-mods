Folder structure this file resides in

./
	./../forgegradle	-> contains gradlew.bat along with the new forge setup
	./**/        		-> contains the Eclipse Workspace with projects in it. There is a master gradle build file each project accesses

To setup my workspace in your eclipse, simply setup Forge, then move your eclipse workspace into this folder.


Each project/mod refers to the master gradle build file which creates download files in their respective /build/libs (or /build/distributions) folders.
Gradle is required, however one could also use the gradlew wrapper found in forgegradle to do this.
The build file reacts to certain conditions like a "META-INF.mf" being present, "makezip" being present...

THIS ASSUMES YOUR MOD COMPILES AGAINST FORGEGRADLE - if there are compile errors in the eclipse project, it will fail.


NOTE: Some projects attempt to include the Update Checker into their packages, which means it should be built first.
NOTE #2: Pet Bat relies on Dynamic Lights to have been compiled first.

Stuff that needs changing each mc version:

- in "./properties.xml" you find the minecraft and forge versions which are used by the build scripts for building and all resulting mod- and archivenames.
(artifacts are named by mc version only, so that filehoster linked files can be overwritten without breaking links)

- each mod has a changelog, add new versions in here
- each mod has a @Mod sourcefile, generally in the common package, in which you need to bump "version =" property accordingly


Mods with special Needs:

-- Multi Mine
- Multi Mine needs it's dummy jar (containing only the Manifest.MF) in the runtime /mods/ folder if you want it to work during debugging
- Multi Mine uses hardcoded obfuscated names in it's sourcefile "common\atomicstryker\multimine\common\fmlmagic\MMTransformer.java"
- those need to be fixed to the current Searge names each time minecraft obfuscation changes
- a good place to get current obfuscated names from is joined.srg, methods.csv and fields.csv somewhere in the forgegradle folders
- another good place is MCPBot in the MCP IRC channels, assuming it runs the version you need

-- Dynamic Lights
- similar to Multi Mine. sourcefile is "common\atomicstryker\dynamiclights\common\DLTransformer.java"

-- Stalker Creepers
- similar to Multi Mine. sourcefile is "common\atomicstryker\stalkercreepers\common\SCTransformer.java"

-- Kenshiro Mod
- uses some hacky stuff for rendering and manipulating punched entities. Will probably crash if broken.

-- Advanced Machines and Ropes+
- rely on external packages, namely IC2 and NEI
