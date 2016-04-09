package de.seyfarth.sid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemSupplier implements Supplier<ItemStack> {
	
	private static final Random RND = new Random();
	
	private Material type;
	private int min;
	private int max;
	
	private byte data = 0;
	private String name = "";
	private final List<String> lore = new ArrayList<>();

	public ItemSupplier (Material type, int min, int max) {
		setType(type);
		setAmount(min, max);
	}
	
	public void setType(Material type) {
		if (type == null) throw new NullPointerException("Material type must not be null.");
		this.type = type;
	}
	
	public void setAmount(int min, int max) {
		if (min < 0 || max < 0)
			throw new IllegalArgumentException("Can not have negative amount!");
		if (min < max) {
			this.min = min;
			this.max = max;
		} else {
			this.min = max;
			this.max = min;
		}
	}
	
	public void setData(byte data) {
		this.data = data;
	}
	
	public void setName(String name) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = "";
		}
	}
	
	public void setLore(Collection<String> lore) {
		this.lore.clear();
		if (lore != null) this.lore.addAll(lore);
	}
	
	@Override
	public ItemStack get() {
		return constructItem();
	}
	
	private ItemStack constructItem() {
		ItemStack item = new ItemStack(type);
		constructAmount(item);
		constructData(item);
		constructName(item);
		constructLore(item);
		return item;
	}

	private void constructAmount(ItemStack item) {
		item.setAmount(min + RND.nextInt((max - min) + 1));
	}
	
	private void constructData (ItemStack item) {
		if (data != 0) {
			item.setDurability(data);
		}
	}
	
	private void constructName (ItemStack item) {
		if (!name.isEmpty()) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta(meta);
		}
	}
	
	private void constructLore (ItemStack item) {
		if (!lore.isEmpty()) {
			ItemMeta meta = item.getItemMeta();
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
	}
}
