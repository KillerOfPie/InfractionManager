package com.killerofpie.infractionmanager.configs;

import com.google.common.collect.Lists;
import com.killerofpie.infractionmanager.InfractionManager;
import com.killerofpie.infractionmanager.util.InfractionType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public class TypeConfig {

	private InfractionManager plugin;
	private File file;
	private FileConfiguration config;

	public TypeConfig(InfractionManager instance) {
		plugin = instance;
		reload();
	}

	private void save() {
		if (config != null)
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void reload() {
		try {
			if (!plugin.getDataFolder().isDirectory()) {
				plugin.getDataFolder().mkdirs();
			}
			file = new File(plugin.getDataFolder(), "infractions.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not create Config file! Disabling plugin!", e);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		config = YamlConfiguration.loadConfiguration(file);
		setDefaults();
	}

	private void setDefaults() {
		Map<Integer, String> punishments = new TreeMap<>();
		punishments.put(1, "You have received an infraction for %type%, this is %num%");
		punishments.put(2, "You have received an infraction for %type%, this is %num%");
		punishments.put(4, "You have received an infraction for %type%, this is %num%");

		addInfraction("grief", 30, punishments);
		addInfraction("spam", 10, punishments);
		addInfraction("advertising", 15, punishments);
		addInfraction("other", 15, punishments);

		save();
	}

	private void addInfraction(String node, int decay, Map<Integer, String> punishments) {
		if (config.get(node) == null) {
			config.set(node + ".decay", decay);
			for (Map.Entry<Integer, String> entry : punishments.entrySet()) {
				config.set(node + ".punishments." + entry.getKey(), entry.getValue());
			}
		}
	}

	public List<InfractionType> getInfractionSet() {
		List<InfractionType> infractionSet = Lists.newArrayList();
		config.getKeys(false).forEach(key -> infractionSet.add(readInfraction(key)));

		return infractionSet;
	}

	public boolean isInfraction(String node) {
		return config.getKeys(false).contains(node);
	}

	public InfractionType readInfraction(String node) {
		if (!isInfraction(node)) {
			return null;
		}

		ConfigurationSection sec = config.getConfigurationSection(node);
		Map<Integer, String> temp = new TreeMap<>();

		for (String s : sec.getConfigurationSection("punishments").getKeys(false)) {
			try {
				int i = Integer.parseInt(s);
				temp.put(i, sec.getString("punishments." + i));
			} catch (NumberFormatException nfe) {
				plugin.getLogger().log(Level.WARNING, "[InfractionManager] Could not read a punishment from file!");
			}

		}

		return new InfractionType(node, sec.getInt("decay"), temp);
	}
}