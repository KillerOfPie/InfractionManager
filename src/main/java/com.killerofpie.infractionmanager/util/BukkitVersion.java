package com.killerofpie.infractionmanager.util;

import org.bukkit.Bukkit;

public class BukkitVersion {
	private final String SERVERVERSION = Bukkit.getBukkitVersion();

	private double major;
	private int minor;

	public BukkitVersion() {
		String[] ver = SERVERVERSION.split("-")[0].split("\\.");
		this.major = parseDouble(ver[0]) + parseMinor(ver[1]);
		this.minor = parseInt(ver[2]);
	}

	public BukkitVersion(double major, int minor) {
		this.major = major;
		this.minor = minor;
	}

	public String toString() {
		return major + "." + minor;
	}

	public String getFullVersion() {
		return SERVERVERSION;
	}

	public double getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public boolean isBelow(double ver) {
		return major < ver;
	}

	public boolean isAbove(double ver) {
		return major >= ver;
	}

	private double parseDouble(String toParse) {
		try {
			return Double.parseDouble(toParse);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private int parseInt(String toParse) {
		try {
			return Integer.parseInt(toParse);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private double parseMinor(String toParse) {
		return parseDouble(toParse) / Math.pow(10, toParse.length());
	}
}