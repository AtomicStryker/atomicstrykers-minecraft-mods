If you have an old config file, best rename or delete it and let the default generate once.


Inside the "needles" json list:

Entries may be added in the following format:

"{BlockID}": [
          R,
          G,
          B,
          ScanrangeHor,
          ScanrangeVer,
          MinBlockY,
          MaxBlockY,
          boolDelayed
        ]


BlockID - the Block Name the compass should look for. Google Minecraft Data for correct values
R:G:B - the color values the needle should use
ScanrangeHor - scanrange -x,-z to +x,+z
ScanrangeVer - scanrange depth, '1' is visible blocks from a 1x2 tunnel
MinBlockY - minimum block height to scan
MaxBlockY - maximum block height to scan
boolDelayed - 1 if the scan should only happen every 15 seconds (long range scans)

to get minecraft IDs visit www.minecraftwiki.net/wiki/Data_values
to get an RGB color just google online RGB mixer

for working examples just look at the default config file.