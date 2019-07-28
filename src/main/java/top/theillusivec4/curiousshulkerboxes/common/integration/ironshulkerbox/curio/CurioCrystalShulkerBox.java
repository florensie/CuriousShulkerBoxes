/*
 * Copyright (C) 2019  C4
 *
 * This file is part of Curious Shulker Boxes, a mod made for Minecraft.
 *
 * Curious Shulker Boxes is free software: you can redistribute it and/or
 * modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curious Shulker Boxes is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curious Shulker Boxes.  If not, see <https://www.gnu
 * .org/licenses/>.
 */

package top.theillusivec4.curiousshulkerboxes.common.integration.ironshulkerbox.curio;

import com.google.common.primitives.SignedBytes;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.Random;

public class CurioCrystalShulkerBox extends CurioIronShulkerBox {

  private static final float[][] SHIFTS =
          {{-0.075F, 0.325F, 0.325F}, {0.075F, 0.325F, 0.325F},
           {-0.075F, 0.175F, 0.325F}, {0.075F, 0.175F, 0.325F},
           {-0.075F, 0.325F, 0.175F}, {0.075F, 0.325F, 0.175F},
           {-0.075F, 0.175F, 0.175F}, {0.075F, 0.175F, 0.175F},
           {0.0F, 0.25F, 0.25F}};

  private final Random rand = new Random();

  private NonNullList<ItemStack> topStacks;
  private boolean                shouldSyncTopStacks;

  private ItemEntity   customItem;
  private ItemRenderer customRenderer;

  public CurioCrystalShulkerBox(ItemStack stack) {

    super(stack);
    this.topStacks = NonNullList.withSize(8, ItemStack.EMPTY);
  }

  public void setTopStacks(NonNullList<ItemStack> stacks) {

    this.topStacks = stacks;
    this.shouldSyncTopStacks = true;
  }

  @Override
  public boolean shouldSyncToTracking(String identifier,
                                      LivingEntity livingEntity) {

    return shouldSyncTopStacks;
  }

  @Nonnull
  @Override
  public CompoundNBT getSyncTag() {

    return ItemStackHelper.saveAllItems(new CompoundNBT(), this.topStacks);
  }

  @Override
  public void readSyncTag(CompoundNBT compound) {

    ItemStackHelper.loadAllItems(compound, this.topStacks);
  }

  @Override
  public void doRender(String identifier, LivingEntity livingEntity,
                       float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw,
                       float headPitch, float scale) {

    super.doRender(identifier, livingEntity, limbSwing, limbSwingAmount,
                   partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
    GlStateManager.enableCull();
    this.rand.setSeed(254L);
    float shiftX;
    float shiftY;
    float shiftZ;
    int shift = 0;
    float blockScale = 0.32F;
    float timeDiff =
            (float) (360D * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL) -
            partialTicks;

    if (topStacks.get(1).isEmpty()) {
      shift = 8;
      blockScale = 0.4F;
    }

    GlStateManager.pushMatrix();

    if (customItem == null) {
      customItem = new ItemEntity(EntityType.ITEM, livingEntity.world);
    }

    for (ItemStack stack : this.topStacks) {

      if (stack.isEmpty()) {
        shift++;
        continue;
      }

      shiftX = SHIFTS[shift][0];
      shiftY = SHIFTS[shift][1];
      shiftZ = SHIFTS[shift][2];
      shift++;
      GlStateManager.pushMatrix();
      GlStateManager.translatef(shiftX, shiftY, shiftZ);
      GlStateManager.rotatef(180.0f, 0.0f, 1.0f, 1.0f);
      GlStateManager.rotatef(timeDiff, 0F, 1F, 0F);
      GlStateManager.scalef(blockScale, blockScale, blockScale);
      customItem.setItem(stack);

      if (this.customRenderer == null) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRendererManager rendererManager = minecraft.getRenderManager();
        net.minecraft.client.renderer.ItemRenderer itemRenderer =
                minecraft.getItemRenderer();
        this.customRenderer = new ItemRenderer(rendererManager, itemRenderer) {

          @Override
          public int getModelCount(ItemStack stack) {

            return SignedBytes.saturatedCast(
                    Math.min(stack.getCount() / 32, 15) + 1);
          }

          @Override
          public boolean shouldBob() {

            return false;
          }

          @Override
          public boolean shouldSpreadItems() {

            return true;
          }
        };
      }

      this.customRenderer.doRender(customItem, 0D, 0D, 0D, 0F, partialTicks);
      GlStateManager.popMatrix();
    }

    GlStateManager.popMatrix();
  }
}