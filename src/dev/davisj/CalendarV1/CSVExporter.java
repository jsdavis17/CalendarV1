package dev.davisj.CalendarV1;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;

public class CSVExporter extends Exporter
{
	private final String HEADER = "type,id,title,description,date,startTime,endTime,busy,duration,deadline,priority,scheduledDate,scheduledStart";

	public CSVExporter(Profile profile)
	{
		super(profile);
	}

	@Override
	public String export(LocalDate start, LocalDate end, String filename)
	{
		ArrayList<Entry> entries = profile.getEntriesInRange(start, end);
		try (PrintWriter pw = new PrintWriter(new FileWriter(filename)))
		{
			pw.println(HEADER);
			for (Entry entry : entries)
				pw.println(toCsv(entry));
			return "Exported " + entries.size() + " entries to " + filename;
		}
		catch (Exception e)
		{
			return "Export error: " + e.getMessage();
		}
	}

	private String toCsv(Entry entry)
	{
		if (entry instanceof Task)
			return buildTaskRow((Task) entry);
		if (entry instanceof Flag)
			return buildFlagRow((Flag) entry);
		return buildEventRow((Event) entry);
	}

	private String buildEventRow(Event ev)
	{
		return row("EVENT", ev.getId().toString(), ev.getTitle(), ev.getDescription(), ev.getDate().toString(),
				ev.getStartTime().toString(), ev.getEndTime().toString(), String.valueOf(ev.isBusy()), "", "", "", "",
				"");
	}

	private String buildTaskRow(Task t)
	{
		return row("TASK", t.getId().toString(), t.getTitle(), t.getDescription(), "", "", "", "",
				String.valueOf(t.getDuration()), t.getDeadline().toString(), String.valueOf(t.getPriority()),
				t.getScheduledDate() != null ? t.getScheduledDate().toString() : "",
				t.getScheduledStart() != null ? t.getScheduledStart().toString() : "");
	}

	private String buildFlagRow(Flag f)
	{
		return row("FLAG", f.getId().toString(), f.getTitle(), f.getDescription(), f.getDate().toString(), "", "", "",
				"", "", "", "", "");
	}

	private String row(String... values)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++)
		{
			if (i > 0)
				sb.append(',');
			sb.append(csvEscape(values[i]));
		}
		return sb.toString();
	}

	private String csvEscape(String value)
	{
		if (value.contains(",") || value.contains("\"") || value.contains("\n"))
			return "\"" + value.replace("\"", "\"\"") + "\"";
		return value;
	}
}
