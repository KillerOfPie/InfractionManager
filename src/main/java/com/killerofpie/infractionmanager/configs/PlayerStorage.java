package com.killerofpie.infractionmanager.configs;

import com.killerofpie.infractionmanager.InfractionManager;
import com.killerofpie.infractionmanager.objects.Infraction;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerStorage {
	private InfractionManager plugin = (InfractionManager) Bukkit.getPluginManager().getPlugin("InfractionManager");
	private File file;
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

		config.set(typeName + "." + num, infraction);

		save();
	}

	public boolean removeInfraction(String type, int num) {
		String path = type + "." + num;
		if (!config.contains(path)) {
			return false;
		}

		config.set(path, null);
		save();
		return true;
	}

	public Infraction getInfraction(String type, int num) {
		String path = type + "." + num;

		if (!config.contains(path)) {
			return null;
		}

		return Infraction.fromMap(config.getConfigurationSection(path).getValues(true));
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
			if (!plugin.getDataFolder().isDirectory()) {
				plugin.getDataFolder().mkdirs();
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

}
