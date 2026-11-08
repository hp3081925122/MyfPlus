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
    SJ_FIN(Kind.VBILL, "swampjaw/fin", 2, 3, true, 1.5f, 1.5f, 200, false),
    SJ_BREACH(Kind.VBILL, "swampjaw/breach", 6, 2, false, 2.5f, 3.75f, 12, false),
    SJ_DROP(Kind.DECAL_SHRINK, "swampjaw/drop_mark_", 4, 1, false, 4.0f, 4.0f, 18, true),
    SJ_MAW(Kind.BILL, "swampjaw/maw", 4, 2, false, 2.0f, 2.0f, 10, false),
    SJ_CHARGE(Kind.BILL, "swampjaw/charge", 4, 2, true, 1.2f, 1.2f, 24, true),
    SJ_SPIKE(Kind.VBILL_DROP, "swampjaw/bone_spike", 3, 1, false, 1.0f, 2.0f, 32, false),
    SJ_SPOUT(Kind.VBILL, "swampjaw/spout", 5, 2, false, 1.25f, 2.5f, 10, false),
    SJ_SWIPE(Kind.ARC, "swampjaw/swipe", 4, 2, false, 1.0f, 1.0f, 8, false),
    SJ_ROAR(Kind.BILL, "swampjaw/roar", 5, 2, false, 4.0f, 4.0f, 10, true),
    BR_NOTE(Kind.BILL, "bellringer/note", 3, 3, false, 0.75f, 0.75f, 10, true),
    BR_PILLAR(Kind.COLUMN, "bellringer/echo_column", 4, 3, false, 2.0f, 3.4f, 12, true),
    BR_IMPACT(Kind.DECAL, "bellringer/impact_ring", 4, 2, false, 1.0f, 1.0f, 8, true),
    BR_COUNT3(Kind.BILL, "bellringer/count_3", 1, 1, false, 0.9f, 0.9f, 20, true),
    BR_COUNT2(Kind.BILL, "bellringer/count_2", 1, 1, false, 0.9f, 0.9f, 20, true),
    BR_COUNT1(Kind.BILL, "bellringer/count_1", 1, 1, false, 0.9f, 0.9f, 20, true),
    BR_ECHO_BELL(Kind.VBILL, "bellringer/echo_bell", 1, 1, false, 1.5f, 1.5f, 300, true),
    BR_ECHO_BREAK(Kind.VBILL, "bellringer/echo_bell_break", 4, 2, false, 1.5f, 1.5f, 8, true),
    BR_GAP_MARK(Kind.DECAL, "bellringer/wave_gap_mark", 1, 1, false, 1.0f, 1.0f, 20, true),
    FT_BLAST(Kind.BILL, "fortuna/dice_blast", 6, 2, false, 1.0f, 1.0f, 12, true),
    FT_CURSE(Kind.BILL, "fortuna/curse_icon", 2, 6, true, 0.5f, 0.5f, 200, true),
    FT_STARS(Kind.BILL, "fortuna/stun_stars", 4, 3, true, 1.0f, 0.5f, 40, true),
    FT_CARD(Kind.VBILL_DROP, "fortuna/giant_card", 5, 1, false, 2.0f, 3.0f, 80, true),
    FT_CARD_BURST(Kind.BILL, "fortuna/card_burst", 4, 2, false, 2.5f, 2.5f, 8, true),
    FT_COLLAPSE(Kind.BILL, "fortuna/allin_collapse", 6, 3, false, 2.0f, 3.0f, 18, true),
    FT_CROWN(Kind.BILL, "fortuna/allin_crown", 1, 1, false, 1.0f, 1.0f, 40, true),
    FT_BROKEN(Kind.BILL, "fortuna/allin_broken", 1, 1, false, 1.0f, 1.0f, 40, true),
    FT_SMOKE(Kind.BILL, "fortuna/slot_smoke", 4, 3, false, 1.2f, 1.2f, 12, false),
    FT_JACKPOT(Kind.BILL, "fortuna/jackpot", 4, 2, false, 2.5f, 2.5f, 8, true),
    RS_GLINT(Kind.BILL, "rosalyne/glint", 4, 2, false, 0.9f, 0.9f, 8, true),
    RS_ARC(Kind.ARC, "rosalyne/slash_arc", 6, 1, false, 0.8f, 1.0f, 7, true),
    RS_THRUST(Kind.SLASH_LINE, "rosalyne/thrust", 4, 2, false, 1.0f, 1.0f, 8, true),
    RS_SIGIL(Kind.BILL, "rosalyne/parry_sigil", 4, 3, true, 2.0f, 2.0f, 60, true),
    RS_SIGIL_BREAK(Kind.BILL, "rosalyne/parry_break", 5, 2, false, 2.667f, 2.667f, 10, true),
    RS_SUNSET(Kind.BILL, "rosalyne/sunset", 4, 3, true, 3.0f, 3.0f, 40, true),
    RS_SUN_BLADE(Kind.COLUMN, "rosalyne/sun_column", 4, 3, false, 1.3f, 3.0f, 12, true),
    RS_PETAL_BURST(Kind.BILL, "rosalyne/petal_burst", 5, 2, false, 2.0f, 2.0f, 10, true),
    RS_PETAL_BREAK(Kind.BILL, "rosalyne/petal_break", 4, 2, false, 1.0f, 1.0f, 8, false),
    RS_SPROUT(Kind.DECAL, "rosalyne/sprout_mark", 1, 1, false, 1.0f, 1.0f, 20, false),
    RS_GHOST(Kind.GHOST, null, 1, 1, false, 1, 1, 10, true);

    public enum Kind {
        NONE, WARN_LINE, WARN_SECTOR, WARN_CIRCLE, DECAL, DECAL_SHRINK, BILL, VBILL, VBILL_DROP, COLUMN, ARC, SLASH_LINE, ROCK_RING, ROCK_LINE, ROCK_BURST, EVENT, GHOST
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
