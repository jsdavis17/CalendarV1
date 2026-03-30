package dev.davisj.CalendarV1;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class PlacementPolicy
{
	public abstract LocalDateTime[] selectSlot(Task task, ArrayList<LocalDateTime[]> candidates);
}
