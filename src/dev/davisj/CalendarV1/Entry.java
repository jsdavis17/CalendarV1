package dev.davisj.CalendarV1;

import java.time.*;
import java.util.UUID;

public abstract class Entry
{
	private UUID id;
	private String title;
	private String description;
	private Instant startTime;
	private Instant endTime;
	
	public Entry(UUID id, String title, String description, Instant startTime, Instant endTime)
	{
		this.id = id;
		this.title = title;
		this.description = description;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	// Getters
	public UUID getID()
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
	
	public Instant getStart()
	{
		return startTime;
	}
	
	public Instant getEnd()
	{
		return endTime;
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
	
	public void setStart(Instant startTime)
	{
		this.startTime = startTime;
	}
	
	public void setEnd(Instant endTime)
	{
		this.endTime = endTime;
	}
	
	public abstract String toRecord();
}
