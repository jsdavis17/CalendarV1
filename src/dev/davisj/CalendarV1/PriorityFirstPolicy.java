package dev.davisj.CalendarV1;

import java.util.ArrayList;

public class PriorityFirstPolicy extends SchedulingPolicy
{
	@Override
	public ArrayList<Task> sortTasks(ArrayList<Task> tasks)
	{
		ArrayList<Task> sorted = new ArrayList<>(tasks);
		sorted.sort((a, b) ->
		{
			int priorityCmp = Integer.compare(a.getPriority(), b.getPriority());
			if (priorityCmp != 0)
				return priorityCmp;
			return a.getDeadline().compareTo(b.getDeadline());
		});
		return sorted;
	}
}
