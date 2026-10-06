package net.thmrite.nullscapebeyond.server.classnetwork;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/** Client -> server notification about a Charger event (movement itself stays client-side). */
public record ChargerEventPayload(Kind kind, int targetId, float speed) implements CustomPacketPayload {
    public enum Kind { START, BRAKE, BONK, KNOCK }

    public static final CustomPacketPayload.Type<ChargerEventPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "charger_event"));

    public static final StreamCodec<ByteBuf, ChargerEventPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(i -> Kind.values()[Mth.clamp(i, 0, Kind.values().length - 1)], Kind::ordinal),
            ChargerEventPayload::kind,
            ByteBufCodecs.VAR_INT, ChargerEventPayload::targetId,
            ByteBufCodecs.FLOAT, ChargerEventPayload::speed,
            ChargerEventPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
