package dev.davisj.CalendarV1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class Scheduler
{
	private final Profile profile;

	public Scheduler(Profile profile)
	{
		this.profile = profile;
	}

	public ArrayList<Event> detectConflicts(Event event)
	{
		ArrayList<Event> conflicts = new ArrayList<>();
		for (Entry e : profile.getEntries())
		{
			if (e instanceof Event && !e.equals(event))
			{
				Event other = (Event) e;
				if (event.overlaps(other))
					conflicts.add(other);
			}
		}
		return conflicts;
	}

	public SchedulingResult scheduleTasks(LocalDate from, SchedulingPolicy sortPolicy, PlacementPolicy placementPolicy)
	{
		ArrayList<Task> unscheduled = new ArrayList<>();
		for (Entry e : profile.getEntries())
		{
			if (e instanceof Task)
			{
				Task t = (Task) e;
				if (!t.isScheduled())
					unscheduled.add(t);
			}
		}

		ArrayList<Task> ordered = sortPolicy.sortTasks(unscheduled);

		ArrayList<Task> scheduled = new ArrayList<>();
		ArrayList<Task> unplaceable = new ArrayList<>();
		ArrayList<Task> newlyScheduled = new ArrayList<>();

		for (Task task : ordered)
		{
			if (task.getDeadline().isBefore(from))
			{
				unplaceable.add(task);
				continue;
			}

			ArrayList<LocalDateTime[]> candidates = findCandidateSlots(task, from, newlyScheduled);

			LocalDateTime[] chosen = placementPolicy.selectSlot(task, candidates);

			if (chosen == null)
			{
				unplaceable.add(task);
			}
			else
			{
				task.setScheduledTime(chosen[0].toLocalDate(), chosen[0].toLocalTime());
				newlyScheduled.add(task);
				scheduled.add(task);
			}
		}

		return new SchedulingResult(scheduled, unplaceable);
	}

	private ArrayList<LocalDateTime[]> findCandidateSlots(Task task, LocalDate from, ArrayList<Task> newlyScheduled)
	{
		ArrayList<LocalDateTime[]> candidates = new ArrayList<>();
		LocalDate deadline = task.getDeadline();
		LocalTime workStart = profile.getWorkStart();
		LocalTime workEnd = profile.getWorkEnd();

		for (LocalDate day = from; !day.isAfter(deadline); day = day.plusDays(1))
		{
			ArrayList<LocalTime[]> occupied = getOccupiedIntervals(day, newlyScheduled);

			ArrayList<LocalTime[]> freeSlots = subtractOccupied(workStart, workEnd, occupied);

			for (LocalTime[] slot : freeSlots)
			{
				long slotMinutes = java.time.Duration.between(slot[0], slot[1]).toMinutes();
				if (slotMinutes >= task.getDuration())
				{
					candidates
							.add(new LocalDateTime[] {LocalDateTime.of(day, slot[0]), LocalDateTime.of(day, slot[1])});
				}
			}
		}

		return candidates;
	}

	private ArrayList<LocalTime[]> getOccupiedIntervals(LocalDate date, ArrayList<Task> newlyScheduled)
	{
		ArrayList<LocalTime[]> occupied = new ArrayList<>();
		int minBreak = profile.getMinBreakMinutes();
		LocalTime workEnd = profile.getWorkEnd();

		LocalTime noWorkStart = profile.getNoWorkStart();
		LocalTime noWorkEnd = profile.getNoWorkEnd();
		if (noWorkStart != null && noWorkEnd != null && noWorkEnd.isAfter(noWorkStart))
			occupied.add(new LocalTime[] {noWorkStart, noWorkEnd});

		for (Entry e : profile.getEntries())
		{
			if (e instanceof RecurringEvent)
			{
				RecurringEvent re = (RecurringEvent) e;
				if (re.isBusy())
				{
					for (Event occ : re.generateOccurrences(date, date))
						occupied.add(new LocalTime[] {occ.getStartTime(), occ.getEndTime()});
				}
			}
			else if (e instanceof Event)
			{
				Event ev = (Event) e;
				if (ev.isBusy() && ev.getDate().equals(date))
					occupied.add(new LocalTime[] {ev.getStartTime(), ev.getEndTime()});
			}
			else if (e instanceof Task)
			{
				Task t = (Task) e;
				if (t.isScheduled() && t.getScheduledDate().equals(date))
				{
					LocalTime tEnd = t.getScheduledStart().plusMinutes(t.getDuration());
					LocalTime paddedEnd = minBreak > 0 ? tEnd.plusMinutes(minBreak) : tEnd;
					if (paddedEnd.isAfter(workEnd))
						paddedEnd = workEnd;
					occupied.add(new LocalTime[] {t.getScheduledStart(), paddedEnd});
				}
			}
		}

		for (Task t : newlyScheduled)
		{
			if (t.getScheduledDate().equals(date))
			{
				LocalTime tEnd = t.getScheduledStart().plusMinutes(t.getDuration());
				LocalTime paddedEnd = minBreak > 0 ? tEnd.plusMinutes(minBreak) : tEnd;
				if (paddedEnd.isAfter(workEnd))
					paddedEnd = workEnd;
				occupied.add(new LocalTime[] {t.getScheduledStart(), paddedEnd});
			}
		}

		return occupied;
	}

	private ArrayList<LocalTime[]> subtractOccupied(LocalTime workStart, LocalTime workEnd,
			ArrayList<LocalTime[]> occupied)
	{
		ArrayList<LocalTime[]> free = new ArrayList<>();
		free.add(new LocalTime[] {workStart, workEnd});

		for (LocalTime[] occ : occupied)
		{
			ArrayList<LocalTime[]> remaining = new ArrayList<>();

			for (LocalTime[] seg : free)
			{
				LocalTime clippedStart = occ[0].isBefore(seg[0]) ? seg[0] : occ[0];
				LocalTime clippedEnd = occ[1].isAfter(seg[1]) ? seg[1] : occ[1];

				if (!clippedStart.isBefore(clippedEnd))
				{
					remaining.add(seg);
					continue;
				}

				if (seg[0].isBefore(clippedStart))
					remaining.add(new LocalTime[] {seg[0], clippedStart});

				if (clippedEnd.isBefore(seg[1]))
					remaining.add(new LocalTime[] {clippedEnd, seg[1]});
			}

			free = remaining;
		}

		return free;
	}
}
