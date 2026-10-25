package net.thmrite.nullscapebeyond.server.effects;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/** Server -> client: "entity X is (no longer) voidbound". Vanilla does not send mob effects to clients when they change. */
public record VoidboundSyncPayload(int entityId, boolean voidbound) implements CustomPacketPayload {
    public static final Type<VoidboundSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "voidbound_sync"));

    public static final StreamCodec<ByteBuf, VoidboundSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, VoidboundSyncPayload::entityId,
            ByteBufCodecs.BOOL, VoidboundSyncPayload::voidbound,
            VoidboundSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}