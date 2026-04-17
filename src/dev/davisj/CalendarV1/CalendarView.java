package dev.davisj.CalendarV1;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CalendarView
{
	private Profile profile;

	public CalendarView(Profile profile)
	{
		this.profile = profile;
	}

	public String display()
	{
		switch (profile.getViewMode())
		{
		case "3day":
			return buildThreeDay(profile.getFocusDate());
		case "week":
			return buildWeek(profile.getFocusDate());
		case "month":
			return buildMonth(profile.getFocusDate());
		default:
			return buildDay(profile.getFocusDate());
		}
	}

	public String buildDay(LocalDate date)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n══════════════════════════════════════\n");
		sb.append("  Day View: ").append(date).append("\n");
		sb.append("══════════════════════════════════════\n");
		sb.append(buildEntriesForDate(date, false));
		return sb.toString();
	}

	public String buildThreeDay(LocalDate date)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n══════════════════════════════════════\n");
		sb.append("  3-Day View: ").append(date).append(" to ").append(date.plusDays(2)).append("\n");
		sb.append("══════════════════════════════════════\n");
		for (int i = 0; i < 3; i++)
			sb.append(buildEntriesForDate(date.plusDays(i), true));
		return sb.toString();
	}

	public String buildWeek(LocalDate date)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n══════════════════════════════════════\n");
		sb.append("  Week View: ").append(date).append(" to ").append(date.plusDays(6)).append("\n");
		sb.append("══════════════════════════════════════\n");
		for (int i = 0; i < 7; i++)
			sb.append(buildEntriesForDate(date.plusDays(i), true));
		return sb.toString();
	}

	public String buildMonth(LocalDate date)
	{
		LocalDate first = date.withDayOfMonth(1);
		LocalDate last = date.withDayOfMonth(date.lengthOfMonth());
		StringBuilder sb = new StringBuilder();
		sb.append("\n══════════════════════════════════════\n");
		sb.append(String.format("  Month View: %s %d%n", date.getMonth(), date.getYear()));
		sb.append("══════════════════════════════════════\n");
		sb.append(buildMonthGrid(first, last));
		for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1))
			if (!profile.getEntriesInRange(d, d).isEmpty())
				sb.append(buildEntriesForDate(d, false));
		return sb.toString();
	}

	private String buildMonthGrid(LocalDate first, LocalDate last)
	{
		StringBuilder sb = new StringBuilder(" SUN  MON  TUE  WED  THU  FRI  SAT\n");
		int startDow = first.getDayOfWeek().getValue() % 7;
		StringBuilder row = new StringBuilder();
		for (int pad = 0; pad < startDow; pad++)
			row.append("     ");
		for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1))
		{
			boolean hasEntries = !profile.getEntriesInRange(d, d).isEmpty();
			row.append(String.format("%2d%s  ", d.getDayOfMonth(), hasEntries ? "*" : " "));
			if ((startDow + d.getDayOfMonth() - 1) % 7 == 6)
			{
				sb.append(row).append("\n");
				row = new StringBuilder();
			}
		}
		if (row.length() > 0)
			sb.append(row).append("\n");
		return sb.toString();
	}

	private String buildEntriesForDate(LocalDate date, boolean compact)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("  -- ").append(date.getDayOfWeek()).append(" ").append(date).append(" --\n");
		ArrayList<Entry> entries = profile.getEntriesInRange(date, date);
		if (entries.isEmpty())
		{
			sb.append("    (no entries)\n");
			return sb.toString();
		}
		ArrayList<Event> timedEvents = new ArrayList<>();
		ArrayList<Entry> other = new ArrayList<>();
		for (Entry e : entries)
		{
			if (e instanceof Event)
				timedEvents.add((Event) e);
			else
				other.add(e);
		}
		timedEvents.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		sb.append(compact ? buildTimedEventsCompact(timedEvents) : buildTimedEventsFull(timedEvents));
		for (Entry e : other)
			sb.append(formatBlock(formatEntry(e)));
		return sb.toString();
	}

	private String buildTimedEventsCompact(ArrayList<Event> timedEvents)
	{
		StringBuilder sb = new StringBuilder();
		Set<Integer> suppressed = new HashSet<>();
		for (int i = 0; i < timedEvents.size(); i++)
		{
			if (suppressed.contains(i))
				continue;
			Event ev = timedEvents.get(i);
			int overlapCount = 0;
			for (int j = i + 1; j < timedEvents.size(); j++)
				if (ev.overlaps(timedEvents.get(j)))
				{
					overlapCount++;
					suppressed.add(j);
				}
			String content = formatEntry(ev) + (overlapCount > 0 ? "  +" + overlapCount + " ovlp" : "");
			sb.append(formatBlock(content));
		}
		return sb.toString();
	}

	private String buildTimedEventsFull(ArrayList<Event> timedEvents)
	{
		Set<Integer> conflictIdx = new HashSet<>();
		for (int i = 0; i < timedEvents.size(); i++)
			for (int j = i + 1; j < timedEvents.size(); j++)
				if (timedEvents.get(i).overlaps(timedEvents.get(j)))
				{
					conflictIdx.add(i);
					conflictIdx.add(j);
				}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < timedEvents.size(); i++)
		{
			String content = formatEntry(timedEvents.get(i));
			if (conflictIdx.contains(i))
				content += "  [!]";
			sb.append(formatBlock(content));
		}
		return sb.toString();
	}

	private String formatBlock(String content)
	{
		int width = 40;
		String line = content.length() > width
				? content.substring(0, width - 1) + "\u2026"
				: String.format("%-" + width + "s", content);
		String bar = "\u2500".repeat(width + 2);
		return "    \u250c" + bar + "\u2510\n"
				+ "    \u2502 " + line + " \u2502\n"
				+ "    \u2514" + bar + "\u2518\n";
	}

	public String formatEntry(Entry entry)
	{
		DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
		if (entry instanceof Event)
		{
			Event ev = (Event) entry;
			return String.format("%s-%s  %s%s", ev.getStartTime().format(timeFmt), ev.getEndTime().format(timeFmt),
					ev.getTitle(), ev.isBusy() ? " [busy]" : "");
		}
		if (entry instanceof Task)
		{
			Task t = (Task) entry;
			return String.format("[TASK] %s  (due %s, p%d)%s", t.getTitle(), t.getDeadline(), t.getPriority(),
					t.isScheduled() ? "" : "  [unscheduled]");
		}
		if (entry instanceof Flag)
			return "[FLAG] " + entry.getTitle();
		return entry.display();
	}

	public String showConflictMarker(ArrayList<Event> events)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < events.size(); i++)
			for (int j = i + 1; j < events.size(); j++)
				if (events.get(i).overlaps(events.get(j)))
					sb.append(String.format("    *** CONFLICT: \"%s\" overlaps \"%s\" ***%n", events.get(i).getTitle(),
							events.get(j).getTitle()));
		return sb.toString();
	}
}
