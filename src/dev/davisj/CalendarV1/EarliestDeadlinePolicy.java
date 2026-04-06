package dev.davisj.CalendarV1;

import java.util.ArrayList;

public class EarliestDeadlinePolicy extends SchedulingPolicy
{
	@Override
	public ArrayList<Task> sortTasks(ArrayList<Task> tasks)
	{
		ArrayList<Task> sorted = new ArrayList<>(tasks);
		sorted.sort((a, b) ->
		{
			int deadlineCmp = a.getDeadline().compareTo(b.getDeadline());
			if (deadlineCmp != 0)
				return deadlineCmp;
			return Integer.compare(a.getPriority(), b.getPriority());
		});
		return sorted;
	}
}
