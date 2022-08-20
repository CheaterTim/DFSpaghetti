package me.wonk2.utilities;

//import eu.endercentral.crazy_advancements.JSONMessage;
//import eu.endercentral.crazy_advancements.NameKey;
//import eu.endercentral.crazy_advancements.advancement.Advancement;
//import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
//import eu.endercentral.crazy_advancements.advancement.AdvancementVisibility;
//import eu.endercentral.crazy_advancements.advancement.criteria.Criteria;
//import eu.endercentral.crazy_advancements.manager.AdvancementManager;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.wonk2.utilities.internals.EntityData;
import me.wonk2.utilities.internals.FileManager;
import me.wonk2.utilities.internals.PlayerData;
import me.wonk2.utilities.values.DFSound;
import me.wonk2.utilities.values.DFValue;
import me.wonk2.utilities.values.DFVar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.codehaus.plexus.util.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

// Class to store general-use methods and implement event handlers to assist other action implementations
public class DFUtilities implements Listener {
	public static FileManager playerConfig;
	public static FileManager varConfig;
	
	public static boolean inCustomInv(Player p){
		Inventory inv = p.getOpenInventory().getTopInventory();
		return inv.getType() != InventoryType.PLAYER
			&& inv.getType() != InventoryType.CRAFTING
			&& inv.getLocation() == null;
	}
	
	public static boolean locationEquals(Location loc, Location loc2, boolean ignoreRotation) {
		return loc.getX() == loc2.getX() && loc.getY() == loc2.getY() && loc.getZ() == loc2.getZ() && (ignoreRotation || loc.getYaw() == loc2.getYaw() && loc.getPitch() == loc2.getPitch());
	}
	
	public static double clampNum(double num, double min, double max){
		if(num > max) num = max;
		else if (num < min) num = min;
		
		return num;
	}
	
	public static double wrapNum(double num, double min, double max){
		if(num > max) num = min;
		else if (num < min) num = max;
		
		return num;
	}
	
	public static String parseTxt(DFValue val){
		switch(val.type){
			case LIST: {
				DFValue[] elements = (DFValue[]) val.getVal();
				String[] result = new String[elements.length];
				for(int i = 0; i < elements.length; i++) result[i] = parseTxt(elements[i]);
				return "[" +  String.join(", ", result) + "]";
			}
			case ITEM: {
				return ((ItemStack) val.getVal()).getType().name().toLowerCase();
			}
			case POT: {
				PotionEffect effect = (PotionEffect) val.getVal();
				String name = StringUtils.capitalise(effect.getType().getName().replace('_', ' '));
				
				return name + " " + effect.getAmplifier() + " - " + formatTime("mm:s", effect.getDuration()/20d);
			}
			case LOC: {
				//Round location values to last 2 decimal places
				DecimalFormat df = new DecimalFormat("#.##");
				
				Location loc = (Location) val.getVal();
				loc.setX(Double.parseDouble(df.format(loc.getX())));
				loc.setY(Double.parseDouble(df.format(loc.getY())));
				loc.setZ(Double.parseDouble(df.format(loc.getZ())));
				loc.setYaw(Float.parseFloat(df.format(loc.getYaw())));
				loc.setPitch(Float.parseFloat(df.format(loc.getPitch())));
				
				if(loc.getYaw() == 0f && loc.getPitch() == 0f)
					return "[" +
						(Math.floor(loc.getX()) == loc.getX() ? String.valueOf((int) loc.getX()) : loc.getX()) + ", " +
						(Math.floor(loc.getY()) == loc.getY() ? String.valueOf((int) loc.getY()) : loc.getY()) + ", " +
						(Math.floor(loc.getZ()) == loc.getZ() ? String.valueOf((int) loc.getZ()) : loc.getZ()) + "]";
				else return "[" +
					(Math.floor(loc.getX()) == loc.getX() ? String.valueOf((int) loc.getX()) : loc.getX()) + ", " +
					(Math.floor(loc.getY()) == loc.getY() ? String.valueOf((int) loc.getY()) : loc.getY()) + ", " +
					(Math.floor(loc.getZ()) == loc.getZ() ? String.valueOf((int) loc.getZ()) : loc.getZ())+ ", " +
					(Math.floor(loc.getPitch()) == loc.getPitch() ? String.valueOf((int) loc.getPitch()) : loc.getPitch()) + ", " +
					(Math.floor(loc.getYaw()) == loc.getYaw() ? String.valueOf((int) loc.getYaw()) : loc.getYaw()) + "]";
			}
			case SND: {
				DFSound sound = (DFSound) val.getVal();
				return sound.getName() + "[" + sound.pitch + "]" + "[" + sound.volume + "]";
			}
			case NUM: {
				if(Math.round((double) val.getVal()) == val.getInt()) return val.getInt().toString();
				else return String.valueOf((double) val.getVal());
			}
			default: {
				return String.valueOf(val.getVal());
			}
		}
	}
	
	public static String[] parseTxt(DFValue[] vals){
		String[] result = new String[vals.length];
		for(int i = 0; i < vals.length; i++){
			result[i] = parseTxt(vals[i]);
		}
		
		return result;
	}
	
	public static String formatTime(String format, double seconds){
		long timestamp = (long) seconds * 1000;
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(new Date(timestamp));
	}
	
	public static String escapeRegex(String input){
		return input.replaceAll("[-.+*?\\[^\\]$(){}=!<>|:\\\\]", "\\\\$0");
	}
	
	public static HashMap<String, DFValue> getArgs(Object obj){
		return (HashMap<String, DFValue>) obj;
	}
	
	public static HashMap<String, String> getTags(Object obj){
		return (HashMap<String, String>) obj;
	}
	
	public static ItemStack parseItemNBT(String rawNBT){
		if(rawNBT == "null") return null;
		CompoundTag nbt = null;
		try{nbt = TagParser.parseTag(rawNBT);}
		catch(CommandSyntaxException ignored){}
		
		net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.of(nbt);
		return CraftItemStack.asBukkitCopy(nmsItem);
	}
	
	public static boolean locIsNear(World world, Location checkLoc, Location loc, double radius, String shape){
		switch(shape){
			case "Sphere": {
				if(Math.sqrt(Math.pow(loc.getX() - checkLoc.getX(), 2) + Math.pow(loc.getY() - checkLoc.getY(), 2) + Math.pow(loc.getZ() - checkLoc.getZ(), 2)) <= radius) return true;
				break;
			}
			case "Circle": {
				if(Math.sqrt(Math.pow(loc.getX() - checkLoc.getX(), 2) + Math.pow(loc.getZ() - checkLoc.getZ(), 2)) <= radius) return true;
				break;
			}
			case "Cube": {
				if(Math.abs(loc.getX() - checkLoc.getX()) <= radius) return true;
				if(Math.abs(loc.getY() - checkLoc.getY()) <= radius) return true;
				if(Math.abs(loc.getZ() - checkLoc.getZ()) <= radius) return true;
				break;
			}
			case "Square": {
				if(Math.abs(loc.getX() - checkLoc.getX()) <= radius) return true;
				if(Math.abs(loc.getZ() - checkLoc.getZ()) <= radius) return true;
				break;
			}
		}
		
		return false;
	}
	
	@EventHandler
	public static void PlayerLeave(PlayerQuitEvent event){
		if(Bukkit.getOnlinePlayers().size() == 1) DFVar.globalVars = new HashMap<>(); // Purge all global vars when all players leave
	}
	
	@EventHandler
	public static void ClickMenuSlot(InventoryClickEvent event){
		if(inCustomInv((Player) event.getView().getPlayer())) event.setCancelled(true); // ClickSlot event triggered from inside custom GUI
	}
	
	@EventHandler
	public static void PlayerDmgPlayer(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player){
			if(!PlayerData.getPlayerData(((Player) event.getEntity()).getUniqueId()).canPvP)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public static void PlaceBlock(BlockPlaceEvent event){
		PlayerData playerData = PlayerData.getPlayerData(event.getPlayer().getUniqueId());
		//if(!playerData.allowedBlocks.contains(event.getBlockPlaced().getType())) event.setCancelled(true);
	}
	
	@EventHandler
	public static void BreakBlock(BlockBreakEvent event){
		PlayerData playerData = PlayerData.getPlayerData(event.getPlayer().getUniqueId());
		//if(!playerData.allowedBlocks.contains(event.getBlock().getType())) event.setCancelled(true);
	}
	
	@EventHandler
	public static void Death(PlayerDeathEvent event){
		PlayerData playerData = PlayerData.getPlayerData(((Player) event.getEntity()).getUniqueId());
		if(!playerData.deathDrops) event.getDrops().clear();
		if(playerData.keepInv) event.setKeepInventory(true);
		if(playerData.instantRespawn) ((Player) event.getEntity()).spigot().respawn();
	}
	
	@EventHandler
	public static void Explode(EntityExplodeEvent event){
		event.setCancelled(true);
		Entity entity = event.getEntity();
		
		entity.getWorld().createExplosion(entity.getLocation(), EntityData.getEntityData(entity.getUniqueId()).tntPower);
	}
	
	public static boolean playerDidJump(PlayerMoveEvent e){ //TODO: Find a better alternative, this check is unreliable.
		Player player = e.getPlayer();
		PlayerData playerData = PlayerData.getPlayerData(player.getUniqueId());
		if (player.getVelocity().getY() > 0) {
			double jumpVelocity = 0.42F;
			if (player.hasPotionEffect(PotionEffectType.JUMP))
				jumpVelocity += (float) (player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1) * 0.1F;
			
			if (e.getPlayer().getLocation().getBlock().getType() != Material.LADDER && playerData.wasGrounded)
				if (!((LivingEntity) player).isOnGround() && Double.compare(player.getVelocity().getY(), jumpVelocity) == 0)
					return true;
		}
		
		playerData.wasGrounded = ((LivingEntity) player).isOnGround();
		return false;
	}
	
	public static boolean cloudAffectedPlayer(List<LivingEntity> entities){
		for(LivingEntity entity : entities) if(entity instanceof Player) return true;
		return false;
	}
	
	public static void getManagers(JavaPlugin plugin){
		playerConfig = new FileManager(plugin, "playerData.yml");
		varConfig = new FileManager(plugin, "varData.yml");
	}
	
}