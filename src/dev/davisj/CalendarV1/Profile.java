package dev.davisj.CalendarV1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class Profile
{
	private String profileName;
	private ArrayList<Entry> entries;
	private LocalDate focusDate;
	private String viewMode;
	private LocalTime workStart;
	private LocalTime workEnd;
	private LocalTime noWorkStart;
	private LocalTime noWorkEnd;
	private int minBreakMinutes;

	public Profile(String name)
	{
		this.profileName = name;
		this.entries = new ArrayList<>();
		this.focusDate = LocalDate.now();
		this.viewMode = "week";
		this.workStart = LocalTime.of(8, 0);
		this.workEnd = LocalTime.of(20, 0);
		this.noWorkStart = null;
		this.noWorkEnd = null;
		this.minBreakMinutes = 0;
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
			}
			else if (e instanceof Event)
			{
				LocalDate d = ((Event) e).getDate();
				if (!d.isBefore(start) && !d.isAfter(end))
					result.add(e);
			}
			else if (e instanceof Task)
			{
				Task t = (Task) e;
				LocalDate ref = t.isScheduled() ? t.getScheduledDate() : t.getDeadline();
				if (!ref.isBefore(start) && !ref.isAfter(end))
					result.add(e);
			}
			else if (e instanceof Flag)
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

	// Settings serialization — order: viewMode, focusDate, workStart, workEnd,
	// noWorkStart, noWorkEnd, minBreak
	public String[] getSettings()
	{
		return new String[] {viewMode, focusDate.toString(), workStart.toString(), workEnd.toString(),
				noWorkStart != null ? noWorkStart.toString() : "", noWorkEnd != null ? noWorkEnd.toString() : "",
				String.valueOf(minBreakMinutes)};
	}

	public void setSettings(String[] settings)
	{
		if (settings.length >= 2)
		{
			viewMode = settings[0];
			focusDate = LocalDate.parse(settings[1]);
		}
		if (settings.length >= 4)
		{
			workStart = LocalTime.parse(settings[2]);
			workEnd = LocalTime.parse(settings[3]);
		}
		if (settings.length >= 6)
		{
			noWorkStart = settings[4].isEmpty() ? null : LocalTime.parse(settings[4]);
			noWorkEnd = settings[5].isEmpty() ? null : LocalTime.parse(settings[5]);
		}
		if (settings.length >= 7)
		{
			try
			{
				minBreakMinutes = Integer.parseInt(settings[6]);
			}
			catch (NumberFormatException ignored)
			{
			}
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

	public LocalTime getWorkStart()
	{
		return workStart;
	}

	public LocalTime getWorkEnd()
	{
		return workEnd;
	}

	public LocalTime getNoWorkStart()
	{
		return noWorkStart;
	}

	public LocalTime getNoWorkEnd()
	{
		return noWorkEnd;
	}

	public int getMinBreakMinutes()
	{
		return minBreakMinutes;
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

	public void setWorkStart(LocalTime workStart)
	{
		this.workStart = workStart;
	}

	public void setWorkEnd(LocalTime workEnd)
	{
		this.workEnd = workEnd;
	}

	public void setNoWorkStart(LocalTime noWorkStart)
	{
		this.noWorkStart = noWorkStart;
	}

	public void setNoWorkEnd(LocalTime noWorkEnd)
	{
		this.noWorkEnd = noWorkEnd;
	}

	public void setMinBreakMinutes(int minutes)
	{
		this.minBreakMinutes = minutes;
	}
}
