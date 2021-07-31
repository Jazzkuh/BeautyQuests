package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;

public class DependenciesManager {

	public static boolean wg = false; //		WorldGuard
	public static boolean mm = false; //	MythicMobs
	public static boolean vault = false; //	Vault
	public static boolean papi = false; //	PlaceholderAPI
	public static boolean holod = false; //	HolographicDisplays
	
	public static void testCompatibilities() {
		wg = testCompatibility("WorldGuard");
		mm = testCompatibility("MythicMobs");
		vault = testCompatibility("Vault");
		papi = testCompatibility("PlaceholderAPI");
		holod = testCompatibility("HolographicDisplays");
	}

	public static void initializeCompatibilities() {
		if (mm) QuestsAPI.registerMobFactory(new MythicMobs());
		if (papi) QuestsPlaceholders.registerPlaceholders();
		if (holod) QuestsAPI.setHologramsManager(new BQHolographicDisplays());
	}
	
	private static boolean testCompatibility(String pluginName) {
		if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) return false;
		BeautyQuests.logger.info("Hooked into " + pluginName);
		return true;
	}
	
}
