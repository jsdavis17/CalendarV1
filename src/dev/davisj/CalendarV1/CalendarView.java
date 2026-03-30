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

	public void display()
	{
		switch (profile.getViewMode())
		{
		case "3day":
			displayThreeDay(profile.getFocusDate());
			break;
		case "week":
			displayWeek(profile.getFocusDate());
			break;
		case "month":
			displayMonth(profile.getFocusDate());
			break;
		default:
			displayDay(profile.getFocusDate());
			break;
		}
	}

	public void displayDay(LocalDate date)
	{
		System.out.println("\n══════════════════════════════════════");
		System.out.println("  Day View: " + date);
		System.out.println("══════════════════════════════════════");
		printEntriesForDate(date, false);
	}

	public void displayThreeDay(LocalDate date)
	{
		System.out.println("\n══════════════════════════════════════");
		System.out.println("  3-Day View: " + date + " to " + date.plusDays(2));
		System.out.println("══════════════════════════════════════");
		for (int i = 0; i < 3; i++)
			printEntriesForDate(date.plusDays(i), true);
	}

	public void displayWeek(LocalDate date)
	{
		System.out.println("\n══════════════════════════════════════");
		System.out.println("  Week View: " + date + " to " + date.plusDays(6));
		System.out.println("══════════════════════════════════════");
		for (int i = 0; i < 7; i++)
			printEntriesForDate(date.plusDays(i), true);
	}

	public void displayMonth(LocalDate date)
	{
		LocalDate first = date.withDayOfMonth(1);
		LocalDate last = date.withDayOfMonth(date.lengthOfMonth());

		System.out.println("\n══════════════════════════════════════");
		System.out.printf("  Month View: %s %d%n", date.getMonth(), date.getYear());
		System.out.println("══════════════════════════════════════");
		System.out.println(" SUN  MON  TUE  WED  THU  FRI  SAT");

		int startDow = first.getDayOfWeek().getValue() % 7;
		StringBuilder row = new StringBuilder();
		for (int pad = 0; pad < startDow; pad++)
			row.append("     ");

		for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1))
		{
			boolean hasEntries = !profile.getEntriesInRange(d, d).isEmpty();
			row.append(String.format("%2d%s  ", d.getDayOfMonth(), hasEntries ? "*" : " "));
			int col = (startDow + d.getDayOfMonth() - 1) % 7;
			if (col == 6)
			{
				System.out.println(row);
				row = new StringBuilder();
			}
		}
		if (row.length() > 0)
			System.out.println(row);

		ArrayList<Entry> all = profile.getEntriesInRange(first, last);
		if (!all.isEmpty())
		{
			System.out.println("\nEntries this month:");
			for (Entry e : all)
				System.out.println("  " + formatEntry(e));
		}
	}

	private void printEntriesForDate(LocalDate date, boolean compact)
	{
		System.out.println("  -- " + date.getDayOfWeek() + " " + date + " --");
		ArrayList<Entry> entries = profile.getEntriesInRange(date, date);
		if (entries.isEmpty())
		{
			System.out.println("    (no entries)");
			return;
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

		if (compact)
		{
			Set<Integer> suppressed = new HashSet<>();
			for (int i = 0; i < timedEvents.size(); i++)
			{
				if (suppressed.contains(i))
					continue;
				Event ev = timedEvents.get(i);
				int overlapCount = 0;
				for (int j = i + 1; j < timedEvents.size(); j++)
				{
					if (ev.overlaps(timedEvents.get(j)))
					{
						overlapCount++;
						suppressed.add(j);
					}
				}
				String suffix = overlapCount > 0 ? "  +" + overlapCount + " ovlp" : "";
				System.out.println("    " + formatEntry(ev) + suffix);
			}
		} else
		{
			Set<Integer> conflictIdx = new HashSet<>();
			for (int i = 0; i < timedEvents.size(); i++)
				for (int j = i + 1; j < timedEvents.size(); j++)
					if (timedEvents.get(i).overlaps(timedEvents.get(j)))
					{
						conflictIdx.add(i);
						conflictIdx.add(j);
					}

			for (int i = 0; i < timedEvents.size(); i++)
			{
				String marker = conflictIdx.contains(i) ? "  [!]" : "";
				System.out.println("    " + formatEntry(timedEvents.get(i)) + marker);
			}
		}

		for (Entry e : other)
			System.out.println("    " + formatEntry(e));
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

	public void showConflictMarker(ArrayList<Event> events)
	{
		for (int i = 0; i < events.size(); i++)
			for (int j = i + 1; j < events.size(); j++)
				if (events.get(i).overlaps(events.get(j)))
					System.out.printf("    *** CONFLICT: \"%s\" overlaps \"%s\" ***%n", events.get(i).getTitle(),
							events.get(j).getTitle());
	}
}
