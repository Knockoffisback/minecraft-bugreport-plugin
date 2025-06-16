package com.yourname.bugreport.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import com.yourname.bugreport.BugReporterPlugin;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class MenuUtil implements Listener {
    private static final Map<UUID, Map<Integer, Consumer<InventoryClickEvent>>> clickHandlers = new HashMap<>();
    private static final Map<UUID, Inventory> openMenus = new HashMap<>();

    public static void initialize(BugReporterPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new MenuUtil(), plugin);
    }

    public static Inventory createMenu(Player player, String title, NamedTextColor color, int rows) {
        Inventory inventory = Bukkit.createInventory(player, rows * 9, Component.text(title).color(color));
        openMenus.put(player.getUniqueId(), inventory);
        clickHandlers.put(player.getUniqueId(), new HashMap<>());
        return inventory;
    }


    public static ItemStack createItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(name);

        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    // Convenience method for creating text with hex colors
    public static Component text(String content, String hexColor) {
        return Component.text(content, TextColor.fromHexString(hexColor));
    }

    public static void setItem(Player player, Inventory menu, int slot, ItemStack item, Consumer<InventoryClickEvent> clickHandler) {
        menu.setItem(slot, item);
        if (clickHandler != null) {
            clickHandlers.get(player.getUniqueId()).put(slot, clickHandler);
        }
    }

    public static void fillEmptySlots(Inventory menu, Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.text(" "));
        filler.setItemMeta(meta);

        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null || Objects.requireNonNull(menu.getItem(i)).getType() == Material.AIR) {
                menu.setItem(i, filler);
            }
        }
    }

    public static void openMenu(Player player, Inventory menu) {
        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        if (clickHandlers.containsKey(playerUUID)) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(openMenus.get(playerUUID))) {
                event.setCancelled(true);

                Consumer<InventoryClickEvent> handler = clickHandlers.get(playerUUID).get(event.getSlot());
                if (handler != null) {
                    handler.accept(event);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (openMenus.containsKey(playerUUID)) {
            if (event.getInventory().equals(openMenus.get(playerUUID))) {
                openMenus.remove(playerUUID);
                clickHandlers.remove(playerUUID);
            }
        }
    }
}