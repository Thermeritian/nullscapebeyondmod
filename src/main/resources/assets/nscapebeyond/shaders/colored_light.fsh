#version 150

uniform sampler2D SceneColor;
uniform sampler2D SceneDepth;
uniform sampler2D LightGrid;

uniform mat4 InvProjMat;
uniform mat4 InvModelViewMat;
uniform vec3 CamInGrid;
uniform float Strength;
uniform float EncodeRange;

in vec2 texCoord;
out vec4 fragColor;

// Must match LightGridBuilder: 64^3 volume stored as an 8x8 atlas of 64x64 slices (slice = Y).
const float GRID = 64.0;
const float TILES = 8.0;
const float ATLAS = 512.0;

vec3 fetchSlice(float slice, vec2 xz) {
    vec2 tile = vec2(mod(slice, TILES), floor(slice / TILES));
    vec2 p = tile * GRID + clamp(xz, vec2(0.5), vec2(GRID - 0.5));
    return texture(LightGrid, p / ATLAS).rgb;
}

// gp: continuous grid coordinates, cell i covers [i, i + 1). Manual trilinear (bilinear in-slice, lerp across slices).
vec3 sampleLight(vec3 gp) {
    float y = gp.y - 0.5;
    float y0 = floor(y);
    float f = y - y0;
    float s0 = clamp(y0, 0.0, GRID - 1.0);
    float s1 = clamp(y0 + 1.0, 0.0, GRID - 1.0);
    return mix(fetchSlice(s0, gp.xz), fetchSlice(s1, gp.xz), f);
}

void main() {
    float depth = texture(SceneDepth, texCoord).r;
    if (depth >= 1.0) discard; // sky

    // Camera-relative world position of this pixel.
    vec4 ndc = vec4(vec3(texCoord, depth) * 2.0 - 1.0, 1.0);
    vec4 view = InvProjMat * ndc;
    view.xyz /= view.w;
    vec3 p = (InvModelViewMat * vec4(view.xyz, 1.0)).xyz;

    // Surface normal from screen-space derivatives, used to step off the surface into the lit air cell.
    vec3 n = normalize(cross(dFdx(p), dFdy(p)));
    if (dot(n, p) > 0.0) n = -n;

    vec3 gp = p + CamInGrid + n * 0.5;
    vec3 e = min(gp, vec3(GRID) - gp);
    float edge = clamp(min(e.x, min(e.y, e.z)) / 4.0, 0.0, 1.0);
    if (edge <= 0.0) discard;

    vec3 light = sampleLight(gp) * EncodeRange * edge;
    if (max(light.r, max(light.g, light.b)) < 0.003) discard;

    vec3 scene = texture(SceneColor, texCoord).rgb;
    float lum = dot(scene, vec3(0.299, 0.587, 0.114));
    float dark = 1.0 - smoothstep(0.45, 0.95, lum);       // daylight-bright surfaces take less colour
    vec3 base = max(scene, vec3(0.07));                   // lets very dark surfaces still pick up colour
    vec3 lit = scene + base * light * Strength * mix(0.35, 2.0, dark);

    fragColor = vec4(lit, 1.0);
}
