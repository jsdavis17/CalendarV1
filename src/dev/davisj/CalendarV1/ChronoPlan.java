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

	public void run()
	{
		System.out.println("╔══════════════════════════════╗");
		System.out.println("║      Welcome to ChronoPlan   ║");
		System.out.println("╚══════════════════════════════╝");

		ArrayList<String> profiles = profileManager.listProfiles();

		if (profiles.isEmpty())
		{
			System.out.println("No profiles found. Let's create one.");
			String name = getStringInput("Profile name: ");
			profile = profileManager.createProfile(name);
			System.out.println("Profile \"" + name + "\" created.");
			handleNewProfileSetup();
		}
		else
		{
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

		view = new CalendarView(profile);
		scheduler = new Scheduler(profile);
		handleMenu();
	}

	private void handleNewProfileSetup()
	{
		System.out.println("\nConfigure profile (Enter to keep default):");
		System.out.print("  Calendar view (day / 3day / week / month) [week]: ");
		String v = scanner.nextLine().trim().toLowerCase();
		if (!v.isEmpty() && (v.equals("day") || v.equals("3day") || v.equals("week") || v.equals("month")))
			profile.setViewMode(v);
		autosave();
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
			view.display();
			System.out.println();
			System.out.println("─── Menu ────────────────────────────────");
			System.out.println(" 1. Change Focus Date");
			System.out.println(" 2. Create Entry");
			System.out.println(" 3. Change View  [" + profile.getViewMode() + "]");
			System.out.println(" 4. Search");
			System.out.println(" 5. Quit");
			System.out.println("─────────────────────────────────────────");

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
				autosave();
				System.out.println("Profile saved. Goodbye!");
				running = false;
				break;
			default:
				System.out.println("Invalid choice.");
			}
		}
	}

	private void handleChangeFocusDate()
	{
		System.out.println("\n── Change Focus Date ──");
		System.out.println("  1. Forward N days");
		System.out.println("  2. Back N days");
		System.out.println("  3. Jump to specific date");
		System.out.println("  4. Jump to today");
		System.out.println("  0. Cancel");

		int choice = getIntInput("Choice: ");
		switch (choice)
		{
		case 1:
			int fwd = getIntInput("  Days forward: ");
			profile.setFocusDate(profile.getFocusDate().plusDays(fwd));
			break;
		case 2:
			int bck = getIntInput("  Days back: ");
			profile.setFocusDate(profile.getFocusDate().minusDays(bck));
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
		System.out.println("Modes: day | 3day | week | month");
		String mode = getStringInput("View mode: ").toLowerCase();
		if (mode.equals("day") || mode.equals("3day") || mode.equals("week") || mode.equals("month"))
		{
			profile.setViewMode(mode);
			autosave();
		}
		else
		{
			System.out.println("Invalid mode — unchanged.");
		}
	}

	public void handleEntryCreation()
	{
		System.out.println("\n── Create Entry ──");
		System.out.println("  1. Event");
		System.out.println("  2. Task");
		System.out.println("  3. Flag");
		System.out.println("  4. Recurring Event");
		System.out.println("  0. Cancel");

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
			return;
		}
	}

	private void createEvent()
	{
		System.out.println("\n  -- Event --");
		String title = getStringInput("  Title: ");
		String desc = getStringInput("  Description: ");
		LocalDate date = getDateInput("  Date (YYYY-MM-DD): ");
		LocalTime start = getTimeInput("  Start time (HH:MM): ");
		LocalTime end = getTimeInput("  End time (HH:MM): ");

		if (!end.isAfter(start))
		{
			System.out.println("  End time must be after start time.");
			return;
		}

		System.out.println("  Busy status — 1. Busy   2. Not Busy");
		boolean busy = getIntInput("  Choice: ") == 1;
		Event event = new Event(title, desc, date, start, end, busy);

		ArrayList<Event> conflicts = scheduler.detectConflicts(event);
		if (!conflicts.isEmpty())
		{
			System.out.println("  WARNING — conflict with:");
			for (Event c : conflicts)
				System.out.printf("    • %s  (%s – %s)%n", c.getTitle(), c.getStartTime(), c.getEndTime());
			System.out.println("  1. Add anyway   2. Cancel");
			if (getIntInput("  Choice: ") != 1)
			{
				System.out.println("  Event not added.");
				return;
			}
		}

		profile.addEntry(event);
		autosave();
		System.out.println("  Event added.");
	}

	private void createTask()
	{
		System.out.println("\n  -- Task --");
		String title = getStringInput("  Title: ");
		String desc = getStringInput("  Description: ");
		int duration = getIntInput("  Duration (minutes): ");
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
		LocalTime start = getTimeInput("  Start time (HH:MM): ");
		LocalTime end = getTimeInput("  End time (HH:MM): ");

		if (!end.isAfter(start))
		{
			System.out.println("  End must be after start.");
			return;
		}

		System.out.println("  Busy status — 1. Busy   2. Not Busy");
		boolean busy = getIntInput("  Choice: ") == 1;

		System.out.println("  Patterns: DAILY | WEEKLY | BIWEEKLY | MONTHLY | YEARLY | CUSTOM");
		String pattern = getStringInput("  Pattern: ").toUpperCase();

		int value;
		String unit;
		switch (pattern)
		{
		case "DAILY":
			value = 1;
			unit = "DAYS";
			break;
		case "WEEKLY":
			value = 1;
			unit = "WEEKS";
			break;
		case "BIWEEKLY":
			value = 2;
			unit = "WEEKS";
			break;
		case "MONTHLY":
			value = 1;
			unit = "MONTHS";
			break;
		case "YEARLY":
			value = 1;
			unit = "YEARS";
			break;
		default:
			System.out.println("  Units: DAYS | WEEKS | MONTHS | YEARS");
			unit = getStringInput("  Interval unit: ").toUpperCase();
			value = getIntInput("  Interval value: ");
			pattern = "CUSTOM";
			break;
		}

		profile.addEntry(new RecurringEvent(title, desc, date, start, end, busy, pattern, value, unit));
		autosave();
		System.out.println("  Recurring event added.");
	}

	private void handleSearch()
	{
		System.out.println("\n── Search ──");
		System.out.println("  1. Title");
		System.out.println("  2. Date         (YYYY-MM-DD)");
		System.out.println("  3. Type         (event / task / flag / recurring)");
		System.out.println("  4. Deadline     (YYYY-MM-DD, tasks only)");
		System.out.println("  0. Cancel");

		int choice = getIntInput("  Search by: ");
		String field, val;
		switch (choice)
		{
		case 1:
			field = "title";
			val = getStringInput("  Title contains: ");
			break;
		case 2:
			field = "date";
			val = getDateInput("  Date (YYYY-MM-DD): ").toString();
			break;
		case 3:
			field = "type";
			val = getStringInput("  Type: ");
			break;
		case 4:
			field = "deadline";
			val = getDateInput("  Deadline (YYYY-MM-DD): ").toString();
			break;
		default:
			return;
		}

		ArrayList<Entry> results = profile.searchEntries(field, val);
		if (results.isEmpty())
		{
			System.out.println("  No entries found.");
			return;
		}

		System.out.println("\n  Results (" + results.size() + "):");
		for (int i = 0; i < results.size(); i++)
		{
			String summary = results.get(i).display().lines().findFirst().orElse(results.get(i).getTitle());
			System.out.println("  " + (i + 1) + ". " + summary);
		}

		if (results.size() == 1)
		{
			handleViewEdit(results.get(0));
			return;
		}

		int sel = getIntInput("\n  Select entry (0 to cancel): ");
		if (sel >= 1 && sel <= results.size())
			handleViewEdit(results.get(sel - 1));
	}

	private void handleViewEdit(Entry entry)
	{
		System.out.println("\n" + entry.display());
		System.out.println("  1. Edit");
		System.out.println("  2. Delete");
		System.out.println("  0. Back");

		int choice = getIntInput("  Choice: ");
		switch (choice)
		{
		case 1:
			editEntry(entry);
			break;
		case 2:
			deleteEntry(entry);
			break;
		}
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
		System.out.println("\n── Edit Event ── (press Enter to keep current value)");

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
			System.out.println("  End time must be after start — edit cancelled.");
			return;
		}

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

		autosave();
		System.out.println("  Event updated.");
	}

	private void editRecurringEvent(RecurringEvent re)
	{
		System.out.println("\n── Edit Recurring Event ── (press Enter to keep current value)");

		String title = tryString("  Title [" + re.getTitle() + "]: ");
		String desc = tryString("  Description [" + re.getDescription() + "]: ");
		LocalDate date = tryDate("  Start date [" + re.getDate() + "]: ");
		LocalTime start = tryTime("  Start time [" + re.getStartTime() + "]: ");
		LocalTime end = tryTime("  End time [" + re.getEndTime() + "]: ");

		System.out.print("  Busy [" + (re.isBusy() ? "Busy" : "Free") + "]  1=Busy  2=Free  (Enter to keep): ");
		String busyIn = scanner.nextLine().trim();

		String pattern = tryString("  Pattern [" + re.getRecurrencePattern() + "]: ");
		int value = tryInt("  Interval value [" + re.getIntervalValue() + "]: ");
		String unit = tryString("  Interval unit [" + re.getIntervalUnit() + "]: ");

		LocalTime newStart = start != null ? start : re.getStartTime();
		LocalTime newEnd = end != null ? end : re.getEndTime();
		if (!newEnd.isAfter(newStart))
		{
			System.out.println("  End time must be after start — edit cancelled.");
			return;
		}

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
		if (value != -1)
			re.setIntervalValue(value);
		if (unit != null)
			re.setIntervalUnit(unit.toUpperCase());

		autosave();
		System.out.println("  Recurring event updated.");
	}

	private void editTask(Task t)
	{
		System.out.println("\n── Edit Task ── (press Enter to keep current value)");

		String title = tryString("  Title [" + t.getTitle() + "]: ");
		String desc = tryString("  Description [" + t.getDescription() + "]: ");
		int duration = tryInt("  Duration minutes [" + t.getDuration() + "]: ");
		LocalDate deadline = tryDate("  Deadline [" + t.getDeadline() + "]: ");
		int priority = tryInt("  Priority 1-3 [" + t.getPriority() + "]: ");

		if (title != null)
			t.setTitle(title);
		if (desc != null)
			t.setDescription(desc);
		if (duration != -1)
			t.setDuration(duration);
		if (deadline != null)
			t.setDeadline(deadline);
		if (priority != -1)
		{
			if (priority >= 1 && priority <= 3)
				t.setPriority(priority);
			else
				System.out.println("  Priority must be 1-3 — unchanged.");
		}

		autosave();
		System.out.println("  Task updated.");
	}

	private void editFlag(Flag f)
	{
		System.out.println("\n── Edit Flag ── (press Enter to keep current value)");

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

	private int tryInt(String prompt)
	{
		while (true)
		{
			System.out.print(prompt);
			String s = scanner.nextLine().trim();
			if (s.isEmpty())
				return -1;
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
