/*
 *     Copyright (c) 2016-2017 KillerOfPie @ http://killerofpie.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: All modifications made by others to the source code belong
 * to the respective contributor. No contributor should be held liable for
 * any damages of any kind, whether be material or moral, which were
 * caused by their contribution(s) to the project. See the full License for more information
 */

package com.killerofpie.infractionmanager;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.killerofpie.infractionmanager.configs.PlayerStorage;
import com.killerofpie.infractionmanager.objects.Infraction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Executor implements CommandExecutor {

	private InfractionManager plugin;

	public Executor(InfractionManager instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//subCommands: new/create, view/check, remove/delete, reset, help, reload
		//commandPerm: create    view -.other  remove         reset  n/a   reload
		//Perm Prefix: InfractionManager.
		//commandKeys: type - t, player/players - p, reason - r

		if (cmd.getName().equalsIgnoreCase("infraction")) {

			//Help Command
			if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
				PluginDescriptionFile pdf = plugin.getDescription();
				StringBuilder sb = new StringBuilder();
				String msg;

				sb.append("\n");
				sb.append("&2" + pdf.getName() + " " + pdf.getVersion() + " by " + pdf.getAuthors().get(0) + "\n");
				sb.append("\n");
				sb.append("\n");
				sb.append("&6/infraction      \n  &6- Base plugin command");
				sb.append("\n");
				sb.append("&6/infraction help \n  &6- Display help message");
				sb.append("\n");

				if (sender.hasPermission("InfractionManager.reset")) {
					sb.append("&6/infraction reset p <player(s)> \n  &6- Completely resets the player(s) infractions");
					sb.append("\n");
				}

				if (sender.hasPermission("InfractionManager.reload")) {
					sb.append("&6/infraction reload \n  &6- Reloads the plugins configuration files");
					sb.append("\n");
				}

				if (sender.hasPermission("InfractionManager.remove")) {
					sb.append("&6/infraction remove p <player> t <infraction type> n <number> \n  &6- Removes an infraction from a player");
					sb.append("\n");
				}

				if (sender.hasPermission("InfractionManager.view.other")) {
					sb.append("&6/infraction view [t <type>] [p <player>] \n  &6- Shows all infractions on player or self if no player specified");
					sb.append("\n");
				} else if (sender.hasPermission("InfractionManager.view")) {
					sb.append("&6/infraction view [t <type>] \n  &6- Shows all of your infractions");
					sb.append("\n");
				}

				if (sender.hasPermission("InfractionManager.create")) {
					sb.append("&6/infraction create p <player(s)> t <infraction type> r <reason message> \n  &6- Creates a infraction on the player(s)");
					sb.append("\n");
				}

				msg = sb.toString();

				sender.sendMessage(colorize(msg));

				return true;
			}

			//Reload Command
			else if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("InfractionManager.reload")) {
					sender.sendMessage(colorize("&cYou don't have permission for that!"));
					return true;
				}

				plugin.reloadConfig();
				plugin.getTypeConfig().reload();
				sender.sendMessage(colorize("&aConfigs Reloaded!"));

				return true;
			}


			//Reset Command
			else if (args[0].equalsIgnoreCase("reset")) {
				if (!sender.hasPermission("InfractionManager.reset")) {
					sender.sendMessage(colorize("&cYou don't have permission for that!"));
					return true;
				} else if (!(args.length < 3)) {
					sender.sendMessage(colorize("&cImproper arguments! Make sure you use 'p' followed by all players to reset separated by spaces."));
					return true;
				}

				Set<String> players = (Set<String>) parseArgs(args).get("players");
				StringBuilder sb = new StringBuilder();

				for (String player : players) {
					new PlayerStorage(Bukkit.getOfflinePlayer(player).getUniqueId()).clearInfractions();
					sb.append(player + " ");
				}

				sender.sendMessage(colorize("&aThe following players infractions have been reset: "));
				sender.sendMessage(colorize("&e" + sb.toString()));

				return true;
			}

			//Remove Command - Delete Alias
			else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
				if (!sender.hasPermission("InfractionManager.remove")) {
					sender.sendMessage("&cYou don't have permission for that!");
					return true;
				}

				Map<String, Object> argMap = parseArgs(args);
				Set<String> players = (Set<String>) argMap.get("players");
				Set<Integer> numbers = (Set<Integer>) argMap.get("numbers");
				String type = argMap.get("type").toString();

				StringBuilder sb = new StringBuilder();
				StringBuilder sbi = new StringBuilder();

				for (int i : numbers) {
					sbi.append(i + " ");
				}

				for (String player : players) {
					PlayerStorage ps = new PlayerStorage(Bukkit.getOfflinePlayer(player).getUniqueId());

					for (int i : numbers) {
						ps.removeInfraction(type, i);
					}

					sb.append(player + " ");
				}

				sender.sendMessage(colorize("&aThe listed players have had any infractions matching the type and numbers removed."));
				sender.sendMessage(colorize("&ePlayers: " + sb.toString()));
				sender.sendMessage(colorize("&eType: " + type));
				sender.sendMessage(colorize("&eNumbers: " + numbers));

				return true;
			}

			//View Command - Check Alias
			else if (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("check")) {
				Map<String, Object> argMap = parseArgs(args);
				Set<String> players = (Set<String>) argMap.get("players");
				String type = argMap.get("type").toString();

				if (!sender.hasPermission("InfractionManager.view") && sender instanceof Player) {
					sender.sendMessage("&cYou don't have permission for that!");
					return true;
				} else if (!sender.hasPermission("InfractionManager.view.other") && players.size() > 0) {
					sender.sendMessage("&cYou don't have permission for that!");
					return true;
				}

				if (type.equalsIgnoreCase("")) {
					if (players.size() == 0) {
						Player player = (Player) sender;
						PlayerStorage ps = new PlayerStorage(player.getUniqueId());
						StringBuilder sb = new StringBuilder();
						Map<String, Integer> countsTotal = ps.getTotalInfractionCount(),
								countsDecay = ps.getDecayInfractionCount();

						sb.append("&aInfractions of " + player.getName());
						sb.append("&aInfraction Type-Total-Active \n");

						for (String key : countsTotal.keySet()) {
							if (countsDecay.containsKey(key)) {
								sb.append("&e" + key + "-" + countsTotal.get(key) + "-" + countsDecay.get(key) + "\n");
							} else {
								sb.append("&e" + key + "-" + countsTotal.get(key) + "-0\n");
							}
						}

						sb.append("&aTo see a list of infractions use the parameter: 't <type>'");

						sender.sendMessage(colorize(sb.toString()));
					} else if (players.size() > 0) {
						Map<String, PlayerStorage> ps = Maps.newHashMap();

						for (String str : players) {
							ps.put(str, new PlayerStorage(Bukkit.getOfflinePlayer(str).getUniqueId()));
						}

						StringBuilder sb = new StringBuilder();

						for (String player : players) {
							sb.append("&aInfractions of " + player);
							sb.append("&aInfraction Type-Total-Active \n");

							Map<String, Integer> countsTotal = ps.get(player).getTotalInfractionCount(),
									countsDecay = ps.get(player).getDecayInfractionCount();

							for (String key : countsTotal.keySet()) {
								if (countsDecay.containsKey(key)) {
									sb.append("&e" + key + "-" + countsTotal.get(key) + "-" + countsDecay.get(key) + "\n");
								} else {
									sb.append("&e" + key + "-" + countsTotal.get(key) + "-0\n");
								}
							}

						}
						sb.append("&aTo see a list of infractions use the parameter: 't <type>'");

						sender.sendMessage(colorize(sb.toString()));
					}
				} else if (plugin.getTypeConfig().isInfraction(type)) {
					if (players.size() == 0) {
						Player player = (Player) sender;
						PlayerStorage ps = new PlayerStorage(player.getUniqueId());
						boolean decay = Boolean.parseBoolean(argMap.get("decay").toString());
						int limit = Integer.parseInt(argMap.get("limit").toString()),
								page = Integer.parseInt(argMap.get("page").toString()),
								upperLimit = limit,
								lowerLimit = 1;

						upperLimit = limit + (limit * (page - 1));
						lowerLimit = 1 + (limit * (page - 1));

						StringBuilder sb = new StringBuilder();
						Map<String, Integer> countsTotal = ps.getTotalInfractionCount(),
								countsDecay = ps.getDecayInfractionCount();
						Map<String, Infraction> infractions = ps.getInfractionsOfType(type, decay);

						if (infractions.size() == 0) {
							sender.sendMessage(colorize("&aNo " + type + " infractions for " + player.getName()));
							return true;
						}

						if (upperLimit > infractions.size()) {
							upperLimit = infractions.size();
						}

						if (lowerLimit > infractions.size()) {
							if (infractions.size() - limit > 1) {
								lowerLimit = infractions.size() - limit;
							} else {
								lowerLimit = 1;
							}

						}

						sb.append("&aInfractions of " + player.getName());
						sb.append("&aType: " + type + "-Total: " + countsTotal.get(type) + "-Active: " + countsDecay.get(type) + " \n");

						for (int i = lowerLimit; i <= upperLimit; i++) {
							Infraction infraction = infractions.get(i + "");
							sb.append("&e" + i + ":\n");
							sb.append("  &ePlayers: ");
							for (UUID uuid : infraction.getPlayers()) {
								sb.append("&e" + Bukkit.getOfflinePlayer(uuid).getName() + " ");
							}
							sb.append("\n");
							sb.append("  &eTime: " + infraction.getTime().toString() + "\n");
							sb.append("  &eReason: " + infraction.getReason() + "\n");
							sb.append("&6--\n");

						}

						sb.append("&aTo see a different page use the parameter: 'pg <number>'\n");
						sb.append("&aTo change the results per page use the parameter: 'l <number>'");

						sender.sendMessage(colorize(sb.toString()));
					} else if (players.size() > 0) {
						Map<String, PlayerStorage> ps = Maps.newHashMap();

						for (String str : players) {
							ps.put(str, new PlayerStorage(Bukkit.getOfflinePlayer(str).getUniqueId()));
						}
						boolean decay = Boolean.parseBoolean(argMap.get("decay").toString());
						int limit = Integer.parseInt(argMap.get("limit").toString()),
								page = Integer.parseInt(argMap.get("page").toString()),
								upperLimit = limit,
								lowerLimit = 1;

						upperLimit = limit + (limit * (page - 1));
						lowerLimit = 1 + (limit * (page - 1));

						StringBuilder sb = new StringBuilder();
						for (String player : players) {
							Map<String, Integer> countsTotal = ps.get(player).getTotalInfractionCount(),
									countsDecay = ps.get(player).getDecayInfractionCount();
							Map<String, Infraction> infractions = ps.get(player).getInfractionsOfType(type, decay);

							if (infractions.size() == 0) {
								sender.sendMessage(colorize("&aNo " + type + " infractions for " + player));
								return true;
							}

							if (upperLimit > infractions.size()) {
								upperLimit = infractions.size();
							}

							if (lowerLimit > infractions.size()) {
								if (infractions.size() - limit > 1) {
									lowerLimit = infractions.size() - limit;
								} else {
									lowerLimit = 1;
								}

							}

							sb.append("&aInfractions of " + player);
							sb.append("&aType: " + type + "-Total: " + countsTotal.get(type) + "-Active: " + countsDecay.get(type) + " \n");

							for (int i = lowerLimit; i <= upperLimit; i++) {
								Infraction infraction = infractions.get(i + "");
								sb.append("&e" + i + ":\n");
								sb.append("  &ePlayers: ");
								for (UUID uuid : infraction.getPlayers()) {
									sb.append("&e" + Bukkit.getOfflinePlayer(uuid).getName() + " ");
								}
								sb.append("\n");
								sb.append("  &eTime: " + infraction.getTime().toString() + "\n");
								sb.append("  &eReason: " + infraction.getReason() + "\n");
								sb.append("&6--\n");

							}
						}

						sb.append("&aTo see a different page use the parameter: 'pg <number>'\n");
						sb.append("&aTo change the results per page use the parameter: 'l <number>'");

						sender.sendMessage(colorize(sb.toString()));
					}
				}

				return true;
			}

			//New Command - Create Alias
			else if (args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("create")) {
				if (!sender.hasPermission("InfractionManager.create")) {
					sender.sendMessage("&cYou don't have permission for that!");
					return true;
				}

				Map<String, Object> argMap = parseArgs(args);
				Set<String> players = (Set<String>) argMap.get("players");
				String type = argMap.get("type").toString(),
						reason = argMap.get("reason").toString();
				LocalDate date = LocalDate.now();
				UUID[] uuids = new UUID[players.size()];
				StringBuilder sb = new StringBuilder();

				if (players.size() < 1) {
					sender.sendMessage(colorize("&cYou need to use the parameter 'p' followed by player names!"));
					return true;
				} else if (!plugin.getTypeConfig().isInfraction(type)) {
					sender.sendMessage(colorize("&cYou need to use the parameter 't' followed by the type of infraction!"));
					return true;
				} else if (reason.equalsIgnoreCase("")) {
					sender.sendMessage(colorize("&cYou need to use the parameter 'r' followed by the reason for the infraction!"));
					return true;
				}
				String[] pls = players.toArray(new String[0]);
				for (int i = 0; i < players.size(); i++) {
					uuids[i] = Bukkit.getOfflinePlayer(pls[i]).getUniqueId();
				}

				Infraction infraction = new Infraction(type, uuids, date, reason);

				for (UUID uuid : uuids) {
					PlayerStorage ps = new PlayerStorage(uuid);
					ps.addInfraction(infraction);
				}

				sb.append("&aSuccessfully gave out infraction(s), details listed below.\n");
				sb.append("&ePlayers: ");
				for (String player : players) {
					sb.append("&e" + player + " ");
				}
				sb.append("\n");
				sb.append("  &eType: " + type + "\n");
				sb.append("  &eTime: " + date.toString() + "\n");
				sb.append("  &eReason: " + reason + "\n");

				return true;
			}
		}
		return false;
	}

	private Map<String, Object> parseArgs(String[] args) {
		String mode = "", type = "";
		boolean decay = false;
		int limit = 5, page = 1;
		StringBuilder reason = new StringBuilder();
		Map<String, Object> ret = new HashMap<>();
		Set<String> players = new HashSet<>(),
				playerKeys = Sets.newHashSet("player", "players", "p"),
				typeKeys = Sets.newHashSet("infractiontype", "type", "t"),
				reasonKeys = Sets.newHashSet("reason", "reasons", "r"),
				numberKeys = Sets.newHashSet("number", "numbers", "n"),
				limitKeys = Sets.newHashSet("limit", "l"),
				decayKeys = Sets.newHashSet("decay", "decayed", "d"),
				pageKeys = Sets.newHashSet("page", "pg");

		Set<Integer> numbers = new HashSet<>();


		for (int i = 0; i < args.length; i++) {
			String check = args[i].toLowerCase();

			if (playerKeys.contains(check)) {
				mode = "p";
			} else if (typeKeys.contains(check)) {
				mode = "t";
			} else if (reasonKeys.contains(check)) {
				mode = "r";
			} else if (numberKeys.contains(check)) {
				mode = "n";
			} else if (limitKeys.contains(check)) {
				mode = "l";
			} else if (decayKeys.contains(check)) {
				mode = "d";
			} else if (pageKeys.contains(check)) {
				mode = "pg";
			} else {

				switch (mode) {
					case "p":
						players.add(check);
						break;
					case "t":
						type = check;
						break;
					case "r":
						reason.append(check + " ");
						break;
					case "n":
						if (isInt(check))
							numbers.add(Integer.parseInt(check));
						break;
					case "l":
						if (isInt(check) && Integer.parseInt(check) > 0)
							limit = Integer.parseInt(check);
						break;
					case "d":
						if (Boolean.parseBoolean(check))
							decay = Boolean.parseBoolean(check);
						break;
					case "pg":
						if (isInt(check) && Integer.parseInt(check) > 0)
							page = Integer.parseInt(check);
						break;
					default:
						//do nothing
						break;
				}
			}
		}

		ret.put("type", type);
		ret.put("reason", reason.toString());
		ret.put("players", players);
		ret.put("numbers", numbers);
		ret.put("decay", decay);
		ret.put("limit", limit);
		ret.put("page", page);

		return ret;
	}

	/**
	 * This function wraps up Bukkit's method {@code ChatColor.translateAlternateColorCodes('&', msg)}.
	 * <br>
	 * Used for shortening purposes and follows the DRY concept.
	 *
	 * @param msg string containing Color and formatting codes.
	 * @return the colorized string returned by the above method.
	 */
	public String colorize(String msg) {
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}

	/**
	 * Checks if the string is an int
	 *
	 * @param toCheck string to check
	 * @return true if is int
	 */
	public boolean isInt(String toCheck) {
		try {
			Integer.parseInt(toCheck);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

}
