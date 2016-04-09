package de.seyfarth.sid;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import de.seyfarth.util.time.Span;

public class ConfigurationLoader {
	
	private final JavaPlugin plugin;
	private final Configuration config;
	
	public ConfigurationLoader(JavaPlugin plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();
	}
	
	public Map<String, Event> loadConfig () {
		Map<String, Event> events = new HashMap<>(); 
		Set<String> keys = config.getValues(false).keySet();
        keys.stream().forEach((key) -> {
            Event event = loadEvent(key);
            if (event != null) {
                events.put(key, event);
            }
        });
		return Collections.unmodifiableMap(events);
	}
	
	private Event loadEvent (String eventName) {
		plugin.getLogger().log(Level.CONFIG, "Loading event ''{0}''...", eventName);
		
		Supplier<ItemStack> item = loadItem(eventName);
		Predicate<EntityType> typeChecker = loadEntityFilter(eventName);
		Predicate<Collection<PotionEffectType>> effectChecker = loadEffectFilter(eventName);
		Predicate<String> worldChecker = loadWorldFilter(eventName);
		Span span = loadSpan(eventName);
		int rarity = loadRarity(eventName);
		
		if (item == null) return null;
		
		Event event = new Event(item);
		event.setTypeChecker(typeChecker);
		event.setEffectChecker(effectChecker);
		event.setWorldChecker(worldChecker);
		event.setSpan(span);
		event.setRarity(rarity);
		return event;
	}
	
	private Supplier<ItemStack> loadItem (String event) {
		plugin.getLogger().config("item:");
		Material material = loadMaterial(event);
		int[] amount = loadAmount(event);
		ItemSupplier item = new ItemSupplier(material, amount[0], amount[1]);
		
		byte data = loadData(event);
		if (data != 0) item.setData(data); 
		
		String name = loadName(event);
		if (!name.isEmpty()) item.setName(name);
		
		List<String> lore = loadLore(event);
		if (!lore.isEmpty()) item.setLore(lore);

		return item;
	}
	
	private Material loadMaterial (String event) {
		String materialName = config.getString(event + ".item.type");
		Material material = Material.matchMaterial(materialName);
		if (material == null) {
			plugin.getLogger().log(Level.SEVERE, "Could not load material type from event ''{0}''.", event);
			return null;
		}
		plugin.getLogger().log(Level.CONFIG, "type: {0}", material.toString());
		return material;
	}
	
	private int[] loadAmount (String event) {
		int min = config.getInt(event + ".item.amount.min", 1);
		int max = config.getInt(event + ".item.amount.max", 1);
		
		if (max < min) {
			plugin.getLogger().log(Level.WARNING, "The max amount is smaller than the min amount of the item from event ''{0}''.", event);
			int tmp = min;
			min = max;
			max = tmp;
		}
		
		if (min < 0) {
			plugin.getLogger().log(Level.WARNING, "Can not have negative amount of the item from event ''{0}''.", event);
			min = 0;
			if (max < 0) {
				plugin.getLogger().log(Level.WARNING, "Due to negative maximum amount, the item will not drop from event ''{0}''.", event);
				max = 0;
			}
		}
		
		plugin.getLogger().log(Level.CONFIG, "minAmount: {0}", min);
		plugin.getLogger().log(Level.CONFIG, "maxAmount: {0}", max);
		
		return new int[] {min, max};
	}
	
	private byte loadData (String event) {
		int dataInt = config.getInt(event + ".item.data");
		byte data = (byte) dataInt;
        if (dataInt != data) {
            plugin.getLogger().log(Level.WARNING, "Data of item from event ''{0}'' overflows byte with value ''{1}'', set smaller value!", new Object[]{event, dataInt});
            data = 0;
        }
		plugin.getLogger().log(Level.CONFIG, "data: {0}", data);
		return data;
	}
	
	private String loadName (String event) {
		String name = config.getString(event + ".item.name", "");
		plugin.getLogger().log(Level.CONFIG, "name: {0}", name);
		return name;
	}
	
	private List<String> loadLore (String event) {
		List<String> lore = config.getStringList(event + ".item.lore");
		while(lore.contains("")) {
            lore.remove("");
        }
        lore.stream().forEach(line -> plugin.getLogger().log(Level.CONFIG, "lore: {0}", line));
		return lore;
	}
	
	private Predicate<EntityType> loadEntityFilter (String event) {
		plugin.getLogger().config("entityies:");
		Set<EntityType> mobTypes = loadEntityTypes(event);
		if (mobTypes.isEmpty()) return type -> true;
		boolean isWhitelist = loadWhitelist(event, "mobs");
		plugin.getLogger().log(Level.CONFIG, "whitelist: {0}", isWhitelist);
		return type -> isWhitelist ? mobTypes.contains(type) : !mobTypes.contains(type);
	}
	
	private Set<EntityType> loadEntityTypes (String event) {
		List<String> typeStrings = config.getStringList(event + ".mobs.list");
		Set<EntityType> types = new HashSet<>();
		
        typeStrings.stream()
            .map(typeString -> convertEntityType(event, typeString))
            .filter(type -> (type != null))
            .peek(type -> plugin.getLogger().log(Level.CONFIG, "mobs: {0}", type.toString()))
            .forEach(type -> types.add(type));
		types.remove(null);
		return types;
	}
	
	private EntityType convertEntityType (String event, String typeString) {
		if (typeString == null) return null;
		EntityType type = EntityType.valueOf(typeString);
		if (type == null) {
            plugin.getLogger().log(Level.WARNING, "EntityType {0} is not valid in event ''{1}''.", new Object[]{typeString, event});
        }
		return type;
	}
	
	private Predicate<Collection<PotionEffectType>> loadEffectFilter (String event) {
		plugin.getLogger().config("effect:");
		Set<PotionEffectType> effects = loadPotionEffects(event);
		if (effects.isEmpty()) return eff -> true;
		boolean noEffects = loadNoEffects(event);
		plugin.getLogger().log(Level.CONFIG, "noEffects: {0}", noEffects);
		boolean isWhitelist = loadWhitelist(event, "effects");
		plugin.getLogger().log(Level.CONFIG, "whitelist: {0}", isWhitelist);
		return eff -> {
			if (eff.isEmpty()) return noEffects;
			return isWhitelist ? eff.containsAll(effects) : eff.stream().noneMatch(effect -> effects.contains(effect));
			};
	}
	
	private Set<PotionEffectType> loadPotionEffects (String event) {
		List<String> typeStrings = config.getStringList(event + ".effects.list");
		Set<PotionEffectType> types = new HashSet<>();
		
        typeStrings.stream()
            .map(typeString -> convertPotionEffectType(event, typeString))
            .filter(type -> type != null)
            .peek(type -> plugin.getLogger().log(Level.CONFIG, "effects: {0}", type.toString()))
            .forEach(type -> types.add(type));
		types.remove(null);
		return types;
	}

	private PotionEffectType convertPotionEffectType (String event, String typeString) {
		if (typeString == null) return null;
		PotionEffectType type = PotionEffectType.getByName(typeString);
		if (type == null) {
            plugin.getLogger().log(Level.WARNING, "PotionEffectType {0} is not valid in event ''{1}''.", new Object[]{typeString, event});
        }
		return type;
	}

	private boolean loadNoEffects (String event) {
		Object bool = config.get(event + ".effects.noeffects");
		if (bool == null) {
			plugin.getLogger().log(Level.WARNING, "Noeffects should be set from event ''{0}''.", event);
		} else if (bool instanceof Boolean) {
			return (Boolean) bool;
		} else {
			plugin.getLogger().log(Level.WARNING, "Noeffects must be a boolean value from event ''{0}''.", event);
		}
		return true;
	}
	
	private Predicate<String> loadWorldFilter (String event) {
		plugin.getLogger().config("world:");
		Set<String> worlds = new HashSet<>(config.getStringList(event + ".worlds.list"));
		if (worlds.isEmpty()) return world -> true;
		boolean isWhitelist = loadWhitelist(event, "worlds");
		plugin.getLogger().log(Level.CONFIG, "whitelist: {0}", isWhitelist);
		return world -> isWhitelist ? worlds.contains(world) : !worlds.contains(world);
	}

	private Span loadSpan (String event) {
		plugin.getLogger().config("span:");
		LocalDate from = convertDate(event, config.getString(event + ".date.from"), LocalDate.MIN);
		LocalDate to = convertDate(event, config.getString(event + ".date.to"), LocalDate.MAX);
			
		plugin.getLogger().log(Level.CONFIG, "from: {0}", from.toString());
		plugin.getLogger().log(Level.CONFIG, "to: {0}", to.toString());
			
		return new Span(from, to);
	}
	
	private LocalDate convertDate (String event, String date, LocalDate defaultDate) {
		if (date == null) {
			return defaultDate;
		} else {
			try {
				return LocalDate.parse(date);
			} catch (DateTimeParseException ex) {
				plugin.getLogger().log(Level.SEVERE, "Could not parse geven string to date from event '" + event + "'. Are you using single quotes?", ex);
				return defaultDate;
			}
		}
	}
	
	private int loadRarity (String event) {
		int rarity = config.getInt(event + ".rarity", 1);
		plugin.getLogger().log(Level.CONFIG, "rarity: {0}", rarity);
		if (rarity < 1) {
			plugin.getLogger().log(Level.WARNING, "Rarity must not be negative from event ''{0}''.", event);
			return 1;
		}
		return rarity;
	}
	
	private boolean loadWhitelist (String event, String category) {
		Object isWhitelist = config.get(event + "." + category + ".whitelist");
		if (isWhitelist == null) {
			plugin.getLogger().log(Level.WARNING, "Whitelist must be set if filter-list exists as it is in category {0} from event ''{1}''.", new Object[]{category, event});
		} else if (isWhitelist instanceof Boolean) {
			return (Boolean) isWhitelist;
		} else {
			plugin.getLogger().log(Level.WARNING, "Whitelist must be a boolean value in category {0} from event ''{1}''.", new Object[]{category, event});
		}
		return true;
	}
}
