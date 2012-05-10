gOpenChest
==========

Minecraft Bukkit plugin for chests that absorb items.

With this plugin, every item that is on top of an chest will get put in the chest on to a appropriate stack.  
Some stacksizes are overruled, this is not configurable (yet).  
  
As an addition I've made the chests stackable, when you have several chests on top of eachother it will look if it fits anywhere in those chests.  
It will scan trough the stack if it can be placed on to an already existing itemstack before taking the first free spot.  

Configuration
-------------
settings.tickrate: Tick delay between checks. (default 5)  
settings.inventStackDepth: How many chests above eachother will be checked (default 0, unlimited)  

Stack depth
-------------
The stackdepth works from top to bottom as long as they are chests.  
First non-chest block will stop the searching down, or when the configuration limit is reached.  

Addition
-------------
Since dispensers work the same I've also made it support them.  
Furnaces however I did not.  