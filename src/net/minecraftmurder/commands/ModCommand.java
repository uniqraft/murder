package net.minecraftmurder.commands;

import net.minecraftmurder.commands.MCommandResult.Result;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.PlayerManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Toggles mod mode
 */
public class ModCommand extends MCommand {
	Murder plugin;
	
	public ModCommand(String label, Murder plugin) {
		super(label);
		
		this.plugin = plugin;
	}

	@Override
	public MCommandResult execute(CommandSender sender, String[] args) {
		MPlayer mod = PlayerManager.getMPlayer((Player) sender);
		if (!sender.hasPermission("murder.mod"))
			return new MCommandResult(this, Result.FAIL_PERMISSIONS);
		if (args.length != 0)
			return new MCommandResult(this, Result.FAIL_ARGUMENTS);
		
		mod.modMode = !mod.modMode;
		sender.sendMessage("Mod mode " + (mod.modMode ? (ChatColor.GREEN + "ON") : (ChatColor.RED + "OFF")));
		return new MCommandResult(this, Result.SUCCESS);
	}

	@Override
	public String getHelp() {
		return "Toggles mod mode.";
	}

	@Override
	public String getUsage() {
		return getLabel();
	}	
}
