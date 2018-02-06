package com.killerofpie.infractionmanager.configs;

import com.google.common.collect.Maps;
import com.killerofpie.infractionmanager.InfractionManager;
import com.killerofpie.infractionmanager.objects.Infraction;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerStorage {
	private InfractionManager plugin = (InfractionManager) Bukkit.getPluginManager().getPlugin("InfractionManager");
	private File file, folder;

	private FileConfiguration config;

	private UUID uuid;

	public PlayerStorage(UUID uuid) {
		folder = new File(plugin.getDataFolder() + File.separator + "data");
		file = new File(folder, uuid.toString() + ".yml");
		this.uuid = uuid;

		load();
	}

	public void addInfraction(Infraction infraction) {
		addPlayerName();
		int num = 1;
		String typeName = infraction.getType().getName();

		if (config.getKeys(false).contains(typeName)) {
			num += config.getConfigurationSection(typeName).getKeys(false).size();
		}

		config.set(typeName + "." + num, infraction.toMap());

		save();
	}

	public void removeInfraction(String type, int num) {
		addPlayerName();
		String path = type + "." + num;
		if (!config.contains(path)) {
			return;
		}

		config.set(path, null);

		for (int i = num + 1; i < config.getConfigurationSection(type).getKeys(false).size(); i++) {
			config.set((i - 1) + "", config.get(i + ""));
			config.set(i + "", null);
		}

		if (config.getConfigurationSection(type).getKeys(false).size() == 0) {
			config.set(type, null);
		}

		save();
	}

	public Infraction getInfraction(String type, int num) {
		String path = type + "." + num;

		if (!config.contains(path)) {
			return null;
		}

		return Infraction.fromMap(config.getConfigurationSection(path).getValues(true));
	}

	public Map<String, Infraction> getInfractionsOfType(String type, boolean useDecay) {
		Map<String, Infraction> infractions = Maps.newHashMap();
		int decayTime = plugin.getTypeConfig().readInfraction(type).getDecay();
		if (plugin.getTypeConfig().isInfraction(type) && config.getKeys(false).contains(type)) {
			ConfigurationSection sec = config.getConfigurationSection(type);

			for (String i : sec.getKeys(false)) {
				if (useDecay) {
					if (!olderThan(config.getString(type + "." + i + ".time"), decayTime)) {
						infractions.put(i + "", Infraction.fromMap(sec.getConfigurationSection(i).getValues(true)));
					}
				} else {
					infractions.put(i + "", Infraction.fromMap(sec.getConfigurationSection(i).getValues(true)));
				}
			}
		}
		
		return infractions;
	}

	public int getInfractionCountOfType(String type, boolean useDecay) {
		return getInfractionsOfType(type, useDecay).size();
	}

	public Map<String, Integer> getTotalInfractionCount() {
		Map<String, Integer> count = Maps.newHashMap();

		for (String key : config.getKeys(false)) {
			if (!key.equalsIgnoreCase("ResetOn"))
				count.put(key, config.getConfigurationSection(key).getKeys(false).size());
		}

		return count;
	}

	public Map<String, Integer> getDecayInfractionCount() {
		Map<String, Integer> count = Maps.newHashMap();

		for (String key : config.getKeys(false)) {
			if (!key.equalsIgnoreCase("ResetOn")) {
				int counter = 0;
				for (String num : config.getConfigurationSection(key).getKeys(false)) {
					if (!olderThan(config.getString(key + "." + num + ".time"), plugin.getTypeConfig().readInfraction(key).getDecay())) {
						counter++;
					}
				}
				count.put(key, counter);
			}
		}

		return count;
	}

	public void clearInfractions() {
		for (String key : config.getKeys(false)) {
			if (!(key.equalsIgnoreCase("ResetOn") || key.equalsIgnoreCase("Player-Name"))) {
				config.set(key, null);
			}
		}
		config.set("ResetOn", LocalDate.now().toString());
		save();
	}

	private void save() {
		if (config != null)
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}

		load();
	}

	public void load() {
		try {
			if (!folder.isDirectory()) {
				folder.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not create Config file! Disabling plugin!", e);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		config = YamlConfiguration.loadConfiguration(file);
	}

	private void addPlayerName() {
		if (!config.contains("Player-Name")) {
			config.set("Player-Name", Bukkit.getOfflinePlayer(uuid).getName());
		}
	}

	private boolean olderThan(String date, int daysOld) {
		return LocalDate.parse(date).isBefore(LocalDate.now().minusDays(daysOld));
	}
}
