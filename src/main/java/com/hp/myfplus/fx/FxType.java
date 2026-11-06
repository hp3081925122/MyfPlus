package com.hp.myfplus.fx;

public enum FxType {
    NONE(Kind.NONE, null, 1, 1, false, 1, 1, 20, false),
    WARN_LINE(Kind.WARN_LINE, null, 1, 1, false, 1, 1, 20, true),
    WARN_SECTOR(Kind.WARN_SECTOR, null, 1, 1, false, 1, 1, 20, true),
    WARN_CIRCLE(Kind.WARN_CIRCLE, null, 1, 1, false, 1, 1, 20, true),
    ROCK_RING(Kind.ROCK_RING, null, 1, 1, false, 1, 1, 30, false),
    ROCK_LINE(Kind.ROCK_LINE, null, 1, 1, false, 1, 1, 30, false),
    ROCK_BURST(Kind.ROCK_BURST, null, 1, 1, false, 1, 1, 36, false),
    SHAKE(Kind.EVENT, null, 1, 1, false, 1, 1, 10, false),
    BANNER(Kind.EVENT, null, 1, 1, false, 1, 1, 10, false),
    SJ_SPLASH(Kind.VBILL, "swampjaw/splash", 4, 2, false, 1.25f, 1.25f, 8, false),
    SJ_HOOK_GLINT(Kind.BILL, "swampjaw/hook_glint", 2, 3, true, 0.5f, 0.5f, 12, true),
    SJ_VINE_SNAP(Kind.BILL, "swampjaw/vine_snap", 3, 3, false, 1.0f, 1.0f, 9, false),
    SJ_RIPPLE(Kind.DECAL, "swampjaw/ripple", 3, 4, false, 1.5f, 1.5f, 12, false),
    SJ_AMBUSH_RING(Kind.DECAL_SHRINK, "swampjaw/ambush_ring_", 4, 1, false, 4.0f, 4.0f, 20, true),
    SJ_GEYSER_SMALL(Kind.VBILL, "swampjaw/geyser_small", 6, 2, false, 1.0f, 2.0f, 12, false),
    SJ_GEYSER(Kind.VBILL, "swampjaw/geyser", 6, 2, false, 1.5f, 4.5f, 12, false),
    SJ_BUBBLE_POP(Kind.BILL, "swampjaw/bubble_pop", 5, 2, false, 2.0f, 2.0f, 10, false),
    SJ_FIN(Kind.VBILL, "swampjaw/fin", 2, 6, true, 1.25f, 1.25f, 200, false),
    SJ_SLAM(Kind.DECAL, "swampjaw/slam_ring", 4, 2, false, 1.0f, 1.0f, 8, false),
    SJ_VORTEX(Kind.DECAL, "swampjaw/vortex", 4, 3, true, 1.0f, 1.0f, 100, false),
    SJ_CHOMP(Kind.BILL, "swampjaw/chomp", 4, 2, false, 2.0f, 2.0f, 8, false);

    public enum Kind {
        NONE, WARN_LINE, WARN_SECTOR, WARN_CIRCLE, DECAL, DECAL_SHRINK, BILL, VBILL, ROCK_RING, ROCK_LINE, ROCK_BURST, EVENT
    }

    public final Kind kind;
    public final String texture;
    public final int frames;
    public final int ticksPerFrame;
    public final boolean loop;
    public final float width;
    public final float height;
    public final int life;
    public final boolean bright;

    FxType(Kind kind, String texture, int frames, int ticksPerFrame, boolean loop, float width, float height, int life, boolean bright) {
        this.kind = kind;
        this.texture = texture;
        this.frames = frames;
        this.ticksPerFrame = ticksPerFrame;
        this.loop = loop;
        this.width = width;
        this.height = height;
        this.life = life;
        this.bright = bright;
    }

    public static FxType byId(int id) {
        FxType[] v = values();
        return id >= 0 && id < v.length ? v[id] : NONE;
    }
}
