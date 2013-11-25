package net.choicecraft;


import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class IFPlayerListener implements Listener {
	private final ItemFilter plugin;
	public IFPlayerListener(ItemFilter main) {
		plugin = main;
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p  = e.getPlayer();
		if(p.hasPermission("itemfilter.exempt")) {
			return;
		}
		Inventory inven = p.getInventory();
		removeBannedItems(inven);
		replaceItems(inven, p.getName());
	}
	//Handles chest openings, but not player inventory openings
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onChestOpenEvent(InventoryOpenEvent e) {
		if(e.getPlayer().hasPermission("itemfilter.exempt")) {
			return;
		}
		Inventory inven = e.getInventory();
		removeBannedItems(inven);
		replaceItems(inven, e.getPlayer().getName());
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onInventoryInteract(InventoryClickEvent e) {
		if(e.getWhoClicked().hasPermission("itemfilter.exempt")) {
			return;
		}
		ItemStack slotItem = e.getCurrentItem();
		ItemStack cursorItem = e.getCursor();
		if(slotItem !=null && plugin.bannedItemIds.contains(slotItem.getTypeId())) {
			e.setCurrentItem(new ItemStack(0));
		}
		if(cursorItem != null && plugin.bannedItemIds.contains(cursorItem.getTypeId())) {
			e.setCursor(new ItemStack(0));
		}
		if(slotItem !=null && plugin.replaceItemIds.containsKey(slotItem.getTypeId())) {
			e.setCurrentItem(new ItemStack(plugin.replaceItemIds.get(slotItem.getTypeId()),slotItem.getAmount()));
			plugin.log.warning("Player: " + e.getWhoClicked().getName() + " Replacing: " + slotItem.getTypeId() + " (" + slotItem.getType() + ") x" + slotItem.getAmount() + " -> " + plugin.replaceItemIds.get(slotItem.getTypeId()));
		}
		if(cursorItem != null && plugin.replaceItemIds.containsKey(cursorItem.getTypeId())) {
			e.setCursor(new ItemStack(plugin.replaceItemIds.get(cursorItem.getTypeId()),cursorItem.getAmount()));
			plugin.log.warning("Player: " + e.getWhoClicked().getName() + " Replacing: " + cursorItem.getTypeId() + " (" + cursorItem.getType() + ") x" + cursorItem.getAmount() + " -> " + plugin.replaceItemIds.get(cursorItem.getTypeId()));
		}
	}
	private void removeBannedItems(Inventory inven) {
		for(Integer bannedID : plugin.bannedItemIds) {
			if(inven.contains(bannedID)) {
				inven.remove(bannedID);
			}
		}
		for(Integer bannedID : plugin.bannedItemIdsWithEnchants.keySet()) {
			if(inven.contains(bannedID)) {
				for(ItemStack item : inven.getContents()) {
					if(item.getTypeId()==bannedID) {
						Map<Enchantment, Integer> enchantments = item.getEnchantments();
						if(enchantments.containsKey(plugin.bannedItemIdsWithEnchants.get(bannedID))) {
							inven.remove(item);
						}
					}
				}
			}
		}
	}
	private void replaceItems(Inventory inven, String player) {
		for(Integer replaceID : plugin.replaceItemIds.keySet()) {
			if(inven.contains(replaceID)) {
				for(ItemStack item : inven.getContents()) {
					if(item != null) {
						if(item.getTypeId()==replaceID) {
							plugin.log.info("Replace: " + item.getTypeId() + " (" + item.getType() +") x" + item.getAmount() + " -> " + plugin.replaceItemIds.get(replaceID));
							item.setTypeId(plugin.replaceItemIds.get(replaceID));
						}
					}
				}
			}
		}
	}
}
