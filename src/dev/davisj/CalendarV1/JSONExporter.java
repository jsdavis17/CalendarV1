package dev.davisj.CalendarV1;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;

public class JSONExporter extends Exporter
{
	public JSONExporter(Profile profile)
	{
		super(profile);
	}

	@Override
	public String export(LocalDate start, LocalDate end, String filename)
	{
		ArrayList<Entry> entries = profile.getEntriesInRange(start, end);
		try (PrintWriter pw = new PrintWriter(new FileWriter(filename)))
		{
			pw.println("[");
			for (int i = 0; i < entries.size(); i++)
			{
				pw.print(toJson(entries.get(i)));
				pw.println(i < entries.size() - 1 ? "," : "");
			}
			pw.println("]");
			return "Exported " + entries.size() + " entries to " + filename;
		}
		catch (Exception e)
		{
			return "Export error: " + e.getMessage();
		}
	}

	private String toJson(Entry entry)
	{
		StringBuilder sb = new StringBuilder("  {\n");
		if (entry instanceof Task)
			buildTaskJson(sb, (Task) entry);
		else if (entry instanceof Flag)
			buildFlagJson(sb, (Flag) entry);
		else if (entry instanceof Event)
			buildEventJson(sb, (Event) entry);
		sb.append("  }");
		return sb.toString();
	}

	private void buildEventJson(StringBuilder sb, Event ev)
	{
		sb.append(field("type", "EVENT")).append(field("id", ev.getId().toString()));
		sb.append(field("title", ev.getTitle())).append(field("description", ev.getDescription()));
		sb.append(field("date", ev.getDate().toString())).append(field("startTime", ev.getStartTime().toString()));
		sb.append(field("endTime", ev.getEndTime().toString())).append(fieldLast("busy", ev.isBusy()));
	}

	private void buildTaskJson(StringBuilder sb, Task t)
	{
		sb.append(field("type", "TASK")).append(field("id", t.getId().toString()));
		sb.append(field("title", t.getTitle())).append(field("description", t.getDescription()));
		sb.append(field("duration", t.getDuration())).append(field("deadline", t.getDeadline().toString()));
		sb.append(field("priority", t.getPriority()));
		sb.append(field("scheduledDate", t.getScheduledDate() != null ? t.getScheduledDate().toString() : ""));
		sb.append(fieldLast("scheduledStart", t.getScheduledStart() != null ? t.getScheduledStart().toString() : ""));
	}

	private void buildFlagJson(StringBuilder sb, Flag f)
	{
		sb.append(field("type", "FLAG")).append(field("id", f.getId().toString()));
		sb.append(field("title", f.getTitle())).append(field("description", f.getDescription()));
		sb.append(fieldLast("date", f.getDate().toString()));
	}

	private String field(String key, String value)
	{
		return "    \"" + key + "\": \"" + escape(value) + "\",\n";
	}

	private String field(String key, int value)
	{
		return "    \"" + key + "\": " + value + ",\n";
	}

	private String fieldLast(String key, String value)
	{
		return "    \"" + key + "\": \"" + escape(value) + "\"\n";
	}

	private String fieldLast(String key, boolean value)
	{
		return "    \"" + key + "\": " + value + "\n";
	}

	private String escape(String s)
	{
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}
}
