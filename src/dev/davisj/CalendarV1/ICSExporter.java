package dev.davisj.CalendarV1;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ICSExporter extends Exporter
{
	public ICSExporter(Profile profile)
	{
		super(profile);
	}

	@Override
	public String export(LocalDate start, LocalDate end, String filename)
	{
		ArrayList<Entry> entries = profile.getEntriesInRange(start, end);
		try (PrintWriter pw = new PrintWriter(new FileWriter(filename)))
		{
			pw.print("BEGIN:VCALENDAR\r\n");
			pw.print("VERSION:2.0\r\n");
			pw.print("PRODID:-//ChronoPlan//CalendarV1//EN\r\n");
			for (Entry entry : entries)
				pw.print(buildEntry(entry));
			pw.print("END:VCALENDAR\r\n");
			return "Exported " + entries.size() + " entries to " + filename;
		}
		catch (Exception e)
		{
			return "Export error: " + e.getMessage();
		}
	}

	private String buildEntry(Entry entry)
	{
		if (entry instanceof Task)
			return buildTodo((Task) entry);
		if (entry instanceof Flag)
			return buildFlag((Flag) entry);
		return buildEvent((Event) entry);
	}

	private String buildEvent(Event ev)
	{
		StringBuilder sb = new StringBuilder("BEGIN:VEVENT\r\n");
		sb.append("UID:").append(ev.getId()).append("@chronoplan\r\n");
		sb.append("DTSTART:").append(icsDateTime(ev.getDate(), ev.getStartTime())).append("\r\n");
		sb.append("DTEND:").append(icsDateTime(ev.getDate(), ev.getEndTime())).append("\r\n");
		sb.append("SUMMARY:").append(icsText(ev.getTitle())).append("\r\n");
		if (!ev.getDescription().isEmpty())
			sb.append("DESCRIPTION:").append(icsText(ev.getDescription())).append("\r\n");
		sb.append("END:VEVENT\r\n");
		return sb.toString();
	}

	private String buildFlag(Flag f)
	{
		StringBuilder sb = new StringBuilder("BEGIN:VEVENT\r\n");
		sb.append("UID:").append(f.getId()).append("@chronoplan\r\n");
		sb.append("DTSTART;VALUE=DATE:").append(icsDate(f.getDate())).append("\r\n");
		sb.append("DTEND;VALUE=DATE:").append(icsDate(f.getDate().plusDays(1))).append("\r\n");
		sb.append("SUMMARY:").append(icsText(f.getTitle())).append("\r\n");
		if (!f.getDescription().isEmpty())
			sb.append("DESCRIPTION:").append(icsText(f.getDescription())).append("\r\n");
		sb.append("END:VEVENT\r\n");
		return sb.toString();
	}

	private String buildTodo(Task t)
	{
		StringBuilder sb = new StringBuilder("BEGIN:VTODO\r\n");
		sb.append("UID:").append(t.getId()).append("@chronoplan\r\n");
		sb.append("SUMMARY:").append(icsText(t.getTitle())).append("\r\n");
		if (!t.getDescription().isEmpty())
			sb.append("DESCRIPTION:").append(icsText(t.getDescription())).append("\r\n");
		sb.append("DUE;VALUE=DATE:").append(icsDate(t.getDeadline())).append("\r\n");
		sb.append("PRIORITY:").append(icsPriority(t.getPriority())).append("\r\n");
		if (t.isScheduled())
			sb.append("DTSTART:").append(icsDateTime(t.getScheduledDate(), t.getScheduledStart())).append("\r\n");
		sb.append("END:VTODO\r\n");
		return sb.toString();
	}

	private String icsDate(LocalDate d)
	{
		return String.format("%04d%02d%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
	}

	private String icsDateTime(LocalDate d, LocalTime t)
	{
		return String.format("%04d%02d%02dT%02d%02d%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth(),
				t.getHour(), t.getMinute(), t.getSecond());
	}

	private String icsText(String s)
	{
		return s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
	}

	private int icsPriority(int p)
	{
		if (p == 1)
			return 1;
		if (p == 2)
			return 5;
		return 9;
	}
}
