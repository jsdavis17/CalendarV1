package dev.davisj.CalendarV1;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

public class RecurringEvent extends Event
{
	private String recurrencePattern;
	private int intervalValue;
	private String intervalUnit;

	public RecurringEvent(String title, String description, LocalDate date, LocalTime startTime, LocalTime endTime,
			boolean busy, String pattern, int value, String unit)
	{
		super(title, description, date, startTime, endTime, busy);
		this.recurrencePattern = pattern;
		this.intervalValue = value;
		this.intervalUnit = unit;
	}

	RecurringEvent(UUID id, String title, String description, LocalDate date, LocalTime startTime, LocalTime endTime,
			boolean busy, String pattern, int value, String unit)
	{
		super(id, title, description, date, startTime, endTime, busy);
		this.recurrencePattern = pattern;
		this.intervalValue = value;
		this.intervalUnit = unit;
	}

	public ArrayList<Event> generateOccurrences(LocalDate start, LocalDate end)
	{
		ArrayList<Event> occurrences = new ArrayList<>();
		LocalDate current = getDate();
		while (current.isBefore(start))
			current = advanceDate(current);
		while (!current.isAfter(end))
		{
			occurrences.add(new Event(getTitle(), getDescription(), current, getStartTime(), getEndTime(), isBusy()));
			current = advanceDate(current);
		}
		return occurrences;
	}

	private LocalDate advanceDate(LocalDate date)
	{
		switch (intervalUnit.toUpperCase())
		{
		case "DAYS":
			return date.plusDays(intervalValue);
		case "WEEKS":
			return date.plusWeeks(intervalValue);
		case "MONTHS":
			return date.plusMonths(intervalValue);
		case "YEARS":
			return date.plusYears(intervalValue);
		default:
			return date.plusDays(intervalValue);
		}
	}

	@Override
	public String display()
	{
		return String.format("[RECURRING] %s | Every %d %s starting %s | %s - %s%n  %s", getTitle(), intervalValue,
				intervalUnit, getDate(), getStartTime(), getEndTime(), getDescription());
	}

	@Override
	public boolean matches(String field, String val)
	{
		if (field.equalsIgnoreCase("title"))
			return getTitle().toLowerCase().contains(val.toLowerCase());
		if (field.equalsIgnoreCase("type"))
			return val.equalsIgnoreCase("recurring");
		return false;
	}

	@Override
	public String toRecord()
	{
		return String.format(
				"RECURRING|id=%s|title=%s|description=%s|date=%s|start=%s|end=%s|busy=%b"
						+ "|pattern=%s|value=%d|unit=%s",
				getId(), URLEncoder.encode(getTitle(), StandardCharsets.UTF_8),
				URLEncoder.encode(getDescription(), StandardCharsets.UTF_8), getDate(), getStartTime(), getEndTime(),
				isBusy(), URLEncoder.encode(recurrencePattern, StandardCharsets.UTF_8), intervalValue,
				URLEncoder.encode(intervalUnit, StandardCharsets.UTF_8));
	}

	// Getters
	public String getRecurrencePattern()
	{
		return recurrencePattern;
	}

	public int getIntervalValue()
	{
		return intervalValue;
	}

	public String getIntervalUnit()
	{
		return intervalUnit;
	}
}
