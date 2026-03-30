package dev.davisj.CalendarV1;

import java.time.LocalDate;

public class ICSExporter extends Exporter
{
	public ICSExporter(Profile profile)
	{
		super(profile);
	}

	@Override
	public void export(LocalDate start, LocalDate end, String filename)
	{
	}
}
