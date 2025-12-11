package com.fireblaze.magic_overhaul;

import com.fireblaze.magic_overhaul.blockentity.EnchantingTable.ArcaneEnchantingTableBlockEntity;
import com.fireblaze.magic_overhaul.network.Network;
import com.fireblaze.magic_overhaul.registry.ModEnchantments;
import com.fireblaze.magic_overhaul.util.BindingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "magic_overhaul")
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BlockPos bound = BindingManager.getBoundTable(player);
        long posLong = bound != null ? bound.asLong() : 0L;

        Network.sendToClient(player, new Network.SyncBoundTablePacket(posLong));
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        // Prüfen, ob das Item in der Hand die Verzauberung "Veil of Eternity" hat
        ItemStack stack = player.getMainHandItem(); // Haupt-Hand
        if (stack.isEmpty()) return;

        // Prüfe, ob das Item verzaubert ist
        if (stack.getEnchantmentLevel(ModEnchantments.VEIL_OF_ETERNITY.get()) > 0) {
            BlockPos pos = BindingManager.getBoundTable(player);
            if (pos == null) {
                stack.getOrCreateTag().putBoolean("Unbreakable", false);
                return;
            }
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof ArcaneEnchantingTableBlockEntity tableBE) {
                float magicPower = tableBE.getMagicAccumulator().getCurrentMagicPower();

                if (magicPower > 0) {
                    // Magie verbrauchen
                    tableBE.getMagicAccumulator().setAccumulatedMagicPower(magicPower - 1);
                    stack.getOrCreateTag().putBoolean("Unbreakable", true);
                }
                else stack.getOrCreateTag().putBoolean("Unbreakable", false);
            }
        }
    }

    @SubscribeEvent public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;

        // Prüfen, ob das Item in der Hand die Verzauberung "Veil of Eternity" hat
        ItemStack stack = player.getMainHandItem(); // Haupt-Hand
        if (stack.isEmpty()) return;

        // Prüfe, ob das Item verzaubert ist
        if (stack.getEnchantmentLevel(ModEnchantments.VEIL_OF_ETERNITY.get()) > 0) {
            int damage = stack.getItem().getDamage(stack);
            if (damage > 0) {
                BlockPos pos = BindingManager.getBoundTable(player);
                if (pos == null) return;
                BlockEntity be = player.level().getBlockEntity(pos);
                if (be instanceof ArcaneEnchantingTableBlockEntity tableBE) {
                    float magicPower = tableBE.getMagicAccumulator().getCurrentMagicPower();
                    if (magicPower > 0) {
                        tableBE.getMagicAccumulator().setAccumulatedMagicPower(magicPower - 1);
                        stack.setDamageValue(damage - 1);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // Vanilla Enchanting Table?
        if (state.getBlock() instanceof EnchantmentTableBlock) {

            // Nur Server-seitig handeln
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.literal("Enchantment Table disabled. Use The Arcane Enchantment Table instead."),
                        true
                );
            }

            // Verhindert, dass sich das GUI öffnet
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

}

