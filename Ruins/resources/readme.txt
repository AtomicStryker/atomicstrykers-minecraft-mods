Mod Name: Ruins

Authors:
Creator: Justin Bengtson (d00dv4d3r) 
- updated to 1.1 by kolt666
- kept and developed since 1.2.3 by AtomicStryker

Version: see changelog in mod file
License: http://atomicstryker.net/about.html


WHAT IT DOES:
Adds procedurally-generated ruins randomly into the game world, which are created from user-defined template files.
This mod comes with some default template files, but if you would like to see a certain template in the game all you have to do is create it.
There is an ingame template parsing process, a video on my website explains how to use it.


HOW IT DOES IT:
This hooks into the chunk generation using the methods provided by Forge to create the ruins.
You can create anything you like procedurally using template files, even complete structures (I just provide extra rules for creating ruins...)

Templates are loaded for each biome, and a generic set of templates is kept that can be used in all biomes (although spawning conditions for each template must still be met.)
The mod will first check to see if a biome-specific template will be used or whether it will use a generic template.  Chances for either are provided per biome.

You can alter global options for ruin spawning by editing ruins.txt using your favorite text editor.  Options are provided for normal and nether.

Each template has a weight.  All of the template weights for a specific biome, as well as the generic templates,
are added together to get a total weight for that biome from which a random number is generated to determine which template to load.
For instance, if you have five templates for a specific biome with weights 10, 10, 5, 5, and 1, the total of these is 31.
A template with a weight of 1 has a 1 in 31 chance of spawning under those weight conditions.

Please see the template_rules.txt file for more information on creating templates for this mod.  The file is set up so that you can simply copy it to the mods/resources/ruins folder and being editing it.


BIOME SPECIFIC FEATURES
In the mods/resources/ruins folder you will find a folder specific to each biome.
If you place a template in one of those folders it will only be available to spawn in that biome.
If you place a template inside the mods/resources/ruins/generic folder, it will be considered a generic template that can spawn in any biome, other conditions (such as surface blocks) permitting.

The ruins.txt file now has variables for each biome, giving the chance that a biome-specific template will be used as opposed to a generic template.
If a biome-specific template is not available when asked for, no template will spawn.



HOW TO INSTALL:
1. Install Forge
2. Copy everything found in this archive into your corresponding .minecraft/ folders
3a. Enjoy.
3b. Edit /mods/ruins.txt, and any template files for weight, to fine-tune your experience.