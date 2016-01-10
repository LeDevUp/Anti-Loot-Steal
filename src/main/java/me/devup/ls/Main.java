package me.devup.ls;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    protected boolean usesDamage = false;

    protected HashMap<UUID, Damagers> damagers = new HashMap<UUID, Damagers>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        usesDamage = getConfig().getBoolean("DamageCheck");

        getServer().getPluginManager().registerEvents(this, this);

        if(usesDamage) {
            new BukkitRunnable() {
                public void run() {
                    for(UUID player : damagers.keySet()) {
                        if(damagers.get(player).elapsed())
                            damagers.remove(player);
                    }
                }
            }.runTaskTimer(this, 20, 20);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if(!(e.getEntity() instanceof Player || e.getEntity().getKiller() instanceof Player))
            return;

        Player player = e.getEntity();

        if(e.getDrops().isEmpty())
            return;

        if(damagers.containsKey(player.getUniqueId())) {
            final Player killer = Bukkit.getPlayer(damagers.get(player.getUniqueId()).getTopDamager());

            for(ItemStack is : e.getDrops()) {
                Entity entity = e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), is);

                if(entity.hasMetadata("LootSteal"))
                    entity.removeMetadata("LootSteal", this);

                entity.setMetadata("LootSteal", new FixedMetadataValue(this, killer.getUniqueId().toString() + " " + System.currentTimeMillis()));
            }

            new BukkitRunnable() {
                public void run() {
                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Your Loot Is No Longer Protected."));
                }
            }.runTaskLater(this, 20 * 10);
        } else {
            final Player killer = (Player) e.getEntity().getKiller();

            for (ItemStack is : e.getDrops()) {
                Entity entity = e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), is);

                if (entity.hasMetadata("LootSteal"))
                    entity.removeMetadata("LootSteal", this);

                entity.setMetadata("LootSteal", new FixedMetadataValue(this, killer.getUniqueId().toString() + " " + System.currentTimeMillis()));
            }

            new BukkitRunnable() {
                public void run() {
                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Your Loot Is No Longer Protected."));
                }
            }.runTaskLater(this, 20 * 10);
        }
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(!(usesDamage))
            return;

        if(!(event.getEntity() instanceof Player))
            return;

        if(event.getDamage() == 0)
            return;

        Player player = (Player) event.getEntity(), damager = null;

        if(event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if(arrow.getShooter() instanceof Player)
                damager = (Player) arrow.getShooter();
            else
                return;
        } else if(event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();

            if(snowball.getShooter() instanceof Player)
                damager = (Player) snowball.getShooter();
            else
                return;
        } else if(event.getDamager() instanceof Egg) {
            Egg egg = (Egg) event.getDamager();

            if(egg.getShooter() instanceof Player)
                damager = (Player) egg.getShooter();
            else
                return;
        } else if(event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else {
            return;
        }

        if(!(damagers.containsKey(player.getUniqueId())))
            damagers.put(player.getUniqueId(), new Damagers());

        damagers.get(player.getUniqueId()).addDamage(damager, (int) event.getDamage());
    }

}
