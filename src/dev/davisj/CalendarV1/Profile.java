package dev.davisj.CalendarV1;

import java.time.LocalDate;
import java.util.ArrayList;

public class Profile
{
	private String profileName;
	private ArrayList<Entry> entries;
	private LocalDate focusDate;
	private String viewMode;

	public Profile(String name)
	{
		this.profileName = name;
		this.entries = new ArrayList<>();
		this.focusDate = LocalDate.now();
		this.viewMode = "week";
	}

	public void addEntry(Entry entry)
	{
		entries.add(entry);
	}

	public void removeEntry(Entry entry)
	{
		entries.remove(entry);
	}

	public ArrayList<Entry> getEntriesInRange(LocalDate start, LocalDate end)
	{
		ArrayList<Entry> result = new ArrayList<>();
		for (Entry e : entries)
		{
			if (e instanceof RecurringEvent)
			{
				result.addAll(((RecurringEvent) e).generateOccurrences(start, end));
			} else if (e instanceof Event)
			{
				LocalDate d = ((Event) e).getDate();
				if (!d.isBefore(start) && !d.isAfter(end))
					result.add(e);
			} else if (e instanceof Task)
			{
				Task t = (Task) e;
				LocalDate ref = t.isScheduled() ? t.getScheduledDate() : t.getDeadline();
				if (!ref.isBefore(start) && !ref.isAfter(end))
					result.add(e);
			} else if (e instanceof Flag)
			{
				LocalDate d = ((Flag) e).getDate();
				if (!d.isBefore(start) && !d.isAfter(end))
					result.add(e);
			}
		}
		return result;
	}

	public ArrayList<Entry> searchEntries(String field, String val)
	{
		ArrayList<Entry> results = new ArrayList<>();
		for (Entry e : entries)
			if (e.matches(field, val))
				results.add(e);
		return results;
	}

	public String[] getSettings()
	{
		return new String[]
		{ viewMode, focusDate.toString() };
	}

	public void setSettings(String[] settings)
	{
		if (settings.length >= 2)
		{
			viewMode = settings[0];
			focusDate = LocalDate.parse(settings[1]);
		}
	}

	// Getters
	public String getProfileName()
	{
		return profileName;
	}

	public ArrayList<Entry> getEntries()
	{
		return entries;
	}

	public LocalDate getFocusDate()
	{
		return focusDate;
	}

	public String getViewMode()
	{
		return viewMode;
	}

	// Setters
	public void setFocusDate(LocalDate date)
	{
		this.focusDate = date;
	}

	public void setViewMode(String mode)
	{
		this.viewMode = mode;
	}
}
