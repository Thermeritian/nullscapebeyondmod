package net.thmrite.nullscapebeyond.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.world.phys.AABB;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class BeaconGeometry {
    private static final String PATH = "/assets/" + NullscapeBeyond.MODID + "/geo/entity/beacon.geo.json";
    private static final String HITBOX_BONE = "Hitbox";

    private record Data(AABB hitbox, double radius, double minY, double maxY) {}

    private static final Data DATA = load();

    public static AABB hitbox() { return DATA.hitbox; }
    public static double radius() { return DATA.radius; }
    public static double minY() { return DATA.minY; }
    public static double maxY() { return DATA.maxY; }

    private static Data load() {
        try (InputStream in = BeaconGeometry.class.getResourceAsStream(PATH)) {
            if (in == null) throw new IOException("missing resource " + PATH);
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject geo = root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();

            AABB hitbox = null;
            double radius = 0, minY = 0, maxY = 0;
            for (JsonElement boneElement : geo.getAsJsonArray("bones")) {
                JsonObject bone = boneElement.getAsJsonObject();
                if (!bone.has("cubes")) continue;
                boolean isHitbox = HITBOX_BONE.equals(bone.get("name").getAsString());
                for (JsonElement cubeElement : bone.getAsJsonArray("cubes")) {
                    AABB box = cubeBox(cubeElement.getAsJsonObject());
                    radius = Math.max(radius, Math.max(
                            Math.max(Math.abs(box.minX), Math.abs(box.maxX)),
                            Math.max(Math.abs(box.minZ), Math.abs(box.maxZ))));
                    minY = Math.min(minY, box.minY);
                    maxY = Math.max(maxY, box.maxY);
                    if (isHitbox && hitbox == null) hitbox = box;
                }
            }
            if (hitbox == null) throw new IOException("bone '" + HITBOX_BONE + "' with a cube not found");
            return new Data(hitbox, radius, minY, maxY);
        } catch (Exception e) {
            NullscapeBeyond.LOGGER.error("Could not read beacon geometry, using fallback hitbox", e);
            return new Data(new AABB(-0.5, 0, -0.5, 0.5, 1, 0.5), 8, 0, 16);
        }
    }

    /** Same conversion GeckoLib does: pixels -> blocks, with X mirrored. Result is relative to the entity origin. */
    private static AABB cubeBox(JsonObject cube) {
        JsonArray o = cube.getAsJsonArray("origin");
        JsonArray s = cube.getAsJsonArray("size");
        double ox = o.get(0).getAsDouble(), oy = o.get(1).getAsDouble(), oz = o.get(2).getAsDouble();
        double sx = s.get(0).getAsDouble(), sy = s.get(1).getAsDouble(), sz = s.get(2).getAsDouble();
        return new AABB(-(ox + sx) / 16.0, oy / 16.0, oz / 16.0, -ox / 16.0, (oy + sy) / 16.0, (oz + sz) / 16.0);
    }

    private BeaconGeometry() {}
}