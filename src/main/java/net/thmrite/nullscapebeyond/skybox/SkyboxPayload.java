package net.thmrite.nullscapebeyond.skybox;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/** Server -> client message that changes the player's skybox state. */
public record SkyboxPayload(Op op, String skybox, int axis, float value) implements CustomPacketPayload {

    public enum Op { SET, CLEAR, MOTION, PULSE, EFFECT_CLEAR }

    public enum Direction { LEFT, RIGHT, UP, DOWN, CLOCKWISE, COUNTERCLOCKWISE }

    public static final Type<SkyboxPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "skybox"));

    public static final StreamCodec<ByteBuf, SkyboxPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(i -> Op.values()[i], Op::ordinal), SkyboxPayload::op,
            ByteBufCodecs.STRING_UTF8, SkyboxPayload::skybox,
            ByteBufCodecs.VAR_INT, SkyboxPayload::axis,
            ByteBufCodecs.FLOAT, SkyboxPayload::value,
            SkyboxPayload::new);

    public static SkyboxPayload set(SkyboxType type) {
        return new SkyboxPayload(Op.SET, type.id(), 0, 0f);
    }

    public static SkyboxPayload clear() {
        return new SkyboxPayload(Op.CLEAR, "", 0, 0f);
    }

    /** speed in degrees per second */
    public static SkyboxPayload motion(Direction dir, float speed) {
        return new SkyboxPayload(Op.MOTION, "", dir.ordinal(), speed);
    }

    /** channel: 0 = R, 1 = G, 2 = B; seconds = length of one full pulse cycle */
    public static SkyboxPayload pulse(int channel, float seconds) {
        return new SkyboxPayload(Op.PULSE, "", channel, seconds);
    }

    public static SkyboxPayload effectClear() {
        return new SkyboxPayload(Op.EFFECT_CLEAR, "", 0, 0f);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}