package de.seyfarth.util;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

/**
 * Converts a given Collection of Identifiers into the specified Type using a Function.
 * 
 * 
 * 
 * @param <I> The Identifier to match for.
 * @param <T> The Type to convert to.
 */
public class AbsoluteConverter<I, T> {
	
	private final Collection<I> identifier;
	private final Function<I, T> conversation;
	
	private final Set<T> converted = new CopyOnWriteArraySet<>();
	
	private boolean isConverted;
	private final Object converting = new Object();

	public AbsoluteConverter(Collection<I> identifier, Function<I, T> conversation) {
		if (identifier == null || conversation == null)
			throw new NullPointerException();
		
		setConverted(false);
		this.identifier = identifier;
		this.conversation = conversation;
	}
	
	public final synchronized boolean isConverted() {
		return isConverted;
	}
	
	private synchronized void setConverted(boolean converted) {
		this.isConverted = converted;
	}
	
	private synchronized void checkConverted() {
		if (!isConverted) throw new IllegalStateException("The Identifiers are not yet converted.");
	}
	
	public Set<T> getResults() {
		checkConverted();
		return converted;
	}
	
	/**
	 *
	 * @return 
	 */
	public final AbsoluteConverter<I, T> convert() {
		synchronized(converting) {
			prepareConversion();
			startConversion();
			finishConversion();
		}
		return this;
	}
	
	private void prepareConversion() {
		setConverted(false);
		converted.clear();
	}

	private void startConversion() {
		identifier.forEach((item) -> convertIdentifier(item));
	}
	
	private void finishConversion() {
		setConverted(true);
	}
	
	private void convertIdentifier(I item) {
		T result = conversation.apply(item);
		converted.add(result);
	}
}