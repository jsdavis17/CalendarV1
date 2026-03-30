package dev.davisj.CalendarV1;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class ProfileManager
{
	private static final String INDEX_FILE = "profiles/index.txt";
	private String profileDirectory;

	public ProfileManager()
	{
		this.profileDirectory = "profiles";
		new File(profileDirectory).mkdirs();
	}

	public ArrayList<String> listProfiles()
	{
		ArrayList<String> names = new ArrayList<>();
		File index = new File(INDEX_FILE);
		if (!index.exists())
			return names;
		try (Scanner sc = new Scanner(index))
		{
			while (sc.hasNextLine())
			{
				String name = sc.nextLine().trim();
				if (!name.isEmpty())
					names.add(name);
			}
		} catch (Exception e)
		{
			System.out.println("Error reading profile index: " + e.getMessage());
		}
		return names;
	}

	public boolean profileExists(String name)
	{
		return listProfiles().contains(name);
	}

	public Profile createProfile(String name)
	{
		if (!profileExists(name))
		{
			try (PrintWriter pw = new PrintWriter(new FileWriter(INDEX_FILE, true)))
			{
				pw.println(name);
			} catch (Exception e)
			{
				System.out.println("Error updating profile index: " + e.getMessage());
			}
		}
		Profile profile = new Profile(name);
		saveProfile(profile);
		return profile;
	}

	public void saveProfile(Profile profile)
	{
		String filePath = profileDirectory + "/" + profile.getProfileName() + ".txt";
		try (PrintWriter pw = new PrintWriter(filePath))
		{
			String[] s = profile.getSettings();
			pw.println("SETTINGS|viewMode=" + s[0] + "|focusDate=" + s[1]);
			for (Entry e : profile.getEntries())
				pw.println(e.toRecord());
		} catch (Exception e)
		{
			System.out.println("Error saving profile: " + e.getMessage());
		}
	}

	public Profile loadProfile(String name)
	{
		Profile profile = new Profile(name);
		String filePath = profileDirectory + "/" + name + ".txt";
		File file = new File(filePath);
		if (!file.exists())
		{
			System.out.println("Profile file not found: " + filePath);
			return profile;
		}
		try (Scanner sc = new Scanner(file))
		{
			while (sc.hasNextLine())
			{
				String line = sc.nextLine().trim();
				if (line.isEmpty())
					continue;
				String[] parts = line.split("\\|");
				String type = parts[0];
				if (type.equals("SETTINGS"))
				{
					Map<String, String> m = parseFields(parts, 1);
					profile.setSettings(new String[]
					{ m.getOrDefault("viewMode", "week"), m.getOrDefault("focusDate", LocalDate.now().toString()) });
				} else
				{
					Entry entry = parseEntry(type, parts);
					if (entry != null)
						profile.addEntry(entry);
				}
			}
		} catch (Exception e)
		{
			System.out.println("Error loading profile: " + e.getMessage());
		}
		return profile;
	}

	private Map<String, String> parseFields(String[] parts, int start)
	{
		Map<String, String> map = new HashMap<>();
		for (int i = start; i < parts.length; i++)
		{
			int eq = parts[i].indexOf('=');
			if (eq < 0)
				continue;
			map.put(parts[i].substring(0, eq).trim(), parts[i].substring(eq + 1));
		}
		return map;
	}

	private Entry parseEntry(String type, String[] parts)
	{
		try
		{
			Map<String, String> f = parseFields(parts, 1);
			UUID id = UUID.fromString(f.get("id"));
			String title = URLDecoder.decode(f.getOrDefault("title", ""), StandardCharsets.UTF_8);
			String desc = URLDecoder.decode(f.getOrDefault("description", ""), StandardCharsets.UTF_8);
			switch (type)
			{
			case "EVENT":
			{
				LocalDate date = LocalDate.parse(f.get("date"));
				LocalTime start = LocalTime.parse(f.get("start"));
				LocalTime end = LocalTime.parse(f.get("end"));
				boolean busy = Boolean.parseBoolean(f.get("busy"));
				return new Event(id, title, desc, date, start, end, busy);
			}
			case "TASK":
			{
				int duration = Integer.parseInt(f.get("duration"));
				LocalDate deadline = LocalDate.parse(f.get("deadline"));
				int priority = Integer.parseInt(f.get("priority"));
				String sd = f.getOrDefault("scheduledDate", "");
				String ss = f.getOrDefault("scheduledStart", "");
				LocalDate schedDate = sd.isEmpty() ? null : LocalDate.parse(sd);
				LocalTime schedStart = ss.isEmpty() ? null : LocalTime.parse(ss);
				return new Task(id, title, desc, duration, deadline, priority, schedDate, schedStart);
			}
			case "FLAG":
			{
				LocalDate date = LocalDate.parse(f.get("date"));
				return new Flag(id, title, desc, date);
			}
			case "RECURRING":
			{
				LocalDate date = LocalDate.parse(f.get("date"));
				LocalTime start = LocalTime.parse(f.get("start"));
				LocalTime end = LocalTime.parse(f.get("end"));
				boolean busy = Boolean.parseBoolean(f.get("busy"));
				String pattern = URLDecoder.decode(f.get("pattern"), StandardCharsets.UTF_8);
				int value = Integer.parseInt(f.get("value"));
				String unit = URLDecoder.decode(f.get("unit"), StandardCharsets.UTF_8);
				return new RecurringEvent(id, title, desc, date, start, end, busy, pattern, value, unit);
			}
			default:
				System.out.println("Unknown entry type: " + type);
				return null;
			}
		} catch (Exception e)
		{
			System.out.println("Error parsing entry (" + type + "): " + e.getMessage());
			return null;
		}
	}
}
