package com.egroen.bukkit.gopenchest;

import java.util.EnumMap;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
public class GOpenChest extends JavaPlugin
{
    
    private final static EnumMap<Material, Short> stackSizeOverrule = new EnumMap<Material, Short>(Material.class);
    private static FileConfiguration config;
    
    static {
        stackSizeOverrule.put(Material.SIGN, 		(short)64);
        stackSizeOverrule.put(Material.SIGN_POST, 	(short)64);
        stackSizeOverrule.put(Material.BOAT, 		(short)64);
        stackSizeOverrule.put(Material.MINECART, 	(short)64);
        stackSizeOverrule.put(Material.IRON_DOOR, 	(short)64);
        stackSizeOverrule.put(Material.WOOD_DOOR,	(short) 64);
        for (int i=2256; i<=2266; i++)              // All music plates
            stackSizeOverrule.put(Material.getMaterial(i), (short)32);
    }
    
    @Override
    public void onDisable() { }

    @Override
    public void onEnable()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();
        config = getConfig();
        long Tickrate = config.getLong("settings.tickrate");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            @Override
            public void run()
            {
                for (World w : Bukkit.getWorlds()) {
                    for (Entity e : w.getEntitiesByClasses(Item.class)) {
                        processItem((Item)e);
                        //if (((Item)e).getItemStack().getAmount() == 0) e.remove();
                    } // for Entity
                } // For world
            }
        }
        , Tickrate, Tickrate);
    }
    
    private void processItem(Item e) {
    	Block iBlock = e.getLocation().getBlock();
    	short stackDepth = (short)config.getInt("settings.inventStackDepth");
    	
    	ItemStack itemStack = e.getItemStack();
    	// Determine max stacksize -> overrule ? <overrule value> : <default value>;
    	int maxStackSize = stackSizeOverrule.containsKey(itemStack.getType())
    						? stackSizeOverrule.get(itemStack.getType())
							: itemStack.getType().getMaxStackSize();
    	
    	// Check stack for existing items throughout the stack
    	for (short depth=0; stackDepth==0||depth<stackDepth; depth++) {
    		Block cBlock = iBlock.getRelative(0, -depth-1, 0);											// -depth-1, -1 because we are checking from entity block
    		if (cBlock.getType() != Material.CHEST && cBlock.getType() != Material.DISPENSER) break;	// End of stack
    		
    		Inventory invent = ((InventoryHolder)cBlock.getState()).getInventory();						// Get block inventory
    		HashMap<Integer, ? extends ItemStack> similars = invent.all(itemStack.getType());			// Get all same-type stacks
    		for (ItemStack is : similars.values()) {													// Loop trough all similar stacks
    			if (itemStack.getData().getData() != is.getData().getData()) continue;						// Different data value, continue to next stack
    			
    			if (is.getAmount() >= maxStackSize) continue;												// Full stack, continue to next stack
    			int putAmount = lowest(maxStackSize - is.getAmount(), itemStack.getAmount());				// Determine lowest, max 'open spaces' or items left.
    			is.setAmount(is.getAmount()+putAmount);														// Increase invent itemstack
    			itemStack.setAmount(itemStack.getAmount()-putAmount);										// Decrease floating itemstack
    		}
    		if (itemStack.getAmount() == 0)	 { e.remove(); return; }									// Nothing in stack, we can quit!
    	}
    	
    	// When we come here, we have no similar stacks, but still some over, going to put them at the first free spots.
    	for (short depth=0; stackDepth==0||depth<stackDepth; depth++) {
    		Block cBlock = iBlock.getRelative(0, -depth-1, 0);											// -depth-1, -1 because we are checking from entity block
    		if (cBlock.getType() != Material.CHEST && cBlock.getType() != Material.DISPENSER) break;	// End of stack
    		
    		Inventory invent = ((InventoryHolder)cBlock.getState()).getInventory();						// Get the block inventory
    		
    		int firstFree;
			while ((firstFree = invent.firstEmpty()) != -1) {											// Loop trough firstFree until -1 returned or amount is 0
    			int putAmount = lowest(maxStackSize, itemStack.getAmount());							// Determine lowest
    			ItemStack newStack = itemStack.clone();													// Create clone, prevents item-data loss.
    			newStack.setAmount(putAmount);															// Set proper amount on the clone
    			itemStack.setAmount(itemStack.getAmount()-putAmount);									// Decrease amount of the original
    			invent.setItem(firstFree, newStack);													// Place the clone in the inventory
    			if (itemStack.getAmount() == 0)	 { e.remove(); return; }								// No items left, do not continue to next free space
    		}
    		if (itemStack.getAmount() == 0)	 { e.remove(); return; }									// No items left, do not continue to next chest.
    	}
    }
    
//    private void processItem(Entity e) {
//        Block iBlock = e.getWorld().getBlockAt(e.getLocation().add(0D, -1D, 0D));
//        int stackDepth = config.getInt("settings.inventStackDepth");
//        
//        for (int depth=0; stackDepth==0||depth<stackDepth; depth++) {
//            if (iBlock.getType() != Material.CHEST
//                    && iBlock.getType() != Material.DISPENSER) return;        // DAMN, not a chest/dispenser :(
//
//            //InventoryHolder block = (InventoryHolder)underMe.getState();
//            Inventory invent = ((InventoryHolder)iBlock.getState()).getInventory();
//            Item item = (Item)e;
//            ItemStack itemStack = item.getItemStack();
//
//            addItem(invent, itemStack);
//            // If nothing left, stop
//            if (itemStack.getAmount() == 0) {
//                e.remove();
//                break;
//            }
//            
//            // Set ready for next block
//            iBlock = iBlock.getRelative(BlockFace.DOWN);
//        }
//    }
//    
//    private void addItem(Inventory invent, ItemStack itemstack) {
//        
//        // Get how far we may stack the items
//        int maxStackSize = stackSizeOverrule.containsKey(itemstack.getType())
//                ? stackSizeOverrule.get(itemstack.getType())
//                : itemstack.getType().getMaxStackSize();
//        
//        // Get all current holdings of the same item
//        HashMap<Integer, ? extends ItemStack> similars = invent.all(itemstack.getType());
//        
//        // Loop trough them all, checking if theres stack-space left
//        for (ItemStack is : similars.values()) {
//            if (itemstack.getData().getData() != is.getData().getData()) continue;  // Different data value, can not use
//            
//            // Can we put anymore here?
//            if (is.getAmount() < maxStackSize) {
//                // Determin how many to fit in, lowest will be used
//                int putAmount = lowest(maxStackSize - is.getAmount(), itemstack.getAmount());
//
//                // Update holder
//                is.setAmount(is.getAmount()+putAmount);
//                // Update original
//                itemstack.setAmount(itemstack.getAmount()-putAmount);
//            }
//            if (itemstack.getAmount() == 0) return;             // We don't have to continue if we don't have some left..
//        }
//        
//        while (itemstack.getAmount() > 0) {
//            // Apparently we where not able to fit them onto existing stacks..
//            int firstFree = invent.firstEmpty();
//            if (firstFree == -1) return;                            // No free space..
//
//            // Determin max to place
//            int putAmount = lowest(maxStackSize, itemstack.getAmount());
//            ItemStack tmp = itemstack.clone();                      // Create a clone of the itemstack
//            tmp.setAmount(putAmount);                               // So we keep all attached data
//            itemstack.setAmount(itemstack.getAmount()-putAmount);   // Change the original
//            invent.setItem(firstFree, tmp);                         // Put the new one in the inventory
//        }
//    }

    private static int lowest(int... integers) {
        int lowest = integers[0];
        for (int i : integers) {
            if (i < lowest) lowest = i;
        }
        return lowest;
    }
}
