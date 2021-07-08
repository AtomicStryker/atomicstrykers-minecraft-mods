
Since Forge 1.13.2, each mod has its own seperate folder and (Intellij/Eclipse) project

Generally, you just open the build.gradle in Intellij, select the gradle wrapper included in the repo, enable auto-import, and it should already build.

There is also a "curseforge" build target my successor can probably safely ignore.


- each mod has a build.gradle and changelog.txt, add new versions in here


Mods with special Needs, aka coremods:

-- Multi Mine
-- Stalker Creepers
-- Finder Compass
