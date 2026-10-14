package net.thmrite.nullscapebeyond.registry;

import java.util.function.Supplier;

import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NullscapeBeyond.MODID);

    /** True while a player is being swallowed by a beacon (synced to everyone tracking them, never saved). */
    public static final Supplier<AttachmentType<Boolean>> BEACON_ENTERING = ATTACHMENT_TYPES.register(
            "beacon_entering",
            () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());

    private ModAttachments() {}
}