package net.sleeplessdev.undying;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.render.IRenderBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class TotemBaubleItem extends Item implements IBauble, IRenderBauble {

    private final BaubleType type;

    protected TotemBaubleItem(BaubleType type) {
        this.type = type;
        setUnlocalizedName("totem");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        ItemStack stack = player.getHeldItem(hand);

        for (int slot : type.getValidSlots()) {
            ItemStack remainder = handler.insertItem(slot, stack.copy(), true);
            if (remainder.getCount() < stack.getCount()) {
                player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
                handler.insertItem(slot, stack.copy(), false);
                stack.setCount(remainder.getCount());
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }

        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return type;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType type, float partialTicks) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
        boolean chest = !player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty();
        switch (this.type) { // TODO Rendering states
            case HEAD:
                if (type != RenderType.HEAD) return;
                break;
            case AMULET:
            case RING:
                if (type != RenderType.BODY) return;
                break;
            case BELT:
                if (type != RenderType.BODY) return;
                break;
            case TRINKET:
            case BODY:
            case CHARM:
                if (type != RenderType.BODY) return;
                IRenderBauble.Helper.rotateIfSneaking(player);
                GlStateManager.scale(0.25, 0.25, 0.25);
                GlStateManager.rotate(180, 0, 0, 1);
                GlStateManager.translate(0.0, -0.5, chest ? -0.8 : -0.5);
                mc.getRenderItem().renderItem(totem, ItemCameraTransforms.TransformType.NONE);
                break;
        }
    }

}
