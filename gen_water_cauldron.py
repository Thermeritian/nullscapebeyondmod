#!/usr/bin/env python3
"""Resources for the cauldron of a water-like fluid (see registry/WaterLikeCauldrons.java).
Run from the project root:
    python gen_water_cauldron.py nulqua --area 20 --display Nulqua
Options:
    --area N        the fluid uses a tiled area texture (the content shows tile 0_0; change --tile for another)
    --tile X_Z      which tile to show inside the cauldron (default 0_0), only with --area
    --display NAME  English name (default: name with capitals)
Shared files (the minecraft:cauldrons tag) are merged with what is already in the project."""
import argparse, json, os

MOD = "nscapebeyond"
ap = argparse.ArgumentParser()
ap.add_argument("name")
ap.add_argument("--area", type=int, default=0)
ap.add_argument("--tile", default="0_0")
ap.add_argument("--display")
ap.add_argument("--out", default=".")
a = ap.parse_args()
n = a.name
c = f"{n}_cauldron"
display = a.display or n.replace("_", " ").title()
RES = os.path.join(a.out, "src/main/resources")
A, DT, MC = f"{RES}/assets/{MOD}", f"{RES}/data/{MOD}", f"{RES}/data/minecraft"

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2); f.write("\n")

content = f"{MOD}:fluid/{n}/{a.tile}" if a.area else f"{MOD}:block/{n}_still"
for suffix, parent in (("level1", "template_cauldron_level1"), ("level2", "template_cauldron_level2"), ("full", "template_cauldron_full")):
    w(f"{A}/models/block/{c}_{suffix}.json", {"parent": f"minecraft:block/{parent}", "render_type": "minecraft:translucent", "textures": {"content": content}})
w(f"{A}/blockstates/{c}.json", {"variants": {
    "level=1": {"model": f"{MOD}:block/{c}_level1"},
    "level=2": {"model": f"{MOD}:block/{c}_level2"},
    "level=3": {"model": f"{MOD}:block/{c}_full"}}})

# breaking it gives back a plain cauldron
w(f"{DT}/loot_table/blocks/{c}.json", {"type": "minecraft:block", "random_sequence": f"{MOD}:blocks/{c}", "pools": [
    {"rolls": 1.0, "bonus_rolls": 0.0, "conditions": [{"condition": "minecraft:survives_explosion"}],
     "entries": [{"type": "minecraft:item", "name": "minecraft:cauldron"}]}]})

# minecraft:cauldrons block tag (merged)
path = f"{MC}/tags/block/cauldrons.json"
tag = json.load(open(path)) if os.path.exists(path) else {"replace": False, "values": []}
if f"{MOD}:{c}" not in tag["values"]: tag["values"].append(f"{MOD}:{c}")
w(path, tag)

w(f"{a.out}/_merge/lang_{c}_en_us.json", {f"block.{MOD}.{c}": f"{display} Cauldron"})
print("done:", c)
