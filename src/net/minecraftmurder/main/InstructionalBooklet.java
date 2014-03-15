package net.minecraftmurder.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * Only an implementation, I HAVE NOT actually given it to the player when they
 * join. This is a bit rough around the edges, but HOPEFULLY it works. Crossing
 * my fingers here, seriously.
 * 
 * @author picklemagnet
 * @since late at night, 15th of March
 */
public class InstructionalBooklet extends ItemStack {
	public static final String TITLE = "Rules & Instructions";
	public static final String AUTHOR = "Staff";

	// Everything happens in this constructor, because this is haphazardly
	// thrown together.
	public InstructionalBooklet(Murder plugin) {
		super(Material.WRITTEN_BOOK);

		List<String> pages = new ArrayList<String>();
		// If we want to change the instructional booklet WITHOUT uploading a
		// new plugin, this line can't point to defaults.
		for (String instructions : delimit(plugin.getConfig().getDefaults()
				.getString("instructions"), 256)) {
			pages.add(ChatColor.translateAlternateColorCodes('$', instructions));
		}

		BookMeta meta = (BookMeta) getItemMeta();
		meta.setAuthor(AUTHOR);
		meta.setTitle(TITLE);
		meta.setPages(pages);
		// Lore?
		setItemMeta(meta);
	}

	/**
	 * I don't even know if this works. I literally copy pasted this. CROSSING
	 * MY FINGERS THOUGH. Maybe remove chunk. Whatever. I'm going to bed.
	 */
	public static String[] delimit(String str, int chunk) {
		int size = (int) Math.ceil((double) str.length() / chunk);
		String[] arr = new String[size];
		int dex = 0;
		for (int i = 0; i < str.length(); i = i + chunk) {
			if (str.length() - i < chunk) {
				arr[dex++] = str.substring(i);
			} else {
				arr[dex++] = str.substring(i, i + chunk);
			}
		}
		return arr;
	}
}
