package com.hp.myfplus.item;

import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.core.EnhanceableBoss;

import lykrast.meetyourfight.entity.SwampjawEntity;
import lykrast.meetyourfight.registry.MYFEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

/** 远古化石之饵：召唤强化版沼牙。用法与本体召唤物一致：按住使用 1 秒，附近 32 格已有 Boss 时不召唤。 */
public class AncientBaitItem extends Item {
    public AncientBaitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(user instanceof Player player) || !(level instanceof ServerLevel server)) return stack;
        if (!level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32), m -> m.isAlive() && m.getType().is(Tags.EntityTypes.BOSSES)).isEmpty()) {
            player.displayClientMessage(Component.translatable("status.meetyourfight.boss_nearby"), true);
            return stack;
        }
        SwampjawEntity fish = MYFEntities.SWAMPJAW.get().create(server);
        if (fish == null) return stack;
        RandomSource r = player.getRandom();
        fish.moveTo(player.getX() + r.nextInt(5) - 2, player.getY() + r.nextInt(6) + 6, player.getZ() + r.nextInt(5) - 2, r.nextFloat() * 360 - 180, 0);
        if (!player.getAbilities().instabuild) fish.setTarget(player);
        fish.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
        fish.finalizeSpawn(server, server.getCurrentDifficultyAt(fish.blockPosition()), MobSpawnType.EVENT, null, null);
        ((EnhanceableBoss) fish).myfplus$enhance();
        server.addFreshEntity(fish);
        server.playSound(null, fish.getX(), fish.getY(), fish.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.5f, 0.6f);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.awardStat(Stats.ITEM_USED.get(this));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.meetyourfight.summon.use").withStyle(ChatFormatting.GRAY));
    }
}
