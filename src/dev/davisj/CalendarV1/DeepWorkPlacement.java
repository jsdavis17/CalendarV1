package dev.davisj.CalendarV1;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DeepWorkPlacement extends PlacementPolicy
{
	@Override
	public LocalDateTime[] selectSlot(Task task, ArrayList<LocalDateTime[]> candidates)
	{
		if (candidates.isEmpty())
			return null;

		LocalDateTime[] best = candidates.get(0);
		long bestMinutes = Duration.between(best[0], best[1]).toMinutes();

		for (LocalDateTime[] candidate : candidates)
		{
			long minutes = Duration.between(candidate[0], candidate[1]).toMinutes();
			if (minutes > bestMinutes)
			{
				best = candidate;
				bestMinutes = minutes;
			}
		}

		LocalDateTime start = best[0];
		LocalDateTime end = start.plusMinutes(task.getDuration());
		return new LocalDateTime[] {start, end};
	}
}
