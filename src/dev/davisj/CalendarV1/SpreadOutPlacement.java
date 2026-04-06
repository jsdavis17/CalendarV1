package dev.davisj.CalendarV1;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class SpreadOutPlacement extends PlacementPolicy
{
	@Override
	public LocalDateTime[] selectSlot(Task task, ArrayList<LocalDateTime[]> candidates)
	{
		if (candidates.isEmpty())
			return null;

		LocalDateTime[] chosen = candidates.get(candidates.size() / 2);
		LocalDateTime start = chosen[0];
		LocalDateTime end = start.plusMinutes(task.getDuration());
		return new LocalDateTime[] {start, end};
	}
}
