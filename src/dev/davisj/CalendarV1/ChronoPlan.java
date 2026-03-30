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
		} else
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
			} else
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
			System.out.println(" 4. Quit");
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
		} else
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
			} catch (NumberFormatException e)
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
			} catch (DateTimeParseException e)
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
			} catch (DateTimeParseException e)
			{
				System.out.println("  Use format HH:MM.");
			}
		}
	}
}
