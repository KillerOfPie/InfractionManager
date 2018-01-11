package com.killerofpie.infractionmanager.util;

import com.killerofpie.infractionmanager.InfractionManager;
import org.bukkit.Bukkit;

import java.util.Map;

public class InfractionType {

	private String type;
	private Map<Integer, String> punishments;
	private int decay;

	private InfractionManager plugin = (InfractionManager) Bukkit.getPluginManager().getPlugin("InfractionManager");

	public InfractionType(String type, int decay, Map<Integer, String> punishments) {
		this.type = type;
		this.decay = decay;
		this.punishments = punishments;
	}

	public InfractionType(String type) {
		if (!plugin.getTypeConfig().isInfraction(type)) {
			return;
		}

		InfractionType it = plugin.getTypeConfig().readInfraction(type);

		this.type = it.getType();
		this.decay = it.getDecay();
		this.punishments = it.getPunishments();
	}

	public String getPunishment(int punNum) {
		if (!punishments.containsKey(punNum)) {
			return "";
		}

		return punishments.get(punNum);
	}

	public String getType() {
		return type;
	}

	public int getDecay() {
		return decay;
	}

	public Map<Integer, String> getPunishments() {
		return punishments;
	}
}
