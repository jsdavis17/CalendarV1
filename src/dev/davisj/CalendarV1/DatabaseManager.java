package dev.davisj.CalendarV1;

import java.io.File;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Scanner;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager
{
	String filepath;
	
	public DatabaseManager(String filepath)
	{
		this.filepath = filepath;
	}
	
	public void load(ArrayList<Entry> entries)
	{
		Scanner fsc;
		
		try
		{
			File file = new File(filepath);
			fsc = new Scanner(file);
		}
		catch (Exception e)
		{
			System.out.println("Error connecting to file: " + e.getMessage());
			return;
		}
		
		try
		{
			while(fsc.hasNextLine())
			{
				String line = fsc.nextLine().trim();
				System.out.println(line); //
				String[] parts = line.split("\\|");
				
				String type = parts[0];
				
				Map<String, String> partMap = new HashMap<>();
				
				for(int i = 1; i < parts.length; i++)
				{
					String token = parts[i];
					int eq = token.indexOf("=");
					
					String key = token.substring(0, eq).trim();
					String value = token.substring(eq + 1);
					
					partMap.put(key, value);
				}
				
				String idString = partMap.get("id");
				UUID id = UUID.fromString(idString);
				System.out.println(idString); //
				
				String title = URLDecoder.decode(partMap.getOrDefault("title", ""), StandardCharsets.UTF_8);
				System.out.println(title); //
				String description  = URLDecoder.decode(partMap.getOrDefault("description", ""), StandardCharsets.UTF_8);
				System.out.println(description); //
				
				Entry entry;
				
				switch(type)
				{
				case "EVENT":
					String startString = partMap.get("start");
					System.out.println(startString); //
					String endString = partMap.get("end");
					System.out.println(endString); //
					
					long startLong = Long.parseLong(startString);
					long endLong = Long.parseLong(endString);
					
					if(endLong <= startLong)
					{
						throw new IllegalArgumentException("EVENT end <= start");
					}
					
					entry = new Event(id, title, description, Instant.ofEpochMilli(startLong), Instant.ofEpochMilli(endLong));
					break;
				case "TASK":
					String dueString = partMap.get("due");
					System.out.println(dueString); //
					String minString = partMap.get("minutes");
					System.out.println(minString); //
					
					long dueLong = Long.parseLong(dueString);
					int minInt = Integer.parseInt(minString);
					
					entry = new Task(id, title, description, Instant.ofEpochMilli(dueLong), minInt);
					break;
				default:
					entry = new Event(UUID.randomUUID(), "TEST", "TEST", Instant.now(), Instant.now().plusMillis(1));
				}
				
				entries.add(entry);
			}
		}
		catch (Exception e)
		{
			System.out.println("Error loading from file: " + e.getMessage());
		}
		
		fsc.close();
	}
	
	public void save(ArrayList<Entry> entries)
	{
		try
		{
			PrintWriter pw = new PrintWriter(filepath);
			
			for(Entry e : entries)
			{
				pw.println(e.toRecord());
			}
			
			pw.close();
		}
		catch (Exception e)
		{
			System.out.println("Error saving entries: " + e.getMessage());
		}
	}
}
