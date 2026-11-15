package net.thmrite.nullscapebeyond.client.renderer;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public class ModBoatRenderer extends BoatRenderer {
    private final Pair<ResourceLocation, ListModel<Boat>> resources;

    public ModBoatRenderer(EntityRendererProvider.Context context, String name, boolean chest) {
        super(context, chest);
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID,
                "textures/entity/" + (chest ? "chest_boat/" : "boat/") + name + ".png");
        ModelLayerLocation layer = layer(name, chest);
        ListModel<Boat> model = chest ? new ChestBoatModel(context.bakeLayer(layer)) : new BoatModel(context.bakeLayer(layer));
        this.resources = Pair.of(texture, model);
    }

    @Override
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
        return resources;
    }

    public static ModelLayerLocation layer(String name, boolean chest) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID,
                (chest ? "chest_boat/" : "boat/") + name), "main");
    }
}
