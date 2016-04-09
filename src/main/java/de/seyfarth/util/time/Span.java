package de.seyfarth.util.time;

import java.time.LocalDate;
import java.time.Period;

public class Span {
	
	private final LocalDate from;
	private final LocalDate to;
	
	public Span(LocalDate startDate, LocalDate endDate) {
		if (startDate.isBefore(endDate)) {
			this.from = startDate;
			this.to = endDate;
		} else {
			this.from = endDate;
			this.to = startDate;
		}
	}
	
	public static Span anytime() {
		return new Span(LocalDate.MIN, LocalDate.MAX);
	}
	
	public LocalDate getStartDate () {
		return from;
	}
	
	public LocalDate getEndDate () {
		return to;
	}
	
	public boolean isBefore(LocalDate date) {
		return date.isBefore(from);
	}
	
	public boolean isWhile(LocalDate date) {
		return !(date.isBefore(from) || date.isAfter(to));
	}
	
	public boolean isAfter(LocalDate date) {
		return date.isAfter(to);
	}
	
	public Period getPeriod() {
		return Period.between(from, to.plusDays(1)).normalized();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(29);
		builder.append("from ").append(from.toString()).append(" to ").append(to.toString());
		return builder.toString();
	}
}
