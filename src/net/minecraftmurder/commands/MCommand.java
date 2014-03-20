package net.minecraftmurder.commands;

import org.bukkit.command.CommandSender;

public abstract class MCommand {
	private String customMessage;
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
	public String getCustomMessage() {
		return customMessage;
	}
	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}
}