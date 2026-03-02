package dev.davisj.CalendarV1;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

public class Task extends Entry
{
	private Instant due;
	private int minutes;
	
	public Task(UUID id, String title, String description, Instant due, int minutes)
	{
		super(id, title, description, due, due.plusSeconds(60 * minutes));
		this.due = due;
		this.minutes = minutes;
	}
	
	public Task(String title, String description, Instant due, int minutes)
	{
		super(UUID.randomUUID(), title, description, due, due.plusSeconds(60 * minutes));
	}

	@Override
	public String toRecord()
	{
		return String.format("TASK|id=%s|title=%s|description=%s|due=%d|minutes=%d", super.getID().toString(), URLEncoder.encode(super.getTitle(), StandardCharsets.UTF_8), URLEncoder.encode(super.getDescription(), StandardCharsets.UTF_8), due.toEpochMilli(), minutes);
	}
}
