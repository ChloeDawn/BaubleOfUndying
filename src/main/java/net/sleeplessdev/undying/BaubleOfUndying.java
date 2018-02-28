package net.sleeplessdev.undying;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

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

        Item totem = new TotemBaubleItem(ModConfig.baubleType);

        try {
            Loader.instance().setActiveModContainer(minecraft);
            totem = totem.setRegistryName(id);
        } finally {
            Loader.instance().setActiveModContainer(active);
        }

        event.getRegistry().register(totem);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().canHarmInCreative()) return;
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        for (EnumHand hand : EnumHand.values()) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) return;
        }

        int slot = BaublesApi.isBaubleEquipped(player, Items.TOTEM_OF_UNDYING);

        if (slot <= -1) return;

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) event.getEntityLiving();
            StatBase stat = StatList.getObjectUseStats(Items.TOTEM_OF_UNDYING);
            ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);

            playerMP.addStat(Objects.requireNonNull(stat));
            CriteriaTriggers.USED_TOTEM.trigger(playerMP, totem);
        }

        player.setHealth(1.0F);
        player.clearActivePotions();
        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 100, 1));
        player.world.setEntityState(event.getEntityLiving(), (byte) 35);
        BaublesApi.getBaublesHandler(player).extractItem(slot, 1, false);
        event.setCanceled(true);
    }

    @Config(modid = BaubleOfUndying.ID)
    @Mod.EventBusSubscriber(modid = BaubleOfUndying.ID)
    public static final class ModConfig {
        @Config.Name("bauble_type")
        @Config.Comment({"The type of bauble to assign to the Totem of Undying.",
                         "This will determine which slot it can be equipped into."})
        public static BaubleType baubleType = BaubleType.CHARM;

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (BaubleOfUndying.ID.equals(event.getModID())) {
                ConfigManager.sync(BaubleOfUndying.ID, Config.Type.INSTANCE);
            }
        }
    }

}
