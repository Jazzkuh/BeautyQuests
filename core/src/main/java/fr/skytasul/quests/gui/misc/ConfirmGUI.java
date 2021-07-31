package fr.skytasul.quests.gui.misc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class ConfirmGUI implements CustomInventory {

	private Runnable yes, no;
	private String indication, lore;
	
	public ConfirmGUI(Runnable yes, Runnable no, String indication) {
		this(yes, no, indication, null);
	}
	
	public ConfirmGUI(Runnable yes, Runnable no, String indication, String lore) {
		this.yes = yes;
		this.no = no;
		this.indication = indication;
		this.lore = lore;
	}
	
	public Inventory open(Player p){
		Inventory inv = Bukkit.createInventory(null, 9 * 2, Lang.INVENTORY_CONFIRM.toString());
		
		inv.setItem(11, ItemUtils.item(XMaterial.LIME_DYE, Lang.confirmYes.toString()));
		inv.setItem(4, ItemUtils.item(XMaterial.BOOK, indication, lore == null ? null : new String[] { lore }));
		inv.setItem(15, ItemUtils.item(XMaterial.BARRIER, Lang.confirmNo.toString()));
		
		return p.openInventory(inv).getTopInventory();
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		Inventories.closeAndExit(p);
		if (slot == 11) {
			yes.run();
		}else if (slot == 15) {
			no.run();
		}
		return true;
	}
	
	public CloseBehavior onClose(Player p, Inventory inv){
		Inventories.closeAndExit(p);
		return CloseBehavior.NOTHING;
	}

}
