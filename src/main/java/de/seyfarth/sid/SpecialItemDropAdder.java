package de.seyfarth.sid;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.seyfarth.util.AbsoluteConverter;

public class SpecialItemDropAdder implements Listener {
	
	private static final Random RND = new Random();
	private final Map<String, Event> events;
	
	public SpecialItemDropAdder (Map<String, Event> events) {
		if (events == null) throw new NullPointerException("Events to listen to can't be null!");
		this.events = new HashMap<>(events);
	}
	
	public void setEvents (Map<String, Event> events) {
		if (events == null) throw new NullPointerException("Events to listen to can't be null!");
		this.events.clear();
		this.events.putAll(events);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onEntityDeath (EntityDeathEvent deathEvent) {
        events.values().stream()
            .forEach(event -> addPossibleEffects(event, deathEvent.getEntity(), deathEvent.getDrops()));
	}
	
	private void addPossibleEffects (Event event, LivingEntity entity, List<ItemStack> drops) {
		if (isPossibleEvent(event, entity)) {
			drops.add(event.getItem());
		}
	}
	
	private boolean isPossibleEvent (Event event, LivingEntity entity) {
		boolean type = isPossibleEntityType(event, entity);
		boolean effect = isPossiblePotionEffect(event, entity);
		boolean world = isPossibleWorld(event, entity);
		boolean date = isPossibleDate(event);
		boolean random = isRandomChoosen(event);
		return type && effect && world && date && random;
	}

	private boolean isPossibleEntityType (Event event, LivingEntity entity) {
		EntityType type = entity.getType();
		return event.isRightType(type);
	}
	
	private boolean isPossiblePotionEffect (Event event, LivingEntity entity) {
		Collection<PotionEffect> effects = entity.getActivePotionEffects();
		AbsoluteConverter<PotionEffect, PotionEffectType> convert = new AbsoluteConverter<>(effects, effect -> effect.getType()).convert();
		return event.isRightEffect(convert.getResults());
	}
	
	private boolean isPossibleWorld (Event event, LivingEntity entity) {
		String worldName = entity.getWorld().getName();
		return event.isInWorld(worldName);
	}
	
	private boolean isPossibleDate (Event event) {
		LocalDate now = LocalDate.now();
		return event.isActive(now);
	}
	
	private boolean isRandomChoosen(Event event) {
		int rarity = event.getRarity();
		int random = RND.nextInt(rarity);
		return random == 0;
	}
}
