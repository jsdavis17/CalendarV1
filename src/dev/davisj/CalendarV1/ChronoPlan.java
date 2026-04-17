package dev.davisj.CalendarV1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChronoPlan
{
	private Profile profile;
	private ProfileManager profileManager;
	private CalendarView view;
	private Scheduler scheduler;
	private Scanner scanner;

	public static void main(String[] args)
	{
		new ChronoPlan().run();
	}

	public ChronoPlan()
	{
		this.profileManager = new ProfileManager();
		this.scanner = new Scanner(System.in);
	}

	// Start program
	public void run()
	{
		System.out.println("╔══════════════════════════════╗");
		System.out.println("║      Welcome to ChronoPlan   ║");
		System.out.println("╚══════════════════════════════╝");
		loadOrCreateProfile();
		view = new CalendarView(profile);
		scheduler = new Scheduler(profile);
		handleMenu();
	}

	// Handles loading or creating of profile on startup
	private void loadOrCreateProfile()
	{
		ArrayList<String> profiles = profileManager.listProfiles();

		// Check if no profiles exist, then create a new one
		if (profiles.isEmpty())
		{
			System.out.println("No profiles found. Let's create one.");
			String name = getStringInput("Profile name: ");
			profile = profileManager.createProfile(name);
			System.out.println("Profile \"" + name + "\" created.");
			handleNewProfileSetup();
			return;
		}

		// Load existing profile from list or create a new one
		System.out.println("\nExisting profiles:");
		for (int i = 0; i < profiles.size(); i++)
			System.out.println("  " + (i + 1) + ". " + profiles.get(i));
		System.out.println("  " + (profiles.size() + 1) + ". Create new profile");
		int choice = getIntInput("Select: ");
		if (choice >= 1 && choice <= profiles.size())
		{
			profile = profileManager.loadProfile(profiles.get(choice - 1));
			System.out.println("Loaded profile: " + profile.getProfileName());
		}
		else
		{
			String name = getStringInput("New profile name: ");
			profile = profileManager.createProfile(name);
			System.out.println("Profile \"" + name + "\" created.");
			handleNewProfileSetup();
		}
	}

	private void handleNewProfileSetup()
	{
		System.out.println("\nConfigure profile (Enter to keep default):");
		System.out.println("  Calendar view (day / 3day / week / month) [week]:");
		String v = scanner.nextLine().trim().toLowerCase();
		if (!v.isEmpty() && (v.equals("day") || v.equals("3day") || v.equals("week") || v.equals("month")))
			profile.setViewMode(v);
		LocalTime ws = tryTime("  Work-day start time (HH:MM) [08:00]: ");
		if (ws != null)
			profile.setWorkStart(ws);
		LocalTime we = tryTime("  Work-day end   time (HH:MM) [20:00]: ");
		if (we != null)
		{
			if (we.isAfter(profile.getWorkStart()))
				profile.setWorkEnd(we);
			else
				System.out.println("  End must be after start - using default 20:00.");
		}
		promptNoWorkPeriod();
		autosave();
	}

	private void promptNoWorkPeriod()
	{
		System.out.println("  No-work period (e.g. 12:00-13:00 for lunch break):");
		LocalTime nws = tryTime("    No-work start (HH:MM, or Enter for none): ");
		if (nws != null)
		{
			LocalTime nwe = tryTime("    No-work end (HH:MM): ");
			if (nwe != null && nwe.isAfter(nws))
			{
				profile.setNoWorkStart(nws);
				profile.setNoWorkEnd(nwe);
			}
			else
			{
				System.out.println("    No-work end must be after start - period not set.");
			}
		}
		Integer mb = tryInt("  Minimum break between work sessions (minutes) [0]: ");
		if (mb != null && mb >= 0)
			profile.setMinBreakMinutes(mb);
	}

	private void autosave()
	{
		profileManager.saveProfile(profile);
	}

	public void handleMenu()
	{
		boolean running = true;
		while (running)
		{
			System.out.print(view.display());
			printMainMenu();
			int choice = getIntInput("Choice: ");
			switch (choice)
			{
			case 1:
				handleChangeFocusDate();
				break;
			case 2:
				handleEntryCreation();
				break;
			case 3:
				handleChangeView();
				break;
			case 4:
				handleSearch();
				break;
			case 5:
				handleAutoSchedule();
				break;
			case 6:
				handleExport();
				break;
			case 7:
				handleSettings();
				break;
			case 8:
				autosave();
				System.out.println("Profile saved. Goodbye!");
				running = false;
				break;
			default:
				System.out.println("Invalid choice - enter 1-8.");
			}
		}
	}

	private void printMainMenu()
	{
		System.out.println("─── Menu ────────────────────────────────");
		System.out.println(" 1. Change Focus Date");
		System.out.println(" 2. Create Entry");
		System.out.println(" 3. Change View  [" + profile.getViewMode() + "]");
		System.out.println(" 4. Search");
		System.out.println(" 5. Auto-Schedule Tasks");
		System.out.println(" 6. Export Calendar");
		System.out.println(" 7. Settings");
		System.out.println(" 8. Quit");
		System.out.println("─────────────────────────────────────────");
	}

	private void handleChangeFocusDate()
	{
		System.out.println("\n-- Change Focus Date --");
		System.out.println("  1. Forward N days  2. Back N days  3. Jump to date  4. Today  0. Cancel");
		int choice = getIntInput("Choice: ");
		switch (choice)
		{
		case 1:
			profile.setFocusDate(profile.getFocusDate().plusDays(getPositiveIntInput("  Days forward: ")));
			break;
		case 2:
			profile.setFocusDate(profile.getFocusDate().minusDays(getPositiveIntInput("  Days back: ")));
			break;
		case 3:
			profile.setFocusDate(getDateInput("  Date (YYYY-MM-DD): "));
			break;
		case 4:
			profile.setFocusDate(LocalDate.now());
			break;
		default:
			return;
		}
		autosave();
		System.out.println("  Focus date: " + profile.getFocusDate());
	}

	private void handleChangeView()
	{
		while (true)
		{
			System.out.println("Modes: day | 3day | week | month");
			String mode = getStringInput("View mode: ").toLowerCase();
			if (mode.equals("day") || mode.equals("3day") || mode.equals("week") || mode.equals("month"))
			{
				profile.setViewMode(mode);
				autosave();
				return;
			}
			System.out.println("Invalid mode - enter one of: day, 3day, week, month.");
		}
	}

	public void handleEntryCreation()
	{
		System.out.println("\n-- Create Entry --");
		System.out.println("  1. Event  2. Task  3. Flag  4. Recurring Event  0. Cancel");
		int type = getIntInput("Entry type: ");
		switch (type)
		{
		case 1:
			createEvent();
			break;
		case 2:
			createTask();
			break;
		case 3:
			createFlag();
			break;
		case 4:
			createRecurringEvent();
			break;
		default:
			System.out.println("Cancelled.");
			break;
		}
	}

	private void createEvent()
	{
		System.out.println("\n  -- Event --");
		String title = getStringInput("  Title: ");
		String desc = getStringInput("  Description: ");
		LocalDate date = getDateInput("  Date (YYYY-MM-DD): ");
		LocalTime[] times = promptStartEnd();
		boolean busy = getBinaryInput("  Busy status - 1. Busy   2. Not Busy", "  Choice: ") == 1;
		Event event = new Event(title, desc, date, times[0], times[1], busy);
		if (!confirmConflicts(event))
		{
			System.out.println("  Event not added.");
			return;
		}
		profile.addEntry(event);
		autosave();
		System.out.println("  Event added.");
	}

	private LocalTime[] promptStartEnd()
	{
		while (true)
		{
			LocalTime start = getTimeInput("  Start time (HH:MM): ");
			LocalTime end = getTimeInput("  End time (HH:MM): ");
			if (end.isAfter(start))
				return new LocalTime[] {start, end};
			System.out.println("  End time must be after start time - re-enter times.");
		}
	}

	private boolean confirmConflicts(Event event)
	{
		ArrayList<Event> conflicts = scheduler.detectConflicts(event);
		if (conflicts.isEmpty())
			return true;
		System.out.println("  WARNING - conflict with:");
		for (Event c : conflicts)
			System.out.printf("    - %s  (%s - %s)%n", c.getTitle(), c.getStartTime(), c.getEndTime());
		System.out.println("  1. Add anyway   2. Cancel");
		return getBinaryInput("", "  Choice: ") == 1;
	}

	private void createTask()
	{
		System.out.println("\n  -- Task --");
		String title = getStringInput("  Title: ");
		String desc = getStringInput("  Description: ");
		int duration = getPositiveIntInput("  Duration (minutes): ");
		LocalDate deadline = getDateInput("  Deadline (YYYY-MM-DD): ");
		int priority = getPriorityInput("  Priority (1 = highest, 3 = lowest): ");
		profile.addEntry(new Task(title, desc, duration, deadline, priority));
		autosave();
		System.out.println("  Task added (unscheduled).");
	}

	private void createFlag()
	{
		System.out.println("\n  -- Flag --");
		String title = getStringInput("  Title: ");
		String desc = getStringInput("  Description: ");
		LocalDate date = getDateInput("  Date (YYYY-MM-DD): ");
		profile.addEntry(new Flag(title, desc, date));
		autosave();
		System.out.println("  Flag added.");
	}

	private void createRecurringEvent()
	{
		System.out.println("\n  -- Recurring Event --");
		String title = getStringInput("  Title: ");
		String desc = getStringInput("  Description: ");
		LocalDate date = getDateInput("  Start date (YYYY-MM-DD): ");
		LocalTime[] times = promptStartEnd();
		boolean busy = getBinaryInput("  Busy status - 1. Busy   2. Not Busy", "  Choice: ") == 1;
		String[] rec = promptRecurrence();
		profile.addEntry(new RecurringEvent(title, desc, date, times[0], times[1], busy, rec[0],
				Integer.parseInt(rec[1]), rec[2]));
		autosave();
		System.out.println("  Recurring event added.");
	}

	private String[] promptRecurrence()
	{
		System.out.println("  Patterns: DAILY | WEEKLY | BIWEEKLY | MONTHLY | YEARLY | CUSTOM");
		String pattern;
		while (true)
		{
			pattern = getStringInput("  Pattern: ").toUpperCase();
			if (isValidPattern(pattern))
				break;
			System.out.println("  Enter one of: DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY, CUSTOM.");
		}
		switch (pattern)
		{
		case "DAILY":
			return new String[] {"DAILY", "1", "DAYS"};
		case "WEEKLY":
			return new String[] {"WEEKLY", "1", "WEEKS"};
		case "BIWEEKLY":
			return new String[] {"BIWEEKLY", "2", "WEEKS"};
		case "MONTHLY":
			return new String[] {"MONTHLY", "1", "MONTHS"};
		case "YEARLY":
			return new String[] {"YEARLY", "1", "YEARS"};
		default:
			System.out.println("  Units: DAYS | WEEKS | MONTHS | YEARS");
			String unit = getValidatedUnit("  Interval unit: ");
			int value = getPositiveIntInput("  Interval value: ");
			return new String[] {"CUSTOM", String.valueOf(value), unit};
		}
	}

	private void handleAutoSchedule()
	{
		System.out.println("\n-- Auto-Schedule Tasks --");
		int unscheduledCount = 0;
		for (Entry e : profile.getEntries())
			if (e instanceof Task && !((Task) e).isScheduled())
				unscheduledCount++;
		if (unscheduledCount == 0)
		{
			System.out.println("  No unscheduled tasks found - nothing to do.");
			return;
		}
		System.out.println("  " + unscheduledCount + " unscheduled task(s) found.");
		System.out.println("  Work window: " + profile.getWorkStart() + " - " + profile.getWorkEnd());
		printNoWorkSummary();
		System.out.println("  1. Keep these constraints   2. Update scheduling constraints");
		if (getIntInput("  Choice: ") == 2)
			updateSchedulingConstraints();
		SchedulingResult result = scheduler.scheduleTasks(LocalDate.now(), promptSortPolicy(), promptPlacementPolicy());
		printScheduleResult(result);
		autosave();
		System.out.println("  Profile saved.");
	}

	private SchedulingPolicy promptSortPolicy()
	{
		System.out.println("\n  Sorting policy:");
		System.out.println("    1. Earliest Deadline First  - minimises missed deadlines");
		System.out.println("    2. Priority First           - highest-priority tasks go first");
		return getIntInput("  Choice [1]: ") == 2 ? new PriorityFirstPolicy() : new EarliestDeadlinePolicy();
	}

	private PlacementPolicy promptPlacementPolicy()
	{
		System.out.println("\n  Placement policy:");
		System.out.println("    1. Deep Work    - prefers the longest uninterrupted free block");
		System.out.println("    2. Spread Out   - distributes tasks across the available window");
		return getIntInput("  Choice [1]: ") == 2 ? new SpreadOutPlacement() : new DeepWorkPlacement();
	}

	private void printScheduleResult(SchedulingResult result)
	{
		if (!result.getScheduled().isEmpty())
		{
			System.out.println("\n  ! Scheduled (" + result.getScheduled().size() + "):");
			for (Task t : result.getScheduled())
				System.out.printf("    - %-30s -> %s at %s%n", t.getTitle(), t.getScheduledDate(),
						t.getScheduledStart());
		}
		if (!result.getUnplaceable().isEmpty())
		{
			System.out.println("\n  X Could not be scheduled (" + result.getUnplaceable().size() + "):");
			for (Task t : result.getUnplaceable())
				System.out.printf("    - %-30s  (deadline: %s, %d min)%n", t.getTitle(), t.getDeadline(),
						t.getDuration());
			System.out.println("  Tip: extend work window, shorten durations, reduce break, or push deadlines.");
		}
		if (result.isComplete())
			System.out.println("\n  All tasks scheduled successfully.");
	}

	private void printNoWorkSummary()
	{
		LocalTime nws = profile.getNoWorkStart();
		LocalTime nwe = profile.getNoWorkEnd();
		System.out.println("  No-work period: " + (nws != null ? nws + " – " + nwe : "none"));
		System.out.println("  Minimum break: " + profile.getMinBreakMinutes() + " min");
	}

	private void updateSchedulingConstraints()
	{
		LocalTime ws = tryTime("    Work-day start (HH:MM, Enter to keep): ");
		LocalTime we = tryTime("    Work-day end   (HH:MM, Enter to keep): ");
		if (ws != null)
			profile.setWorkStart(ws);
		if (we != null)
		{
			if (we.isAfter(profile.getWorkStart()))
				profile.setWorkEnd(we);
			else
				System.out.println("    End must be after start - unchanged.");
		}
		promptNoWorkConstraintsUpdate();
		autosave();
		System.out.println("  Constraints updated.");
	}

	private void promptNoWorkConstraintsUpdate()
	{
		System.out.println("    No-work period (Enter to keep current):");
		LocalTime nws = tryTime("    No-work start (HH:MM, Enter to keep): ");
		if (nws != null)
		{
			LocalTime nwe = tryTime("    No-work end (HH:MM): ");
			if (nwe != null && nwe.isAfter(nws))
			{
				profile.setNoWorkStart(nws);
				profile.setNoWorkEnd(nwe);
			}
			else
				System.out.println("    No-work end must be after start - unchanged.");
		}
		else
		{
			System.out.print("    Type 'clear' to remove no-work period, or Enter to keep: ");
			if (scanner.nextLine().trim().equalsIgnoreCase("clear"))
			{
				profile.setNoWorkStart(null);
				profile.setNoWorkEnd(null);
				System.out.println("    No-work period cleared.");
			}
		}
		Integer mb = tryInt("    Minimum break (minutes, Enter to keep): ");
		if (mb != null && mb >= 0)
			profile.setMinBreakMinutes(mb);
	}

	private void handleExport()
	{
		System.out.println("\n-- Export Calendar --");
		System.out.println("  Format: 1. JSON   2. CSV   3. ICS   0. Cancel");
		int choice = getIntInput("  Choice: ");
		if (choice < 1 || choice > 3)
		{
			System.out.println("  Cancelled.");
			return;
		}
		System.out.println("  Export range (relative to focus date " + profile.getFocusDate() + "):");
		System.out.println("    1. Day   2. 3-Day   3. Week   4. Month");
		int rangeChoice = getIntInput("  Range: ");
		LocalDate[] range = buildExportRange(rangeChoice);
		String filename = promptExportFilename(choice);
		System.out.println("  " + buildExporter(choice).export(range[0], range[1], filename));
	}

	private LocalDate[] buildExportRange(int rangeChoice)
	{
		LocalDate focus = profile.getFocusDate();
		switch (rangeChoice)
		{
		case 2:
			return new LocalDate[] {focus, focus.plusDays(2)};
		case 3:
			return new LocalDate[] {focus, focus.plusDays(6)};
		case 4:
			LocalDate first = focus.withDayOfMonth(1);
			return new LocalDate[] {first, focus.withDayOfMonth(focus.lengthOfMonth())};
		default:
			return new LocalDate[] {focus, focus};
		}
	}

	private void handleSettings()
	{
		System.out.println("\n-- Settings --");
		System.out.println("  1. Work hours   2. No-work period / break   3. View mode   0. Back");
		int choice = getIntInput("  Choice: ");
		switch (choice)
		{
		case 1:
			updateWorkHours();
			break;
		case 2:
			promptNoWorkConstraintsUpdate();
			autosave();
			System.out.println("  Settings saved.");
			break;
		case 3:
			handleChangeView();
			break;
		default:
			break;
		}
	}

	private void updateWorkHours()
	{
		LocalTime ws = tryTime("  Work-day start (HH:MM, Enter to keep): ");
		LocalTime we = tryTime("  Work-day end   (HH:MM, Enter to keep): ");
		if (ws != null)
			profile.setWorkStart(ws);
		if (we != null)
		{
			if (we.isAfter(profile.getWorkStart()))
				profile.setWorkEnd(we);
			else
				System.out.println("  End must be after start - unchanged.");
		}
		autosave();
		System.out.println("  Work hours updated.");
	}

	private Exporter buildExporter(int choice)
	{
		if (choice == 1)
			return new JSONExporter(profile);
		if (choice == 2)
			return new CSVExporter(profile);
		return new ICSExporter(profile);
	}

	private String promptExportFilename(int choice)
	{
		String ext = choice == 1 ? ".json" : (choice == 2 ? ".csv" : ".ics");
		System.out.print("  Output filename [export" + ext + "]: ");
		String filename = scanner.nextLine().trim();
		return filename.isEmpty() ? "export" + ext : filename;
	}

	private void handleSearch()
	{
		String[] criteria = promptSearchCriteria();
		if (criteria == null)
			return;
		ArrayList<Entry> results = profile.searchEntries(criteria[0], criteria[1]);
		if (results.isEmpty())
		{
			System.out.println("  No entries found.");
			return;
		}
		Entry selected = selectFromResults(results);
		if (selected != null)
			handleViewEdit(selected);
	}

	private String[] promptSearchCriteria()
	{
		System.out.println("\n-- Search --");
		System.out.println("  1. Title   2. Date (YYYY-MM-DD)   3. Type   4. Deadline (YYYY-MM-DD)   0. Cancel");
		int choice = getIntInput("  Search by: ");
		switch (choice)
		{
		case 1:
			return new String[] {"title", getStringInput("  Title contains: ")};
		case 2:
			return new String[] {"date", getDateInput("  Date (YYYY-MM-DD): ").toString()};
		case 3:
			return new String[] {"type", getStringInput("  Type: ")};
		case 4:
			return new String[] {"deadline", getDateInput("  Deadline (YYYY-MM-DD): ").toString()};
		default:
			return null;
		}
	}

	private Entry selectFromResults(ArrayList<Entry> results)
	{
		System.out.println("\n  Results (" + results.size() + "):");
		for (int i = 0; i < results.size(); i++)
		{
			String summary = results.get(i).display().lines().findFirst().orElse(results.get(i).getTitle());
			System.out.println("  " + (i + 1) + ". " + summary);
		}
		if (results.size() == 1)
			return results.get(0);
		int sel = getIntInput("\n  Select entry (0 to cancel): ");
		return (sel >= 1 && sel <= results.size()) ? results.get(sel - 1) : null;
	}

	private void handleViewEdit(Entry entry)
	{
		System.out.println("\n" + entry.display());
		System.out.println("  1. Edit   2. Delete   0. Back");
		int choice = getIntInput("  Choice: ");
		if (choice == 1)
			editEntry(entry);
		else if (choice == 2)
			deleteEntry(entry);
	}

	private void editEntry(Entry entry)
	{
		if (entry instanceof RecurringEvent)
			editRecurringEvent((RecurringEvent) entry);
		else if (entry instanceof Event)
			editEvent((Event) entry);
		else if (entry instanceof Task)
			editTask((Task) entry);
		else if (entry instanceof Flag)
			editFlag((Flag) entry);
	}

	private void editEvent(Event ev)
	{
		System.out.println("\n-- Edit Event -- (press Enter to keep current value)");
		String title = tryString("  Title [" + ev.getTitle() + "]: ");
		String desc = tryString("  Description [" + ev.getDescription() + "]: ");
		LocalDate date = tryDate("  Date [" + ev.getDate() + "]: ");
		LocalTime start = tryTime("  Start time [" + ev.getStartTime() + "]: ");
		LocalTime end = tryTime("  End time [" + ev.getEndTime() + "]: ");
		System.out.print("  Busy [" + (ev.isBusy() ? "Busy" : "Free") + "]  1=Busy  2=Free  (Enter to keep): ");
		String busyIn = scanner.nextLine().trim();
		LocalTime newStart = start != null ? start : ev.getStartTime();
		LocalTime newEnd = end != null ? end : ev.getEndTime();
		if (!newEnd.isAfter(newStart))
		{
			System.out.println("  End must be after start - cancelled.");
			return;
		}
		applyEventEdits(ev, title, desc, date, newStart, newEnd, busyIn);
		autosave();
		System.out.println("  Event updated.");
	}

	private void applyEventEdits(Event ev, String title, String desc, LocalDate date, LocalTime newStart,
			LocalTime newEnd, String busyIn)
	{
		if (title != null)
			ev.setTitle(title);
		if (desc != null)
			ev.setDescription(desc);
		if (date != null)
			ev.setDate(date);
		ev.setStartTime(newStart);
		ev.setEndTime(newEnd);
		if (busyIn.equals("1"))
			ev.setBusyStatus(true);
		else if (busyIn.equals("2"))
			ev.setBusyStatus(false);
	}

	private void editRecurringEvent(RecurringEvent re)
	{
		System.out.println("\n-- Edit Recurring Event -- (press Enter to keep current value)");
		String title = tryString("  Title [" + re.getTitle() + "]: ");
		String desc = tryString("  Description [" + re.getDescription() + "]: ");
		LocalDate date = tryDate("  Start date [" + re.getDate() + "]: ");
		LocalTime start = tryTime("  Start time [" + re.getStartTime() + "]: ");
		LocalTime end = tryTime("  End time [" + re.getEndTime() + "]: ");
		System.out.print("  Busy [" + (re.isBusy() ? "Busy" : "Free") + "]  1=Busy  2=Free  (Enter to keep): ");
		String busyIn = scanner.nextLine().trim();
		String pattern = tryValidatedPattern(re.getRecurrencePattern());
		Integer value = tryInt("  Interval value [" + re.getIntervalValue() + "]: ");
		String unit = tryString("  Interval unit [" + re.getIntervalUnit() + "]: ");
		LocalTime newStart = start != null ? start : re.getStartTime();
		LocalTime newEnd = end != null ? end : re.getEndTime();
		if (!newEnd.isAfter(newStart))
		{
			System.out.println("  End must be after start - cancelled.");
			return;
		}
		applyRecurringEdits(re, title, desc, date, newStart, newEnd, busyIn, pattern, value, unit);
		autosave();
		System.out.println("  Recurring event updated.");
	}

	private void applyRecurringEdits(RecurringEvent re, String title, String desc, LocalDate date, LocalTime newStart,
			LocalTime newEnd, String busyIn, String pattern, Integer value, String unit)
	{
		if (title != null)
			re.setTitle(title);
		if (desc != null)
			re.setDescription(desc);
		if (date != null)
			re.setDate(date);
		re.setStartTime(newStart);
		re.setEndTime(newEnd);
		if (busyIn.equals("1"))
			re.setBusyStatus(true);
		else if (busyIn.equals("2"))
			re.setBusyStatus(false);
		if (pattern != null)
			re.setRecurrencePattern(pattern);
		if (value != null && value > 0)
			re.setIntervalValue(value);
		if (unit != null)
			re.setIntervalUnit(unit.toUpperCase());
	}

	private void editTask(Task t)
	{
		System.out.println("\n-- Edit Task -- (press Enter to keep current value)");
		String title = tryString("  Title [" + t.getTitle() + "]: ");
		String desc = tryString("  Description [" + t.getDescription() + "]: ");
		Integer duration = tryInt("  Duration minutes [" + t.getDuration() + "]: ");
		LocalDate deadline = tryDate("  Deadline [" + t.getDeadline() + "]: ");
		Integer priority = tryInt("  Priority 1-3 [" + t.getPriority() + "]: ");
		if (title != null)
			t.setTitle(title);
		if (desc != null)
			t.setDescription(desc);
		if (duration != null && duration > 0)
			t.setDuration(duration);
		else if (duration != null)
			System.out.println("  Duration must be positive - unchanged.");
		if (deadline != null)
			t.setDeadline(deadline);
		if (priority != null)
		{
			if (priority >= 1 && priority <= 3)
				t.setPriority(priority);
			else
				System.out.println("  Priority must be 1-3 - unchanged.");
		}
		autosave();
		System.out.println("  Task updated.");
	}

	private void editFlag(Flag f)
	{
		System.out.println("\n-- Edit Flag -- (press Enter to keep current value)");
		String title = tryString("  Title [" + f.getTitle() + "]: ");
		String desc = tryString("  Description [" + f.getDescription() + "]: ");
		LocalDate date = tryDate("  Date [" + f.getDate() + "]: ");
		if (title != null)
			f.setTitle(title);
		if (desc != null)
			f.setDescription(desc);
		if (date != null)
			f.setDate(date);
		autosave();
		System.out.println("  Flag updated.");
	}

	private void deleteEntry(Entry entry)
	{
		System.out.print("  Delete \"" + entry.getTitle() + "\"? (y/N): ");
		if (scanner.nextLine().trim().equalsIgnoreCase("y"))
		{
			profile.removeEntry(entry);
			autosave();
			System.out.println("  Entry deleted.");
		}
		else
		{
			System.out.println("  Cancelled.");
		}
	}

	private String tryString(String prompt)
	{
		System.out.print(prompt);
		String s = scanner.nextLine();
		return s.isEmpty() ? null : s;
	}

	private LocalDate tryDate(String prompt)
	{
		while (true)
		{
			System.out.print(prompt);
			String s = scanner.nextLine().trim();
			if (s.isEmpty())
				return null;
			try
			{
				return LocalDate.parse(s);
			}
			catch (DateTimeParseException e)
			{
				System.out.println("  Use format YYYY-MM-DD.");
			}
		}
	}

	private LocalTime tryTime(String prompt)
	{
		while (true)
		{
			System.out.print(prompt);
			String s = scanner.nextLine().trim();
			if (s.isEmpty())
				return null;
			try
			{
				return LocalTime.parse(s);
			}
			catch (DateTimeParseException e)
			{
				System.out.println("  Use format HH:MM.");
			}
		}
	}

	private Integer tryInt(String prompt)
	{
		while (true)
		{
			System.out.print(prompt);
			String s = scanner.nextLine().trim();
			if (s.isEmpty())
				return null;
			try
			{
				return Integer.parseInt(s);
			}
			catch (NumberFormatException e)
			{
				System.out.println("  Enter a whole number.");
			}
		}
	}

	private String tryValidatedPattern(String current)
	{
		System.out.println("  Valid patterns: DAILY | WEEKLY | BIWEEKLY | MONTHLY | YEARLY | CUSTOM");
		while (true)
		{
			System.out.print("  Pattern [" + current + "]: ");
			String s = scanner.nextLine().trim().toUpperCase();
			if (s.isEmpty())
				return null;
			if (isValidPattern(s))
				return s;
			System.out.println("  Enter a valid pattern or press Enter to keep current.");
		}
	}

	private boolean isValidPattern(String p)
	{
		return p.equals("DAILY") || p.equals("WEEKLY") || p.equals("BIWEEKLY") || p.equals("MONTHLY")
				|| p.equals("YEARLY") || p.equals("CUSTOM");
	}

	private int getPriorityInput(String prompt)
	{
		while (true)
		{
			int p = getIntInput(prompt);
			if (p >= 1 && p <= 3)
				return p;
			System.out.println("  Priority must be 1, 2, or 3.");
		}
	}

	private int getPositiveIntInput(String prompt)
	{
		while (true)
		{
			int v = getIntInput(prompt);
			if (v > 0)
				return v;
			System.out.println("  Enter a positive number (greater than 0).");
		}
	}

	private int getBinaryInput(String header, String prompt)
	{
		if (!header.isEmpty())
			System.out.println(header);
		while (true)
		{
			int v = getIntInput(prompt);
			if (v == 1 || v == 2)
				return v;
			System.out.println("  Enter 1 or 2.");
		}
	}

	private String getValidatedUnit(String prompt)
	{
		while (true)
		{
			String u = getStringInput(prompt).toUpperCase();
			if (u.equals("DAYS") || u.equals("WEEKS") || u.equals("MONTHS") || u.equals("YEARS"))
				return u;
			System.out.println("  Enter one of: DAYS, WEEKS, MONTHS, YEARS.");
		}
	}

	public int getIntInput(String prompt)
	{
		while (true)
		{
			System.out.print(prompt);
			try
			{
				return Integer.parseInt(scanner.nextLine().trim());
			}
			catch (NumberFormatException e)
			{
				System.out.println("  Enter a whole number.");
			}
		}
	}

	public String getStringInput(String prompt)
	{
		System.out.print(prompt);
		return scanner.nextLine();
	}

	public LocalDate getDateInput(String prompt)
	{
		while (true)
		{
			System.out.print(prompt);
			try
			{
				return LocalDate.parse(scanner.nextLine().trim());
			}
			catch (DateTimeParseException e)
			{
				System.out.println("  Use format YYYY-MM-DD.");
			}
		}
	}

	public LocalTime getTimeInput(String prompt)
	{
		while (true)
		{
			System.out.print(prompt);
			try
			{
				return LocalTime.parse(scanner.nextLine().trim());
			}
			catch (DateTimeParseException e)
			{
				System.out.println("  Use format HH:MM.");
			}
		}
	}
}
