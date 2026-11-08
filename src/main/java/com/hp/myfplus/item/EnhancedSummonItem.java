package com.hp.myfplus.item;

import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

/** 强化召唤物：用法与本体召唤物一致，按住使用 1 秒，附近 32 格已有 Boss 时不召唤，召唤出本模组的强化 Boss 实体。 */
public class EnhancedSummonItem extends Item {
    private final BiConsumer<Player, ServerLevel> spawner;

    public EnhancedSummonItem(Properties properties, BiConsumer<Player, ServerLevel> spawner) {
        super(properties);
        this.spawner = spawner;
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
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(user instanceof Player player) || !(level instanceof ServerLevel server)) return stack;
        if (!level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32), m -> m.isAlive() && m.getType().is(Tags.EntityTypes.BOSSES)).isEmpty()) {
            player.displayClientMessage(Component.translatable("status.meetyourfight.boss_nearby"), true);
            return stack;
        }
        spawner.accept(player, server);
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
