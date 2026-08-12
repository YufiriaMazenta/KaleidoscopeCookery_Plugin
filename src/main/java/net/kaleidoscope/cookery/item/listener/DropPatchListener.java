package net.kaleidoscope.cookery.item.listener;

import net.kaleidoscope.cookery.api.ItemTags;
import net.kaleidoscope.cookery.item.ItemKeys;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 临时用于解决油、种子掉落问题的监听器，等待CE支持loot后删除
 */
public class DropPatchListener implements Listener {

    private final Set<EntityType> PIG_ENTITIES = Set.of(
        EntityType.PIG,
        EntityType.PIGLIN,
        EntityType.PIGLIN_BRUTE,
        EntityType.HOGLIN
    );
    private final Set<Material> GRASSES = Set.of(
        Material.SHORT_GRASS,
        Material.TALL_GRASS
    );
    private final List<Key> SEEDS = List.of(
        Key.of("kaleidoscopecookery", "wild_rice"),
        Key.of("kaleidoscopecookery", "tomato_seed"),
        Key.of("kaleidoscopecookery", "chili_seed"),
        Key.of("kaleidoscopecookery", "lettuce_seed"),
        Key.of("minecraft", "beetroot_seeds"),
        Key.of("minecraft", "melon_seeds"),
        Key.of("minecraft", "wheat_seeds"),
        Key.of("minecraft", "pumpkin_seeds")
    );
    private final Random RANDOM = new Random();

    @EventHandler
    public void onPigEntityDeath(EntityDeathEvent event) {
        LivingEntity deathEntity = event.getEntity();
        if (!PIG_ENTITIES.contains(deathEntity.getType())) {
            return;
        }
        Player killer = deathEntity.getKiller();
        if (killer == null) {
            return;
        }
        ItemStack itemInMainHand = killer.getInventory().getItemInMainHand();
        BukkitItemDefinition mainHandItemDef = CraftEngineItems.byItemStack(itemInMainHand);
        if (mainHandItemDef == null) {
            return;
        }
        boolean isKnife = ItemTags.instance().matches(
            Key.of("kaleidoscopecookery:kitchen_knife"),
            mainHandItemDef.buildItem(BukkitAdaptor.adapt(killer)));
        if (isKnife) {
            BukkitItemDefinition oilItem = CraftEngineItems.byId(ItemKeys.OIL);
            if (oilItem == null) {
                return;
            }
            ItemStack bukkitOilItem = oilItem.buildBukkitItem();
            bukkitOilItem.setAmount(RANDOM.nextInt(3));
            event.getDrops().add(bukkitOilItem);
        }
    }

    @EventHandler
    public void onBreakGrassDropSeed(BlockDropItemEvent event) {
        if (!GRASSES.contains(event.getBlock().getType())) {
            return;
        }
        List<Item> items = event.getItems();
        if (items.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType().equals(Material.AIR)) {
            return;
        }
        BukkitItemDefinition helmetItemDef = CraftEngineItems.byItemStack(helmet);
        if (helmetItemDef == null) {
            return;
        }
        Key strawHatId = Key.of("kaleidoscopecookery", "straw_hat");
        Key flowerStrawHatId = Key.of("kaleidoscopecookery", "straw_hat_flower");
        Key helmetId = helmetItemDef.id();
        if (!helmetId.equals(strawHatId) && !helmetId.equals(flowerStrawHatId)) {
            return;
        }
        for (Item item : items) {
            if (!item.getItemStack().getType().equals(Material.WHEAT_SEEDS)) {
                continue;
            }
            int seedIndex = RANDOM.nextInt(SEEDS.size());
            Key seedKey = SEEDS.get(seedIndex);
            BukkitItemDefinition seedItem = CraftEngineItems.byId(seedKey);
            if (seedItem == null) {
                Bukkit.getConsoleSender().sendMessage("§cCan not get seed by id: " + seedKey.asString());
                return;
            }
            item.setItemStack(seedItem.buildBukkitItem());
        }
    }

}
