package dev.davisj.CalendarV1;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Event extends Entry
{
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private boolean busyStatus;

	public Event(String title, String description, LocalDate date, LocalTime startTime, LocalTime endTime, boolean busy)
	{
		super(title, description);
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.busyStatus = busy;
	}

	Event(UUID id, String title, String description, LocalDate date, LocalTime startTime, LocalTime endTime,
			boolean busy)
	{
		super(id, title, description);
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.busyStatus = busy;
	}

	public boolean overlaps(Event other)
	{
		if (!this.date.equals(other.date))
			return false;
		return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
	}

	@Override
	public String display()
	{
		return String.format("[EVENT] %s | %s | %s - %s | %s%n  %s", getTitle(), date, startTime, endTime,
				busyStatus ? "Busy" : "Free", getDescription());
	}

	@Override
	public boolean matches(String field, String val)
	{
		if (field.equalsIgnoreCase("title"))
			return getTitle().toLowerCase().contains(val.toLowerCase());
		if (field.equalsIgnoreCase("date"))
			return date.toString().equals(val);
		if (field.equalsIgnoreCase("type"))
			return val.equalsIgnoreCase("event");
		return false;
	}

	@Override
	public String toRecord()
	{
		return String.format("EVENT|id=%s|title=%s|description=%s|date=%s|start=%s|end=%s|busy=%b", getId(),
				URLEncoder.encode(getTitle(), StandardCharsets.UTF_8),
				URLEncoder.encode(getDescription(), StandardCharsets.UTF_8), date, startTime, endTime, busyStatus);
	}

	// Getters
	public LocalDate getDate()
	{
		return date;
	}

	public LocalTime getStartTime()
	{
		return startTime;
	}

	public LocalTime getEndTime()
	{
		return endTime;
	}

	public boolean isBusy()
	{
		return busyStatus;
	}

	// Setters
	public void setDate(LocalDate date)
	{
		this.date = date;
	}

	public void setStartTime(LocalTime startTime)
	{
		this.startTime = startTime;
	}

	public void setEndTime(LocalTime endTime)
	{
		this.endTime = endTime;
	}

	public void setBusyStatus(boolean busyStatus)
	{
		this.busyStatus = busyStatus;
	}
}
