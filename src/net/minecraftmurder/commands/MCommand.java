package net.minecraftmurder.commands;

import org.bukkit.command.CommandSender;

public abstract class MCommand {
	private final String label;
	
	public MCommand(String label) {
		this.label = label;
	}
	
	public abstract MCommandResult exectute(CommandSender sender, String[] args);
	public abstract String getUsage();
	public abstract String getHelp();
	
	public String getLabel() {
		return label;
	}
	@Deprecated
	public String getCustomMessage() {
		return null;
	}
	@Deprecated
	public void setCustomMessage(String customMessage) {
		return;
	}
}