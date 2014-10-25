package lumien.randomthings.Items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import lumien.randomthings.RandomThings;
import lumien.randomthings.Library.ClientUtil;
import lumien.randomthings.Library.GuiIds;
import lumien.randomthings.Library.ItemUtils;
import lumien.randomthings.Library.Texts;
import lumien.randomthings.Library.Interfaces.IItemWithProperties;
import lumien.randomthings.Library.Inventorys.InventoryItemFilter;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemFilter extends ItemBase implements IItemWithProperties
{
	public enum FilterType
	{
		BLOCK, ITEM, ENTITY;
	}

	public static ItemStack standardItemFilter;

	IIcon[] icons;

	public ItemFilter()
	{
		super("filter");
		this.setHasSubtypes(true);

		icons = new IIcon[3];

		standardItemFilter = new ItemStack(this, 1, 1);
		standardItemFilter.stackTagCompound = new NBTTagCompound();
		standardItemFilter.stackTagCompound.setBoolean("oreDict", false);
		standardItemFilter.stackTagCompound.setBoolean("metadata", true);
	}

	public static boolean matchesBlock(ItemStack filter, Block block, int metadata)
	{
		String blockName = Block.blockRegistry.getNameForObject(block);

		if (filter.stackTagCompound == null || !filter.stackTagCompound.hasKey("block"))
		{
			return true;
		}

		String filterName = filter.stackTagCompound.getString("block");
		int filterMetadata = filter.stackTagCompound.getInteger("metadata");

		return filterName.equals(blockName) && filterMetadata == metadata;
	}

	public static boolean matchesItem(ItemStack filter, ItemStack toCheck)
	{
		if (filter == null || toCheck == null || filter.stackTagCompound == null)
		{
			return false;
		}

		if (filter.getItem() == null && !(filter.getItem() instanceof ItemFilter) || filter.getItemDamage() != 1)
		{
			return false;
		}

		// Update Check
		if (!filter.stackTagCompound.hasKey("metadata"))
		{
			filter.stackTagCompound.setBoolean("metadata", true);
		}

		boolean metadata = filter.stackTagCompound.getBoolean("metadata");
		boolean oreDict = filter.stackTagCompound.getBoolean("oreDict");
		int listType = filter.stackTagCompound.getInteger("listType");

		IInventory filterInventory = getItemFilterInv(null, filter);
		filterInventory.openInventory();
		for (int slot = 0; slot < filterInventory.getSizeInventory(); slot++)
		{
			ItemStack is = filterInventory.getStackInSlot(slot);
			if (is != null)
			{
				if (oreDict && ItemUtils.areOreDictionaried(is, toCheck))
				{
					return listType == 0 ? true : false;
				}
				if (metadata ? is.isItemEqual(toCheck) : isItemEqualIgnoreMetadata(is, toCheck))
				{
					return listType == 0 ? true : false;
				}
			}
		}

		return listType == 0 ? false : true;
	}

	private static boolean isItemEqualIgnoreMetadata(ItemStack is1, ItemStack is2)
	{
		return is1.getItem() == is2.getItem();
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		if (par1ItemStack.stackTagCompound != null)
		{
			if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				par3List.add(Texts.PSHIFT);
			}
			else
			{
				switch (par1ItemStack.getItemDamage())
				{
					case 0:
						String block = par1ItemStack.stackTagCompound.getString("block");
						if (!block.equals(""))
						{
							par3List.add(I18n.format("text.miscellaneous.block", (Object[]) null) + ": " + block);
							par3List.add(I18n.format("text.miscellaneous.metadata", (Object[]) null) + ": " + par1ItemStack.stackTagCompound.getInteger("metadata"));
						}
						break;
					case 1:
						if (!(ItemFilter.getItemFilterInv(par2EntityPlayer, par1ItemStack) == null))
						{
							String yes = I18n.format("text.miscellaneous.yes");
							String no = I18n.format("text.miscellaneous.no");
							String whiteList = I18n.format("text.miscellaneous.whiteList");
							String blackList = I18n.format("text.miscellaneous.blackList");

							par3List.add(ClientUtil.translate("text.miscellaneous.metadata") + ": " + (par1ItemStack.stackTagCompound.getBoolean("metadata") ? yes : no));
							par3List.add(I18n.format("text.miscellaneous.oreDictionary", (Object[]) null) + ": " + (par1ItemStack.stackTagCompound.getBoolean("oreDict") ? yes : no));
							par3List.add(I18n.format("text.miscellaneous.listType", (Object[]) null) + ": " + (par1ItemStack.stackTagCompound.getInteger("listType") == 1 ? blackList : whiteList));
							IInventory inventoryFilter = new InventoryItemFilter(par2EntityPlayer, par1ItemStack);
							inventoryFilter.openInventory();
							if (inventoryFilter != null)
							{
								for (int i = 0; i < 9; i++)
								{
									ItemStack isg = inventoryFilter.getStackInSlot(i);
									if (isg != null)
									{
										par3List.add("- " + isg.getDisplayName());
									}
								}
							}
						}
						break;
					case 2:
						int entityID = par1ItemStack.stackTagCompound.getInteger("entity");
						String entityName = par1ItemStack.stackTagCompound.getString("entityName");
						if (!(entityID == 0))
						{
							par3List.add(I18n.format("text.miscellaneous.entityid", (Object[]) null) + ": " + entityID);
							par3List.add(I18n.format("text.miscellaneous.entityName", (Object[]) null) + ": " + entityName);
						}
						break;
				}
			}
		}
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
	{
		if (par1ItemStack.stackTagCompound == null)
		{
			par1ItemStack.stackTagCompound = new NBTTagCompound();
		}
		switch (par1ItemStack.getItemDamage())
		{
			case 0:
				Block b = par3World.getBlock(par4, par5, par6);
				par1ItemStack.stackTagCompound.setString("block", Block.blockRegistry.getNameForObject(b));
				par1ItemStack.stackTagCompound.setInteger("metadata", par3World.getBlockMetadata(par4, par5, par6));
				return true;
		}
		return false;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity)
	{
		if (itemstack.getItemDamage() == 2)
		{
			ItemStack realItemStack = player.getCurrentEquippedItem();
			if (realItemStack.stackTagCompound == null)
			{
				realItemStack.stackTagCompound = new NBTTagCompound();
			}

			realItemStack.stackTagCompound.setInteger("entity", entity.getEntityId());
			realItemStack.stackTagCompound.setString("entityName", entity.getCommandSenderName());
			return true;
		}
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		if (itemStack.stackTagCompound == null || !itemStack.stackTagCompound.hasKey("oreDict"))
		{
			itemStack.stackTagCompound = new NBTTagCompound();
			if (itemStack.getItemDamage() == 1)
			{
				itemStack.stackTagCompound.setBoolean("oreDict", false);
				itemStack.stackTagCompound.setBoolean("metadata", true);
			}
		}
		if (!par2World.isRemote)
		{
			if (itemStack.getItemDamage() == 1)
			{
				if (!itemStack.stackTagCompound.hasKey("metadata"))
				{
					itemStack.stackTagCompound.setBoolean("metadata", true);
				}
				par3EntityPlayer.openGui(RandomThings.instance, GuiIds.ITEM_FILTER, par2World, (int) par3EntityPlayer.posX, (int) par3EntityPlayer.posY, (int) par3EntityPlayer.posZ);
			}
		}
		return itemStack;
	}

	@Override
	public IIcon getIconFromDamage(int par1)
	{
		if (par1 > 0 && par1 < icons.length)
		{
			return icons[par1];
		}
		else
		{
			return icons[0];
		}
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		icons[0] = par1IconRegister.registerIcon("RandomThings:filter/blockFilter");
		icons[1] = par1IconRegister.registerIcon("RandomThings:filter/itemFilter");
		icons[2] = par1IconRegister.registerIcon("RandomThings:filter/entityFilter");
	}

	public static FilterType getFilterType(int damage)
	{
		switch (damage)
		{
			case 0:
				return FilterType.BLOCK;
			case 1:
				return FilterType.ITEM;
			case 2:
				return FilterType.ENTITY;
			default:
				return FilterType.BLOCK;
		}
	}

	public static IInventory getItemFilterInv(EntityPlayer player)
	{
		ItemStack filter;
		IInventory inventoryFilter = null;
		filter = player.getCurrentEquippedItem();

		if (filter != null && filter.getItem() instanceof ItemFilter && filter.getItemDamage() == 1)
		{
			inventoryFilter = new InventoryItemFilter(player, filter);
		}

		return inventoryFilter;
	}

	public static IInventory getItemFilterInv(EntityPlayer player, ItemStack filter)
	{
		IInventory inventoryFilter = null;

		if (filter != null && filter.getItem() instanceof ItemFilter && filter.getItemDamage() == 1)
		{
			inventoryFilter = new InventoryItemFilter(player, filter);
		}

		return inventoryFilter;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack)
	{
		String type = "";
		switch (par1ItemStack.getItemDamage())
		{
			case 0: // Block Filter
				type = "filterBlock";
				break;
			case 1: // Item Filter
				type = "filterItem";
				break;
			case 2: // Entity Filter
				type = "filterEntity";
				break;
		}
		return "item." + type;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list)
	{
		ItemStack blockFilter = new ItemStack(item, 1, 0); // Block Filter
		blockFilter.stackTagCompound = new NBTTagCompound();
		list.add(blockFilter);

		list.add(standardItemFilter);

		ItemStack entityFilter = new ItemStack(item, 1, 2); // Entity Filter
		entityFilter.stackTagCompound = new NBTTagCompound();
		list.add(entityFilter);
	}

	@Override
	public boolean isValidAttribute(ItemStack is, String attributeName, int attributeType)
	{
		switch (is.getItemDamage())
		{
			case 1:
				return (attributeName.equals("oreDict") || attributeName.equals("metadata")) && attributeType == 0;
		}
		return false;
	}
}
