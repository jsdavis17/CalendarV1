package dev.davisj.CalendarV1;

import java.time.LocalDate;

public abstract class Exporter
{
	protected final Profile profile;

	public Exporter(Profile profile)
	{
		this.profile = profile;
	}

	public abstract String export(LocalDate start, LocalDate end, String filename);
}
