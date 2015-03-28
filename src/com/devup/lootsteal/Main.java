package com.devup.lootsteal;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if(!(e.getEntity() instanceof Player || e.getEntity().getKiller() instanceof Player))
			return;
		
		final Player killer = (Player) e.getEntity().getKiller();
		
		if(e.getDrops().isEmpty())
			return;
		
		for(ItemStack is : e.getDrops()) {
			Entity entity = e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), is);
			
			if(entity.hasMetadata("LootSteal"))
				entity.removeMetadata("LootSteal", this);
			
			entity.setMetadata("LootSteal", new FixedMetadataValue(this, killer.getUniqueId().toString() + " " + System.currentTimeMillis()));
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				killer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Your Loot Is No Longer Protected."));
			}
		}.runTaskLater(this, 20 * 10);
		
		e.getDrops().clear();
	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent e) {
		if(!(e.getItem().hasMetadata("LootSteal")))
			return;
		
		String value = e.getItem().getMetadata("LootSteal").get(0).asString();
		
		String[] values = value.split(" ");
		
		if(e.getPlayer().getUniqueId().toString().equals(values[0]))
			return;
		
		if(System.currentTimeMillis() - Long.valueOf(values[1]).longValue() >= (10 * 1000))
			return;
		
		e.setCancelled(true);
	}

}
