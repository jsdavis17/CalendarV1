package dev.davisj.CalendarV1;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

public class Event extends Entry
{
	public Event(UUID id, String title, String description, Instant startTime, Instant endTime)
	{
		super(id, title, description, startTime, endTime);
	}
	
	public Event(String title, String description, Instant startTime, Instant endTime)
	{
		super(UUID.randomUUID(), title, description, startTime, endTime);
	}

	@Override
	public String toRecord()
	{
		return String.format("EVENT|id=%s|title=%s|description=%s|start=%d|end=%d", super.getID().toString(), URLEncoder.encode(super.getTitle(), StandardCharsets.UTF_8), URLEncoder.encode(super.getDescription(), StandardCharsets.UTF_8), super.getStart().toEpochMilli(), super.getEnd().toEpochMilli());
	}
}
