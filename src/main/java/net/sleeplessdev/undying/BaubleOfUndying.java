package net.sleeplessdev.undying;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.render.IRenderBauble;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = BaubleOfUndying.ID,
     name = BaubleOfUndying.NAME,
     version = BaubleOfUndying.VERSION,
     dependencies = BaubleOfUndying.DEPENDENCIES)
@Mod.EventBusSubscriber(modid = BaubleOfUndying.ID)
public final class BaubleOfUndying {

    public static final String ID = "baubleofundying";
    public static final String NAME = "Bauble Of Undying";
    public static final String VERSION = "%VERSION%";
    public static final String DEPENDENCIES = "required-after:baubles";

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        ResourceLocation id = new ResourceLocation("minecraft", "totem_of_undying");
        ModContainer minecraft = Loader.instance().getMinecraftModContainer();
        ModContainer active = Loader.instance().activeModContainer();

        Item totem = new TotemBaubleItem();

        try {
            Loader.instance().setActiveModContainer(minecraft);
            totem = totem.setRegistryName(id);
        } finally {
            Loader.instance().setActiveModContainer(active);
        }

        event.getRegistry().register(totem);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().canHarmInCreative()) return;
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        for (EnumHand hand : EnumHand.values()) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return;
            }
        }

        int slot = BaublesApi.isBaubleEquipped(player, Items.TOTEM_OF_UNDYING);

        if (slot <= -1) return;

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) event.getEntityLiving();
            playerMP.addStat(StatList.getObjectUseStats(Items.TOTEM_OF_UNDYING));
            CriteriaTriggers.USED_TOTEM.trigger(playerMP, new ItemStack(Items.TOTEM_OF_UNDYING));
        }

        player.setHealth(1.0F);
        player.clearActivePotions();
        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 100, 1));
        player.world.setEntityState(event.getEntityLiving(), (byte) 35);
        BaublesApi.getBaublesHandler(player).extractItem(slot, 1, false);
        event.setCanceled(true);
    }

    public static final class TotemBaubleItem extends Item implements IBauble, IRenderBauble {
        {
            setUnlocalizedName("totem");
            setMaxStackSize(1);
            setCreativeTab(CreativeTabs.COMBAT);
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
            ItemStack stack = player.getHeldItem(hand);

            for (int slot : BaubleType.CHARM.getValidSlots()) {
                IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
                if (handler.getStackInSlot(slot).isEmpty()) {
                    player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
                    handler.insertItem(slot, stack.copy(), false);
                    stack.setCount(0);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            }

            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        @Override
        public BaubleType getBaubleType(ItemStack stack) {
            return BaubleType.CHARM;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void onPlayerBaubleRender(ItemStack stack, EntityPlayer player, RenderType type, float partialTicks) {
            if (type != RenderType.BODY) return;

            Minecraft mc = FMLClientHandler.instance().getClient();
            ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
            TransformType transform = TransformType.NONE;

            Helper.rotateIfSneaking(player);

            GlStateManager.scale(0.25, 0.25, 0.25);
            GlStateManager.rotate(180, 0, 0, 1);
            GlStateManager.translate(0.0, -0.5, hasChestplate(player) ? -0.8 : -0.5);

            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            mc.getRenderItem().renderItem(totem, transform);
        }

        private boolean hasChestplate(EntityPlayer player) {
            EntityEquipmentSlot chest = EntityEquipmentSlot.CHEST;
            return !player.getItemStackFromSlot(chest).isEmpty();
        }
    }

}
