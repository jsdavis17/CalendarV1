package dev.davisj.CalendarV1;

import java.util.ArrayList;

public abstract class SchedulingPolicy
{
	public abstract ArrayList<Task> sortTasks(ArrayList<Task> tasks);
}
