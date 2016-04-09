package de.seyfarth.sid;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	private Map<String, Event> events;
	private SpecialItemDropAdder listener;
	
	@Override
	public void onEnable () {
		super.onEnable();
		loadConfiguration();
		enableListener();
	}
	
	private void loadConfiguration () {
	    this.saveDefaultConfig();
	    events = new ConfigurationLoader(this).loadConfig();
	}
	
	private void reloadConfiguration () {
		this.reloadConfig();
		events = new ConfigurationLoader(this).loadConfig();
		listener.setEvents(events);
	}
	
	private void enableListener () {
		listener = new SpecialItemDropAdder(events);
		getServer().getPluginManager().registerEvents(listener, this);
	}
	
	@Override
	public void onDisable () {
		super.onDisable();
		disableListener();
		System.gc();
	}
	
	private void disableListener() {
		EntityDeathEvent.getHandlerList().unregister(listener);
		listener = null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("sidconfre")) {
			reloadConfiguration();
			sender.sendMessage("Config successfully reloaded.");
			return true;
		}
		return super.onCommand(sender, command, label, args);
	}
}
