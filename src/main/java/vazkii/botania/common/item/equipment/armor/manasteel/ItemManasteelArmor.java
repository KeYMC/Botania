/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 13, 2014, 6:38:21 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.armor.manasteel;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.items.IRunicArmor;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IPhantomInkable;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.client.model.armor.ModelArmorManasteel;
import vazkii.botania.common.core.BotaniaCreativeTab;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Optional.Interface(modid = "Thaumcraft", iface = "thaumcraft.api.items.IRunicArmor")
public class ItemManasteelArmor extends ItemArmor implements ISpecialArmor, IManaUsingItem, IPhantomInkable, IRunicArmor {

	private static final int MANA_PER_DAMAGE = 70;

	private static final String TAG_PHANTOM_INK = "phantomInk";

	protected Map<EntityEquipmentSlot, ModelBiped> models = null;
	public EntityEquipmentSlot type;

	public ItemManasteelArmor(EntityEquipmentSlot type, String name) {
		this(type, name, BotaniaAPI.manasteelArmorMaterial);
	}

	public ItemManasteelArmor(EntityEquipmentSlot type, String name, ArmorMaterial mat) {
		super(mat, 0, type);
		this.type = type;
		setCreativeTab(BotaniaCreativeTab.INSTANCE);
		GameRegistry.register(this, new ResourceLocation(LibMisc.MOD_ID, name));
		setUnlocalizedName(name);
	}

	@Nonnull
	@Override
	public String getUnlocalizedNameInefficiently(@Nonnull ItemStack par1ItemStack) {
		return super.getUnlocalizedNameInefficiently(par1ItemStack).replaceAll("item.", "item." + LibResources.PREFIX_MOD);
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
		if(source.isUnblockable())
			return new ArmorProperties(0, 0, 0);
		return new ArmorProperties(0, damageReduceAmount / 25D, armor.getMaxDamage() + 1 - armor.getItemDamage());
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		return damageReduceAmount;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity player, int par4, boolean par5) {
		if(player instanceof EntityPlayer)
			onArmorTick(world, (EntityPlayer) player, stack);
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		if(!world.isRemote && stack.getItemDamage() > 0 && ManaItemHandler.requestManaExact(stack, player, MANA_PER_DAMAGE * 2, true))
			stack.setItemDamage(stack.getItemDamage() - 1);
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		ToolCommons.damageItem(stack, damage, entity, MANA_PER_DAMAGE);
	}

	@Nonnull
	@Override
	public final String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return hasPhantomInk(stack) ? LibResources.MODEL_INVISIBLE_ARMOR : getArmorTextureAfterInk(stack, slot);
	}

	public String getArmorTextureAfterInk(ItemStack stack, EntityEquipmentSlot slot) {
		return ConfigHandler.enableArmorModels ? LibResources.MODEL_MANASTEEL_NEW : slot == EntityEquipmentSlot.LEGS ? LibResources.MODEL_MANASTEEL_1 : LibResources.MODEL_MANASTEEL_0;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped original) {
		if(ConfigHandler.enableArmorModels) {
			ModelBiped model = getArmorModelForSlot(entityLiving, itemStack, armorSlot);
			if(model == null)
				model = provideArmorModelForSlot(itemStack, armorSlot);

			if(model != null) {
				model.setModelAttributes(original);
				return model;
			}
		}

		return super.getArmorModel(entityLiving, itemStack, armorSlot, original);
	}

	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModelForSlot(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot) {
		if(models == null)
			models = new EnumMap<>(EntityEquipmentSlot.class);

		return models.get(slot);
	}

	@SideOnly(Side.CLIENT)
	public ModelBiped provideArmorModelForSlot(ItemStack stack, EntityEquipmentSlot slot) {
		models.put(slot, new ModelArmorManasteel(slot));
		return models.get(slot);
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
		return par2ItemStack.getItem() == ModItems.manaResource && par2ItemStack.getItemDamage() == 0 ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean adv) {
		if(GuiScreen.isShiftKeyDown())
			addInformationAfterShift(stack, player, list, adv);
		else addStringToTooltip(I18n.translateToLocal("botaniamisc.shiftinfo"), list);
	}

	public void addInformationAfterShift(ItemStack stack, EntityPlayer player, List<String> list, boolean adv) {
		addStringToTooltip(getArmorSetTitle(player), list);
		addArmorSetDescription(stack, list);
		ItemStack[] stacks = getArmorSetStacks();
		for(int i = 0; i < stacks.length; i++)
			addStringToTooltip((hasArmorSetItem(player, i) ? TextFormatting.GREEN : "") + " - " + stacks[i].getDisplayName(), list);
		if(hasPhantomInk(stack))
			addStringToTooltip(I18n.translateToLocal("botaniamisc.hasPhantomInk"), list);
	}
	
	public void addStringToTooltip(String s, List<String> tooltip) {
		tooltip.add(s.replaceAll("&", "\u00a7"));
	}

	static ItemStack[] armorset;

	public ItemStack[] getArmorSetStacks() {
		if(armorset == null)
			armorset = new ItemStack[] {
				new ItemStack(ModItems.manasteelHelm),
				new ItemStack(ModItems.manasteelChest),
				new ItemStack(ModItems.manasteelLegs),
				new ItemStack(ModItems.manasteelBoots)
		};

		return armorset;
	}

	public boolean hasArmorSet(EntityPlayer player) {
		return hasArmorSetItem(player, 0) && hasArmorSetItem(player, 1) && hasArmorSetItem(player, 2) && hasArmorSetItem(player, 3);
	}

	public boolean hasArmorSetItem(EntityPlayer player, int i) {
		ItemStack stack = player.inventory.armorInventory[3 - i];
		if(stack == null)
			return false;

		switch(i) {
		case 0: return stack.getItem() == ModItems.manasteelHelm || stack.getItem() == ModItems.manasteelHelmRevealing;
		case 1: return stack.getItem() == ModItems.manasteelChest;
		case 2: return stack.getItem() == ModItems.manasteelLegs;
		case 3: return stack.getItem() == ModItems.manasteelBoots;
		}

		return false;
	}

	public int getSetPiecesEquipped(EntityPlayer player) {
		int pieces = 0;
		for(int i = 0; i < 4; i++)
			if(hasArmorSetItem(player, i))
				pieces++;

		return pieces;
	}

	public String getArmorSetName() {
		return I18n.translateToLocal("botania.armorset.manasteel.name");
	}

	public String getArmorSetTitle(EntityPlayer player) {
		return I18n.translateToLocal("botaniamisc.armorset") + " " + getArmorSetName() + " (" + getSetPiecesEquipped(player) + "/" + getArmorSetStacks().length + ")";
	}

	public void addArmorSetDescription(ItemStack stack, List<String> list) {
		addStringToTooltip(I18n.translateToLocal("botania.armorset.manasteel.desc"), list);
	}

	@Override
	public boolean hasPhantomInk(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_PHANTOM_INK, false);
	}

	@Override
	public void setPhantomInk(ItemStack stack, boolean ink) {
		ItemNBTHelper.setBoolean(stack, TAG_PHANTOM_INK, ink);
	}

	@Override
	@Optional.Method(modid = "Thaumcraft")
	public int getRunicCharge(ItemStack itemstack) {
		return 0;
	}
}
