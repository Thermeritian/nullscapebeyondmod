package net.thmrite.nullscapebeyond.registry;


public final class ModFluids {
    public static final ModFluidsWater.Entry NULQUA = ModFluidsWater.builder("nulqua")
            .areaTexture(20)      // one 320x320 texture cut into 20x20 tiles
            .withBottle()
            .register();

    public static final ModFluidsCauldron.Cauldron NULQUA_CAULDRON = ModFluidsCauldron.register("nulqua",
            () -> NULQUA.bucket().get(), () -> NULQUA.bottle().get());

    private ModFluids() {
    }

    public static void init() {
    }
}
