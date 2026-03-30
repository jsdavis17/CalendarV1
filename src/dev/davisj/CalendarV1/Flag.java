package dev.davisj.CalendarV1;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

public class Flag extends Entry
{
	private LocalDate date;

	public Flag(String title, String description, LocalDate date)
	{
		super(title, description);
		this.date = date;
	}

	Flag(UUID id, String title, String description, LocalDate date)
	{
		super(id, title, description);
		this.date = date;
	}

	@Override
	public String display()
	{
		return String.format("[FLAG] %s on %s%n  %s", getTitle(), date, getDescription());
	}

	@Override
	public boolean matches(String field, String val)
	{
		if (field.equalsIgnoreCase("title"))
			return getTitle().toLowerCase().contains(val.toLowerCase());
		if (field.equalsIgnoreCase("date"))
			return date.toString().equals(val);
		if (field.equalsIgnoreCase("type"))
			return val.equalsIgnoreCase("flag");
		return false;
	}

	@Override
	public String toRecord()
	{
		return String.format("FLAG|id=%s|title=%s|description=%s|date=%s", getId(),
				URLEncoder.encode(getTitle(), StandardCharsets.UTF_8),
				URLEncoder.encode(getDescription(), StandardCharsets.UTF_8), date);
	}

	// Getters
	public LocalDate getDate()
	{
		return date;
	}

	// Setters
	public void setDate(LocalDate date)
	{
		this.date = date;
	}
}
