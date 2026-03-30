package dev.davisj.CalendarV1;

import java.util.ArrayList;

public class Scheduler
{
	private Profile profile;

	public Scheduler(Profile profile)
	{
		this.profile = profile;
	}

	public ArrayList<Event> detectConflicts(Event event)
	{
		ArrayList<Event> conflicts = new ArrayList<>();
		for (Entry e : profile.getEntries())
		{
			if (e instanceof Event && !e.equals(event))
			{
				Event other = (Event) e;
				if (event.overlaps(other))
					conflicts.add(other);
			}
		}
		return conflicts;
	}
}
