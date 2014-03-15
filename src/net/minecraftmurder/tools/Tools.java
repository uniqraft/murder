package net.minecraftmurder.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Tools {
	
	public static boolean compareBlockLocation (Location loc1, Location loc2) {
		return (loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ());
	}
	
	public static String dateToString (Date date, String seperator) {
		String s = "";
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		// Date(year, month, date, hrs, min, sec)
		s += calendar.get(Calendar.YEAR) 		+ seperator;
		s += calendar.get(Calendar.MONTH) 		+ seperator;
		s += calendar.get(Calendar.DATE) 		+ seperator;
		s += calendar.get(Calendar.HOUR_OF_DAY) + seperator;
		s += calendar.get(Calendar.MINUTE) 		+ seperator;
		s += calendar.get(Calendar.SECOND) 		+ "";
		return s;
	}
	
	public static Date stringToDate (String date, String seperator) {
		String[] split = date.split(seperator);
		int[] splitDate = new int[split.length];
		if (split.length != 6) return null;
		GregorianCalendar calendar = new GregorianCalendar();
		try {
			for (int i = 0; i < 6; i++) {
				splitDate[i] = Integer.parseInt(split[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		calendar.set(splitDate[0], splitDate[1], splitDate[2], splitDate[3], splitDate[4], splitDate[5]);
		return calendar.getTime();
	}
	
	public static String locationToString (Location loc) {
		String locString = "";
		locString += loc.getWorld().getName() + " ";
		locString += loc.getX() + " ";
		locString += loc.getY() + " ";
		locString += loc.getZ() + " ";
		locString += loc.getYaw() + " ";
		locString += loc.getPitch() + " ";
		return locString;
	}
	
	public static Location stringToLocation (String loc) {
		try {
			String[] split = loc.split(" ");
			if (Bukkit.getWorld(split[0]) == null) {
				throw new NullPointerException("Tried using world that doesn't exist.");
			}
			World world = Bukkit.getWorld(split[0]);
			double x = Double.parseDouble(split[1]);
			double y = Double.parseDouble(split[2]);
			double z = Double.parseDouble(split[3]);
			float yaw = Float.parseFloat(split[4]);
			float pitch = Float.parseFloat(split[5]);
			
			return new Location(world, x, y, z, yaw, pitch);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void sendMessageAll (String message) {
		sendMessageAll(message, new ArrayList<Player>());
	}
	public static void sendMessageAll (String message, Player ignore) {
		sendMessageAll(message, Arrays.asList(ignore));
	}
	public static void sendMessageAll (String message, List<Player> ignore) {
		for (Player player: Bukkit.getOnlinePlayers()) {
			if (!ignore.contains(player))
				player.sendMessage(message);
		}
	}
	
	public static ItemStack setItemStackName (ItemStack itemStack, String name) {
		return setItemStackName(itemStack, name, null);
	}
	public static ItemStack setItemStackName (ItemStack itemStack, List<String> lore) {
		return setItemStackName(itemStack, null, lore);
	}
	public static ItemStack setItemStackName (ItemStack itemStack, String name, List<String> lore) {
		ItemMeta meta = itemStack.getItemMeta();
		if (meta != null)
			meta.setDisplayName(name);
		if (lore != null)
			meta.setLore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	public static ItemStack addGlow(ItemStack item) { 
		  net.minecraft.server.v1_7_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		  NBTTagCompound tag = null;
		  if (!nmsStack.hasTag()) {
		      tag = new NBTTagCompound();
		      nmsStack.setTag(tag);
		  }
		  if (tag == null) tag = nmsStack.getTag();
		  NBTTagList ench = new NBTTagList();
		  tag.set("ench", ench);
		  nmsStack.setTag(tag);
		  return CraftItemStack.asCraftMirror(nmsStack);
	}
	
	public static boolean array2DContains (Object[][] array, Object object) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				if (array[i][j].equals(object)) {
					return true;
				}
			}
		}
		return false;
	}
}
