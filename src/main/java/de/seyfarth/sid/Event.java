package de.seyfarth.sid;

import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import de.seyfarth.util.time.Span;

public class Event {
	
	private final Supplier<ItemStack> item;
	private Predicate<EntityType> typeChecker;
	private Predicate<Collection<PotionEffectType>> effectChecker;
	private Predicate<String> worldChecker;
	private Span span;
	private int rarity;
	
	public Event(Supplier<ItemStack> item) {
		if (item == null) throw new IllegalArgumentException("item must not be null");
		this.item = item;
		
		typeChecker = type -> true;
		effectChecker = effect -> true;
		worldChecker = world -> true;
		
		span = Span.anytime();
		rarity = 1;
	}
	
	public void setTypeChecker (Predicate<EntityType> typeChecker) {
		if (typeChecker == null) {
			this.typeChecker = type -> true;
		} else {
			this.typeChecker = typeChecker;
		}
	}

	public void setEffectChecker (Predicate<Collection<PotionEffectType>> effectChecker) {
		if (effectChecker == null) {
			this.effectChecker = effect -> true;
		} else {
			this.effectChecker = effectChecker;
		}
	}

	public void setWorldChecker (Predicate<String> worldChecker) {
		if (worldChecker == null) {
			this.worldChecker = world -> true;
		} else {
			this.worldChecker = worldChecker;
		}
	}

	public void setSpan (Span span) {
		if (span == null) {
			this.span = Span.anytime();
		} else {
			this.span = span;
		}
	}
	
	public void setRarity (int rarity) {
		if (rarity > 0) {
			this.rarity = rarity;
		} else {
			this.rarity = 1;
		}
	}
	
	public ItemStack getItem () {
		return item.get();
	}
	
	public int getRarity () {
		return rarity;
	}
	
	public boolean isInWorld (String worldName) {
		return worldChecker.test(worldName);
	}
	
	public boolean isRightType (EntityType type) {
		// TODO Add to other class [return types.contains(type);]
		return typeChecker.test(type);
	}
	
	public boolean isRightEffect (Collection<PotionEffectType> effects) {
		return effectChecker.test(effects);
	}
	
	public boolean isActive (LocalDate when) {
		return span == null || span.isWhile(when);
	}
}
