package dev.davisj.CalendarV1;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class CalendarManager
{
	public static void main(String[] args)
	{
		CalendarManager manager = new CalendarManager();
		manager.runApp();
	}

	public CalendarManager()
	{}
	
	public void runApp()
	{
		ArrayList<Entry> entries = new ArrayList<>();
		Scanner sc = new Scanner(System.in);
		DatabaseManager db = new DatabaseManager("src/dev/davisj/CalendarV1/entries.txt");
		
		entries.add(new Event(UUID.randomUUID(), "SAVE TEST 1", "SAVE TEST 1", Instant.now(), Instant.now().plusMillis(1)));
		entries.add(new Task(UUID.randomUUID(), "SAVE TEST 2", "SAVE TEST 2", Instant.now(), 120));

		db.save(entries);
		db.load(entries);
		
		sc.close();
	}
}
