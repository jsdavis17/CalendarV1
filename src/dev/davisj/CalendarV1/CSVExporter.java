package dev.davisj.CalendarV1;

import java.time.LocalDate;

public class CSVExporter extends Exporter
{
	public CSVExporter(Profile profile)
	{
		super(profile);
	}

	@Override
	public void export(LocalDate start, LocalDate end, String filename)
	{
	}
}
