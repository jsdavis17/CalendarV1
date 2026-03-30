package dev.davisj.CalendarV1;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Task extends Entry
{
	private int duration;
	private LocalDate deadline;
	private int priority;
	private LocalDate scheduledDate;
	private LocalTime scheduledStart;

	public Task(String title, String description, int duration, LocalDate deadline, int priority)
	{
		super(title, description);
		this.duration = duration;
		this.deadline = deadline;
		this.priority = priority;
	}

	Task(UUID id, String title, String description, int duration, LocalDate deadline, int priority,
			LocalDate scheduledDate, LocalTime scheduledStart)
	{
		super(id, title, description);
		this.duration = duration;
		this.deadline = deadline;
		this.priority = priority;
		this.scheduledDate = scheduledDate;
		this.scheduledStart = scheduledStart;
	}

	public boolean isScheduled()
	{
		return scheduledDate != null && scheduledStart != null;
	}

	public void setScheduledTime(LocalDate date, LocalTime start)
	{
		this.scheduledDate = date;
		this.scheduledStart = start;
	}

	@Override
	public String display()
	{
		String sched = isScheduled() ? "Scheduled: " + scheduledDate + " at " + scheduledStart : "Unscheduled";
		return String.format("[TASK] %s | Due: %s | Priority: %d | %d min | %s%n  %s", getTitle(), deadline, priority,
				duration, sched, getDescription());
	}

	@Override
	public boolean matches(String field, String val)
	{
		if (field.equalsIgnoreCase("title"))
			return getTitle().toLowerCase().contains(val.toLowerCase());
		if (field.equalsIgnoreCase("deadline"))
			return deadline.toString().equals(val);
		if (field.equalsIgnoreCase("type"))
			return val.equalsIgnoreCase("task");
		return false;
	}

	@Override
	public String toRecord()
	{
		return String.format(
				"TASK|id=%s|title=%s|description=%s|duration=%d|deadline=%s|priority=%d"
						+ "|scheduledDate=%s|scheduledStart=%s",
				getId(), URLEncoder.encode(getTitle(), StandardCharsets.UTF_8),
				URLEncoder.encode(getDescription(), StandardCharsets.UTF_8), duration, deadline, priority,
				scheduledDate != null ? scheduledDate.toString() : "",
				scheduledStart != null ? scheduledStart.toString() : "");
	}

	// Getters
	public int getDuration()
	{
		return duration;
	}

	public LocalDate getDeadline()
	{
		return deadline;
	}

	public int getPriority()
	{
		return priority;
	}

	public LocalDate getScheduledDate()
	{
		return scheduledDate;
	}

	public LocalTime getScheduledStart()
	{
		return scheduledStart;
	}

	// Setters
	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public void setDeadline(LocalDate deadline)
	{
		this.deadline = deadline;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}
}
