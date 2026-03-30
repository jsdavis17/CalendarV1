package dev.davisj.CalendarV1;

import java.time.LocalDate;

public abstract class Exporter
{
	public Exporter(Profile profile)
	{
	}

	public abstract void export(LocalDate start, LocalDate end, String filename);
}
