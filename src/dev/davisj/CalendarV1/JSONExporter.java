package dev.davisj.CalendarV1;

import java.time.LocalDate;

public class JSONExporter extends Exporter
{
	public JSONExporter(Profile profile)
	{
		super(profile);
	}

	@Override
	public void export(LocalDate start, LocalDate end, String filename)
	{
	}
}
