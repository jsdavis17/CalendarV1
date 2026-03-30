package dev.davisj.CalendarV1;

import java.util.UUID;

public abstract class Entry
{
	private UUID id;
	private String title;
	private String description;

	public Entry(String title, String description)
	{
		this.id = UUID.randomUUID();
		this.title = title;
		this.description = description;
	}

	Entry(UUID id, String title, String description)
	{
		this.id = id;
		this.title = title;
		this.description = description;
	}

	public abstract String display();

	public abstract boolean matches(String field, String val);

	public abstract String toRecord();

	// Getters
	public UUID getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	// Setters
	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}
