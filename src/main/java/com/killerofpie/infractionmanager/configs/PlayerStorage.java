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
	private File file, folder = new File(plugin.getDataFolder() + File.pathSeparator + "data" + File.pathSeparator);
	;
	private FileConfiguration config;

	private UUID uuid;

	public PlayerStorage(UUID uuid) {
		file = new File(plugin.getDataFolder() + File.pathSeparator + "data", uuid.toString() + ".yml");
		this.uuid = uuid;

		load();
	}

	public void addInfraction(Infraction infraction) {
		int num = 1;
		String typeName = infraction.getType().getName();

		if (config.getKeys(false).contains(typeName)) {
			num += config.getConfigurationSection(typeName).getKeys(false).size();
		}

		config.set(typeName + "." + num, infraction.toMap());

		save();
	}

	public void removeInfraction(String type, int num) {
		String path = type + "." + num;
		if (!config.contains(path)) {
			return;
		}

		config.set(path, null);
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
		if (plugin.getTypeConfig().isInfraction(type) && config.getKeys(false).contains(type)) {
			ConfigurationSection sec = config.getConfigurationSection(type);

			for (String i : sec.getKeys(false)) {
				if (useDecay) {
					if (!olderThan(config.getString(type + i), plugin.getTypeConfig().readInfraction(type).getDecay())) {
						infractions.put(i + "", Infraction.fromMap(sec.getConfigurationSection(i).getValues(true)));
					}
				} else {
					infractions.put(i + "", Infraction.fromMap(sec.getConfigurationSection(i).getValues(true)));
				}
			}
		}

		return infractions;
	}

	public Map<String, Integer> getTotalInfractionCount() {
		Map<String, Integer> count = Maps.newHashMap();

		for (String key : config.getKeys(false)) {
			count.put(key, config.getConfigurationSection(key).getKeys(false).size());
		}

		return count;
	}

	public Map<String, Integer> getDecayInfractionCount() {
		Map<String, Integer> count = Maps.newHashMap();

		for (String key : config.getKeys(false)) {
			int counter = 0;
			for (String num : config.getConfigurationSection(key).getKeys(false)) {
				if (!olderThan(config.getString(key + num), plugin.getTypeConfig().readInfraction(key).getDecay())) {
					counter++;
				}
			}
			count.put(key, counter);
		}

		return count;
	}

	public void clearInfractions() {
		config.set("", null);
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

	private boolean olderThan(String date, int daysOld) {
		LocalDate toCheck = LocalDate.parse(date);
		return toCheck.isBefore(LocalDate.now().minusDays(daysOld));
	}

}
