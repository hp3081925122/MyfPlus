package com.hp.myfplus.core;

import java.util.List;
import java.util.UUID;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.entity.BoneHookEntity;
import com.hp.myfplus.entity.MudPitEntity;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import lykrast.meetyourfight.config.MYFConfigValues;
import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Myfplus.MOD_ID)
public final class CommonEvents {
    public static final UUID MUD_SLOW = UUID.fromString("5f0d1c8e-6b8a-4f0e-9a3c-7d1e2b4c6a01");
    private static final String ATK_INIT = "myfplus_atk_init";

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent e) {
        if (e.getEntity() instanceof MyfBossHost host && host.myfplus$brain() != null && host.myfplus$brain().blocksAttack(e.getSource())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        if (e.getEntity() instanceof MyfBossHost host && host.myfplus$brain() != null) e.setAmount(host.myfplus$brain().onHurt(e.getSource(), e.getAmount()));
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent e) {
        if (e.getEntity().level().isClientSide) return;
        if (e.getEntity() instanceof MyfBossHost host && host.myfplus$brain() != null) host.myfplus$brain().serverTick();
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent e) {
        LivingEntity jumper = e.getEntity();
        if (jumper.level().isClientSide) return;
        for (BoneHookEntity hook : jumper.level().getEntitiesOfClass(BoneHookEntity.class, jumper.getBoundingBox().inflate(3))) {
            if (hook.hooked == jumper) hook.jumps++;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide) return;
        Player p = e.player;
        AttributeInstance speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        List<MudPitEntity> pits = p.level().getEntitiesOfClass(MudPitEntity.class, p.getBoundingBox().inflate(3, 1.5, 3), pit -> pit.contains(p));
        boolean has = speed.getModifier(MUD_SLOW) != null;
        if (!pits.isEmpty() && !has) speed.addTransientModifier(new AttributeModifier(MUD_SLOW, "myfplus mud", -0.35, AttributeModifier.Operation.MULTIPLY_TOTAL));
        else if (pits.isEmpty() && has) speed.removeModifier(MUD_SLOW);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof Mob mob)) return;
        double base;
        if (mob instanceof BellringerEntity) base = BellringerEntity.DMG * MYFConfigValues.BELLRINGER_DMG_MULT;
        else if (mob instanceof DameFortunaEntity) base = DameFortunaEntity.DMG * MYFConfigValues.FORTUNA_DMG_MULT;
        else return;
        if (mob.getPersistentData().getBoolean(ATK_INIT)) return;
        AttributeInstance atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk == null) return;
        atk.setBaseValue(base);
        mob.getPersistentData().putBoolean(ATK_INIT, true);
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal(Myfplus.MOD_ID).requires(s -> s.hasPermission(2))
                .then(Commands.literal("skill").then(Commands.argument("name", StringArgumentType.word()).executes(CommonEvents::forceSkill)))
                .then(Commands.literal("fx").then(Commands.argument("type", StringArgumentType.word())
                        .executes(c -> spawnFx(c, 2f))
                        .then(Commands.argument("size", FloatArgumentType.floatArg(0.1f, 32f)).executes(c -> spawnFx(c, FloatArgumentType.getFloat(c, "size")))))));
    }

    private static int forceSkill(CommandContext<CommandSourceStack> c) {
        String name = StringArgumentType.getString(c, "name");
        Vec3 pos = c.getSource().getPosition();
        Mob best = null;
        double bestD = 64 * 64;
        for (Mob m : c.getSource().getLevel().getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(pos, pos).inflate(64), m -> m instanceof MyfBossHost h && h.myfplus$brain() != null)) {
            double d = m.distanceToSqr(pos);
            if (d < bestD) {
                bestD = d;
                best = m;
            }
        }
        if (best == null) {
            c.getSource().sendFailure(Component.literal("No reworked boss nearby"));
            return 0;
        }
        BossBrain<?> brain = ((MyfBossHost) best).myfplus$brain();
        if (brain == null || !brain.director.forceByName(name)) {
            c.getSource().sendFailure(Component.literal("Unknown skill: " + name));
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Queued " + name), false);
        return 1;
    }

    private static int spawnFx(CommandContext<CommandSourceStack> c, float size) {
        FxType type;
        try {
            type = FxType.valueOf(StringArgumentType.getString(c, "type").toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            c.getSource().sendFailure(Component.literal("Unknown fx"));
            return 0;
        }
        if (!(c.getSource().getEntity() instanceof ServerPlayer p)) return 0;
        Vec3 look = Combat.dirFromYaw(p.getYRot());
        double x = p.getX() + look.x * 4;
        double z = p.getZ() + look.z * 4;
        double y = GroundProbe.under(p.level(), x, z, p.getY() + 0.5);
        FxEntity fx = FxEntity.at(p.level(), type, x, y, z).size(size).yaw(p.getYRot()).life(type.life * 3);
        if (type == FxType.WARN_SECTOR) fx.line(size, 90f);
        else fx.line(size * 3, 1.5f);
        fx.spawn();
        return 1;
    }
}
