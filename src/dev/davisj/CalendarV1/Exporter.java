package dev.davisj.CalendarV1;

import java.util.ArrayList;

public abstract class Exporter
{
	public Exporter()
	{}
	
	public abstract void export(ArrayList<Entry> entries);
}
