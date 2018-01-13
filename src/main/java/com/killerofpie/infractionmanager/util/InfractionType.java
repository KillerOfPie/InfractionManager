package com.killerofpie.infractionmanager.util;

import com.killerofpie.infractionmanager.InfractionManager;
import org.bukkit.Bukkit;

import java.util.Map;

public class InfractionType {

	private String name;
	private Map<Integer, String> punishments;
	private int decay;

	private InfractionManager plugin = (InfractionManager) Bukkit.getPluginManager().getPlugin("InfractionManager");

	public InfractionType(String name, int decay, Map<Integer, String> punishments) {
		this.name = name;
		this.decay = decay;
		this.punishments = punishments;
	}

	public InfractionType(String type) {
		if (!plugin.getTypeConfig().isInfraction(type)) {
			return;
		}

		InfractionType it = plugin.getTypeConfig().readInfraction(type);

		this.name = it.getName();
		this.decay = it.getDecay();
		this.punishments = it.getPunishments();
	}

	public String getPunishment(int num) {
		if (!punishments.containsKey(num)) {
			return "";
		}

		return punishments.get(num);
	}

	public String getName() {
		return name;
	}

	public int getDecay() {
		return decay;
	}

	public Map<Integer, String> getPunishments() {
		return punishments;
	}
}
