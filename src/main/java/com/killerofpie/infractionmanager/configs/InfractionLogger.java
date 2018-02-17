package com.killerofpie.infractionmanager.configs;

import com.killerofpie.infractionmanager.InfractionManager;
import com.killerofpie.infractionmanager.objects.Infraction;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class InfractionLogger {

	InfractionManager plugin;
	LocalDate date = LocalDate.now();
	File file, folder;

	public InfractionLogger(InfractionManager plugin) {
		this.plugin = plugin;
		folder = new File(plugin.getDataFolder() + File.separator + "infraction-logs");
		file = new File(folder, date.toString() + ".log");

		load();
	}

	private void load() {
		try {
			if (!folder.isDirectory()) {
				folder.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not create Log file! Logging may not work!", e);
		}
	}

	private void checkDate() {
		if (LocalDate.now().isAfter(date)) {
			date = LocalDate.now();
			file = new File(folder, date.toString() + ".log");
			load();
		}
	}

	public void logCreate(Infraction infraction) {
		StringBuilder sb = new StringBuilder();
		String senderName = Bukkit.getOfflinePlayer(infraction.getSenderUUID()).getName();
		int lastNameLength = 0;

		for (UUID uuid : infraction.getPlayers()) {
			sb.append(Bukkit.getOfflinePlayer(uuid).getName());

			if (infraction.getPlayers().length == 2
					&& sb.length() == Bukkit.getOfflinePlayer(uuid).getName().length()) {
				sb.append(" and ");
			} else if (infraction.getPlayers().length > 2) {
				lastNameLength = Bukkit.getOfflinePlayer(uuid).getName().length();
				sb.append(", ");
			}

		}

		if (lastNameLength != 0) {
			sb.insert(sb.length() - lastNameLength, " and");
		}

		sb.append(" has/have been given an infraction by ");
		sb.append(senderName);
		sb.append(" for ");
		sb.append(infraction.getType().getName());
		sb.append(" because ");
		sb.append(infraction.getReason());
		sb.append(" <- End Of Infraction");

		log(sb.toString());

	}

	public void logRemove(String sender, String playername, Infraction infraction) {
		StringBuilder sb = new StringBuilder();

		sb.append(playername);
		sb.append(" has had the following infraction removed by ");
		sb.append(sender);
		sb.append("\n                          ");
		sb.append(infraction.getType().getName());
		sb.append(" for ");
		sb.append(infraction.getReason());
		sb.append(" <- End Of Removal");

		log(sb.toString());
	}

	public void logReset(String sender, Set<String> players) {
		StringBuilder sb = new StringBuilder();
		int lastNameLength = 0;

		for (String player : players) {
			sb.append(player);

			if (players.size() == 2 && sb.length() == player.length()) {
				sb.append(" and ");
			} else if (players.size() > 2) {
				lastNameLength = player.length();
				sb.append(", ");
			}

		}

		if (lastNameLength != 0) {
			sb.insert(sb.length() - lastNameLength, " and");
		}

		sb.append(" have had their infractions reset by ");
		sb.append(sender);
		sb.append(" <- End Of Reset");

		log(sb.toString());

	}

	public void log(String message) {
		checkDate();
		LocalTime time = LocalTime.now();

		try {
			FileWriter fw = new FileWriter(file, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(time.toString() + " -> " + message);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
