package fr.skytasul.quests.gui.quests;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.options.OptionStartable;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerListGUI implements CustomInventory {

	private Inventory inv;
	private Player open;
	private PlayerAccount acc;
	
	private int page = 0;
	private Category cat = Category.NONE;
	private List<Quest> quests;
	
	public PlayerListGUI(PlayerAccount acc){
		this.acc = acc;
	}

	public void setCategory(Category category) {
		this.cat = category;
	}
	
	public Inventory open(Player p) {
		setCategory(Category.IN_PROGRESS);
		open = p;
		inv = Bukkit.createInventory(null, (int) (Math.ceil((double) (getGUISize()) / 9d) * 9), Lang.INVENTORY_PLAYER_LIST.format(acc.getOfflinePlayer().getName()));

		page = 0;
		setQuests(QuestsAPI.getQuestsStarteds(acc));
		for (Quest quest : quests){
			ItemStack item;
			try {
				//int progress = (acc.getQuestDatas(quest).getStage() / quest.getBranchesManager().getPlayerBranch(acc).getRegularStages().size()) * 100;
				String desc = ChatColor.GRAY + ChatColor.stripColor(quest.getBranchesManager().getPlayerBranch(acc).getDescriptionLine(acc, Source.MENU));
				item = createQuestItem(quest, QuestsConfiguration.allowPlayerCancelQuest() && quest.isCancellable() ?
						new String[] { quest.getDescription() != null ? "§eBeschrijving§8: §7" + quest.getDescription() : "§eBeschrijving§8: §7Geen quest beschrijving beschikbaar.", null, "§7Objectief§8: §f" + desc, null, "§eLinker-klik §7om deze quest te beëindigen." }
						: new String[] { quest.getDescription() != null ? "§eBeschrijving§8: §7" + quest.getDescription() : "§eBeschrijving§8: §7Geen quest beschrijving beschikbaar.", null, "§7Objectief§8: §f" + desc });
			} catch (Exception ex) {
				item = ItemUtils.item(XMaterial.BARRIER, "§cError - Quest #" + quest.getID());
				BeautyQuests.getInstance().getLogger().severe("An error ocurred when creating item of quest " + quest.getID() + " for account " + acc.abstractAcc.getIdentifier());
				ex.printStackTrace();
			}
			inv.addItem(item);
		}

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	private void setQuests(List<Quest> quests) {
		this.quests = quests;
		quests.sort(null);
	}

	private int getGUISize() {
		switch (cat) {
			case FINISHED:
				return QuestsAPI.getQuestsFinished(acc).isEmpty() ? 9 : QuestsAPI.getQuestsFinished(acc).size();
			case IN_PROGRESS:
				return QuestsAPI.getQuestsStarteds(acc).isEmpty() ? 9 : QuestsAPI.getQuestsStarteds(acc).size();

			case NOT_STARTED:
				return QuestsAPI.getQuestsUnstarted(acc, true, true).isEmpty() ? 9 : QuestsAPI.getQuestsUnstarted(acc, true, true).size();
		}

		return 9;
	}
	
	private ItemStack createQuestItem(Quest qu, String... lore){
		return ItemUtils.item(qu.getQuestMaterial(), open.hasPermission("beautyquests.seeId") ? Lang.formatId.format(qu.getName(), qu.getID()) : Lang.formatNormal.format(qu.getName()), lore);
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		PlayerListGUI thiz = this;
		switch (slot % 9){
			case 8:
				int barSlot = (slot - 8) / 9;
				switch (barSlot){
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
						break;
				}
				break;

			case 7:
				break;

			default:
				if (QuestsConfiguration.allowPlayerCancelQuest() && cat == Category.IN_PROGRESS) {
					int id = (int) (slot - (Math.floor(slot * 1D / 9D)*2) + page*35);
					Quest qu = quests.get(id);
					if (!qu.isCancellable()) break;
					Inventories.create(p, new ConfirmGUI(() -> {
						qu.cancelPlayer(acc);
					}, () -> {
						p.openInventory(inv);
						Inventories.put(p, thiz, inv);
					}, Lang.INDICATION_CANCEL.format(qu.getName())));
				}else if (cat == Category.NOT_STARTED) {
					int id = (int) (slot - (Math.floor(slot * 1D / 9D) * 2) + page * 35);
					Quest qu = quests.get(id);
					if (!qu.getOptionValueOrDef(OptionStartable.class)) break;
					if (!acc.isCurrent()) break;
					Player target = acc.getPlayer();
					if (qu.isLauncheable(target, acc, true)) {
						p.closeInventory();
						qu.attemptStart(target);
					}
				}
				break;

		}
		return true;
	}
	
	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REMOVE;
	}

	enum Category{
		NONE, FINISHED, IN_PROGRESS, NOT_STARTED;
	}
	
}
