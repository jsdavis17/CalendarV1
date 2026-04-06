package dev.davisj.CalendarV1;

import java.util.ArrayList;

public class SchedulingResult
{
	private final ArrayList<Task> scheduled;
	private final ArrayList<Task> unplaceable;

	public SchedulingResult(ArrayList<Task> scheduled, ArrayList<Task> unplaceable)
	{
		this.scheduled = scheduled;
		this.unplaceable = unplaceable;
	}

	public ArrayList<Task> getScheduled()
	{
		return scheduled;
	}

	public ArrayList<Task> getUnplaceable()
	{
		return unplaceable;
	}

	public boolean isComplete()
	{
		return unplaceable.isEmpty();
	}

	public int totalProcessed()
	{
		return scheduled.size() + unplaceable.size();
	}
}
