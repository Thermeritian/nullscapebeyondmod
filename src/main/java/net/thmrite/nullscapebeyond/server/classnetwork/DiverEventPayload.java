package net.thmrite.nullscapebeyond.server.classnetwork;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

public record DiverEventPayload(Kind kind, float value) implements CustomPacketPayload {
    public enum Kind { DIVE, JUMP_OUT, BONK, POSE_ON, POSE_OFF }

    public static final CustomPacketPayload.Type<DiverEventPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "diver_event"));

    public static final StreamCodec<ByteBuf, DiverEventPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(i -> Kind.values()[Mth.clamp(i, 0, Kind.values().length - 1)], Kind::ordinal),
            DiverEventPayload::kind,
            ByteBufCodecs.FLOAT, DiverEventPayload::value,
            DiverEventPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
