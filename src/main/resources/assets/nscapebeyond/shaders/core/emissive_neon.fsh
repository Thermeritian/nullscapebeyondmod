#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float NeonBoost; // 0 = plain fullbright, 1 = full neon look

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) discard;

    // Neon: no lighting at all, richer saturation, brighter, with a slightly white-hot core on the lightest texels.
    float lum = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 neon = mix(vec3(lum), color.rgb, 1.0 + 0.55 * NeonBoost);
    neon *= 1.0 + 0.45 * NeonBoost;
    neon = mix(neon, vec3(1.0), 0.18 * NeonBoost * smoothstep(0.5, 1.0, lum));
    color.rgb = max(neon, vec3(0.0));

    float fog = vertexDistance <= FogStart ? 0.0
              : vertexDistance >= FogEnd ? 1.0
              : (vertexDistance - FogStart) / (FogEnd - FogStart);
    fragColor = vec4(mix(color.rgb, FogColor.rgb, fog * FogColor.a), color.a);
}
