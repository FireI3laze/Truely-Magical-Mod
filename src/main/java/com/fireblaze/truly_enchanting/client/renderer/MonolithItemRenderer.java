package com.fireblaze.truly_enchanting.client.renderer;

import com.fireblaze.truly_enchanting.block.MonolithBlock;
import com.fireblaze.truly_enchanting.blockentity.MonolithBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.entity.ItemRenderer;

import java.util.Objects;

public class MonolithItemRenderer implements BlockEntityRenderer<MonolithBlockEntity> {

    public MonolithItemRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MonolithBlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        ItemStack stack = entity.getItemInSlot(0);
        if (stack.isEmpty()) return;

        ItemRenderer itemRenderer = net.minecraft.client.Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, entity.getLevel(), null, 0);

        // --- Facing des Blocks
        Direction facing = entity.getBlockState().getValue(MonolithBlock.FACING);
        float blockYRot = switch (facing) {
            case NORTH -> 0f;
            case EAST  -> 90f;
            case SOUTH -> 180f;
            case WEST  -> 270f;
            default -> 0f;
        };

        // --- Spielerposition
        var player = net.minecraft.client.Minecraft.getInstance().player;
        double targetPY = 1.0;  // default unten
        float targetYaw = blockYRot + 90; // default Blockrotation
        boolean rotateToPlayer = false;    // Steuert, ob Rune zum Spieler drehen soll
        boolean freeRotate = false;        // Steuert, ob Rune sich frei dreht

        if (player != null) {
            double dx = player.getX() - (entity.getBlockPos().getX() + 0.5);
            double dz = player.getZ() - (entity.getBlockPos().getZ() + 0.5);
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 15.0) {
                // Phase 1: Rune unten, still
                targetPY = 1.0;
            } else if (distance <= 15.0 && distance > 10.0) {
                // Phase 2: Rune oben, langsam frei drehend
                targetPY = 1.76;
                freeRotate = true;
            } else {
                // Phase 3: Rune oben, zeigt zum Spieler
                targetPY = 1.76;
                rotateToPlayer = true;
                targetYaw = (float) Math.toDegrees(Math.atan2(dx, dz));
            }
        }

        // --- Alte Werte interpolieren für sanfte Bewegung
        entity.itemPY = (float) (entity.itemPY + (targetPY - entity.itemPY) * 0.05f); // sanftes Hoch/Runter

        float currentYaw = entity.itemYaw;
        if (rotateToPlayer) {
            float newYaw = currentYaw + Mth.wrapDegrees(targetYaw - currentYaw) * 0.02f; // sanfte Verzögerung Rotation
            entity.itemYaw = newYaw;
        } else if (freeRotate) {
            // Langsame Eigenrotation in Phase 2
            entity.itemYaw += 0.5f; // langsame Rotation pro Tick
        } else {
            entity.itemYaw = blockYRot + 90;
        }

        // --- Rendern
        poseStack.pushPose();
        poseStack.translate(0.5, entity.itemPY, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.itemYaw));
        poseStack.scale(1f, 1f, 1f);

        int light = 0xF000F0;
        itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, light, combinedOverlay, model);
        poseStack.popPose();
    }
}

