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
public class MatchingConverter<I, T> {
	
	private final Collection<I> identifier;
	private final Function<I, Collection<T>> conversation;
	
	private final Set<T> converted = new CopyOnWriteArraySet<>();
	private final Set<I> multipleMatches = new CopyOnWriteArraySet<>();
	private final Set<I> nothingMatched = new CopyOnWriteArraySet<>();
	
	private boolean isConverted;
	private final Object converting = new Object();

	public MatchingConverter(Collection<I> identifier, Function<I, Collection<T>> conversation) {
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
	
	public Set<I> getMultipeMatched() {
		checkConverted();
		return multipleMatches;
	}
	
	public Set<I> getNotMatched() {
		checkConverted();
		return multipleMatches;
	}
	
	/**
	 *
	 * @return 
	 */
	public final MatchingConverter<I, T> convert() {
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
		multipleMatches.clear();
		nothingMatched.clear();
		
		prepareHook();
	}
	
	protected void prepareHook() {}

	private void startConversion() {
		identifier.forEach((item) -> convertIdentifier(item));
		
		conversionHook();
	}
	
	protected void conversionHook() {}
	
	private void finishConversion() {
		setConverted(true);
	}
	
	private void convertIdentifier(I item) {
		Collection<T> result = conversation.apply(item);
		handleSpecialCases(item, result);
		result.addAll(result);
	}
	
	private void handleSpecialCases(I item, Collection<T> result) {
		if (result.isEmpty()) {
			nothingMatched.add(item);
		}
		if (result.size() > 1) {
			multipleMatches.add(item);
		}
	}
}