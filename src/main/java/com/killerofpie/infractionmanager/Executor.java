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
import com.killerofpie.infractionmanager.util.InfractionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
		//subCommands: new/create, view/check, remove/delete, reset, help, reload, type,  params
		//commandPerm: create    view -.other  remove         reset  n/a   reload  create n/a
		//otherThings: receiveBroadcast
		//other Perms: notify
		//perm Prefix: InfractionManager.

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
				sb.append("&a/infraction      \n  &e- Base plugin command");
				sb.append("\n");
				sb.append("&a/infraction help \n  &e- Display help message");
				sb.append("\n");
				sb.append("&a/infraction params \n  &e- Lists parameters for commands");
				sb.append("\n");

				if (sender.hasPermission("InfractionManager.reset")) {
					sb.append("&a/infraction reset <p> \n  &e- Completely resets the player(s) infractions");
					sb.append("\n");
				}

				if (sender.hasPermission("InfractionManager.reload")) {
					sb.append("&a/infraction reload \n  &e- Reloads the plugins configuration files");
					sb.append("\n");
				}

				if (sender.hasPermission("InfractionManager.remove")) {
					sb.append("&a/infraction remove <p> <t> <n> \n  &e- Removes an infraction from a player");
					sb.append("\n");
				}

				if (sender.hasPermission("InfractionManager.view.other")) {
					sb.append("&a/infraction view [t] [p] [pg] [l] [d] \n  &e- Shows all infractions on player or self if no player specified");
					sb.append("\n");
				} else if (sender.hasPermission("InfractionManager.view")) {
					sb.append("&a/infraction view [t] [pg] [l] [d] \n  &e- Shows all of your infractions");
					sb.append("\n");
				}

				if (sender.hasPermission("InfractionManager.create")) {
					sb.append("&a/infraction create <p> <t> <r> \n  &e- Creates a infraction on the player(s)");
					sb.append("\n");
					sb.append("&a/infraction types [pg] [l] [d] \n  &e- Lists available infraction types");
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
				} else if (args.length < 3) {
					sender.sendMessage(colorize("&cYou need to use the parameter 'p' followed by player names!"));
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

				if (players.size() < 1) {
					sender.sendMessage(colorize("&cYou need to use the parameter 'p' followed by player names!"));
					return true;
				} else if (!plugin.getTypeConfig().isInfraction(type)) {
					sender.sendMessage(colorize("&cYou need to use the parameter 't' followed by the type of infraction!"));
					return true;
				} else if (numbers.size() < 1) {
					sender.sendMessage(colorize("&cYou need to use the parameter 'n' followed by the numbers to delete!"));
					return true;
				}

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

				sender.sendMessage(colorize("&aThe listed players don't had any infractions matching the type and numbers removed."));
				sender.sendMessage(colorize("&aPlayers: &e" + sb.toString()));
				sender.sendMessage(colorize("&aType: &e" + type));
				sender.sendMessage(colorize("&aNumbers: &e" + numbers));

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

						sb.append("&2Infractions of " + player.getName());
						sb.append("&aInfraction Type-Total-Active \n");

						for (String key : countsTotal.keySet()) {
							if (countsDecay.containsKey(key)) {
								sb.append("&e" + key + "-" + countsTotal.get(key) + "-" + countsDecay.get(key) + "\n");
							} else {
								sb.append("&e" + key + "-" + countsTotal.get(key) + "-0\n");
							}
						}

						sb.append("&2To see a list of infractions use the parameter: 't <type>'");

						sender.sendMessage(colorize(sb.toString()));
					} else if (players.size() > 0) {
						Map<String, PlayerStorage> ps = Maps.newHashMap();

						for (String str : players) {
							ps.put(str, new PlayerStorage(Bukkit.getOfflinePlayer(str).getUniqueId()));
						}

						StringBuilder sb = new StringBuilder();

						for (String player : players) {
							sb.append("&2Infractions of " + player + "\n");
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
						sb.append("&2To see a list of infractions use the parameter: 't <type>'");

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

						sb.append("&2Infractions of &e" + player.getName() + "\n");
						sb.append("&aType: " + type + "-Total: " + countsTotal.get(type) + "-Active: " + countsDecay.get(type) + " \n");

						for (int i = lowerLimit; i <= upperLimit; i++) {
							Infraction infraction = infractions.get(i + "");
							if (infraction != null) {
								sb.append("&a" + i + ":\n");
								sb.append("  &aPlayers: ");
								for (UUID uuid : infraction.getPlayers()) {
									sb.append("&e" + Bukkit.getOfflinePlayer(uuid).getName() + " ");
								}
								sb.append("\n");
								sb.append("  &aTime: &e" + infraction.getTime().toString() + "\n");
								sb.append("  &aReason: &e" + infraction.getReason() + "\n");
							}

						}

						sb.append("&2To see a different page use the parameter: 'pg <number>'\n");
						sb.append("&2To change the results per page use the parameter: 'l <number>'");

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

							sb.append("&2Infractions of &e" + player + "\n");
							sb.append("&aType: " + type + "-Total: " + countsTotal.get(type) + "-Active: " + countsDecay.get(type) + " \n");

							for (int i = lowerLimit; i <= upperLimit; i++) {
								Infraction infraction = infractions.get(i + "");
								sb.append("&e" + i + ":\n");
								sb.append("  &aPlayers: ");
								for (UUID uuid : infraction.getPlayers()) {
									sb.append("&e" + Bukkit.getOfflinePlayer(uuid).getName() + " ");
								}
								sb.append("\n");
								sb.append("  &aTime: &e" + infraction.getTime().toString() + "\n");
								sb.append("  &aReason: &e" + infraction.getReason() + "\n");

							}
						}

						sb.append("&2To see a different page use the parameter: 'pg <number>'\n");
						sb.append("&2To change the results per page use the parameter: 'l <number>'");

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
					ConsoleCommandSender console = Bukkit.getConsoleSender();
					PlayerStorage ps = new PlayerStorage(uuid);

					ps.addInfraction(infraction);
					int num = ps.getInfractionsOfType(type, plugin.getConfig().getBoolean("enable-decay")).size();
					plugin.getServer().dispatchCommand(console, plugin.getTypeConfig().readInfraction(type).getPunishment(num)
							.replaceAll("%player%", Bukkit.getOfflinePlayer(uuid).getName())
							.replaceAll("%sender%", sender.toString())
							.replaceAll("%type%", type)
							.replaceAll("%num%", num + ""));
				}

				sb.append("&2Successfully gave out infraction(s), details listed below.\n");
				sb.append("&aPlayers: ");
				for (String player : players) {
					sb.append("&e" + player + " ");
				}
				sb.append("\n");
				sb.append("  &aType: &e" + type + "\n");
				sb.append("  &aTime: &e" + date.toString() + "\n");
				sb.append("  &aReason: &e" + reason + "\n");

				sender.sendMessage(colorize(sb.toString()));

				boolean toBroadcast = plugin.getConfig().getBoolean("broadcast");
				StringBuilder broadcast = new StringBuilder();

				broadcast.append("&cThe following players have received warnings for " + type + "\n");
				broadcast.append("&ePlayers: ");
				for (String player : players) {
					broadcast.append("&e" + player + " ");
				}

				for (Player player : plugin.getServer().getOnlinePlayers()) {
					if (toBroadcast || player.hasPermission("InfractionManager.notify"))
						player.sendMessage(colorize(broadcast.toString()));
				}

				return true;
			}

			//Type command
			else if (args[0].equalsIgnoreCase("types")) {
				Map<String, Object> argMap = parseArgs(args);
				int limit = Integer.parseInt(argMap.get("limit").toString()),
						page = Integer.parseInt(argMap.get("page").toString()),
						upperLimit,
						lowerLimit;
				List<InfractionType> types = plugin.getTypeConfig().getInfractionSet();
				StringBuilder sb = new StringBuilder();

				upperLimit = limit + (limit * (page - 1));
				lowerLimit = 1 + (limit * (page - 1));

				if (types.size() == 0) {
					sender.sendMessage(colorize("&cNo infractions types to display!"));
					return true;
				}

				if (upperLimit > types.size()) {
					upperLimit = types.size();
				}

				if (lowerLimit > types.size()) {
					if (types.size() - limit > 1) {
						lowerLimit = types.size() - limit;
					} else {
						lowerLimit = 1;
					}

				}

				sb.append("&2List of infraction types\n");
				sb.append("&2Showing " + (upperLimit - lowerLimit) + " out of " + types.size() + "\n");

				for (int i = lowerLimit; i <= upperLimit; i++) {
					InfractionType infraction = types.get(i);
					if (infraction != null) {
						sb.append("&a" + i + ":\n");
						sb.append("  &aName:&e " + infraction.getName() + "\n");
						sb.append("  &aDecay:&e " + infraction.getDecay() + " days\n");
						sb.append("  &aPunishments:&e \n");
						for (Map.Entry<Integer, String> entry : infraction.getPunishments().entrySet()) {
							sb.append("    &a" + entry.getKey() + ":&e " + entry.getValue() + " \n");
						}
					}

				}

				sb.append("&2To see a different page use the parameter: 'pg <number>'\n");
				sb.append("&2To change the results per page use the parameter: 'l <number>'");

				sender.sendMessage(colorize(sb.toString()));
			}

			//Params command
			else if (args[0].equalsIgnoreCase("params")) {
				StringBuilder sb = new StringBuilder();

				sb.append("&2List of usable parameters\n");
				sb.append("&2Parameters and values should be separated by spaces\n");
				sb.append("&aParam - Value\n");

				sb.append("&e  [P]  - player name(s)\n");
				sb.append("&e  [T]  - infraction type\n");
				sb.append("&e  [R]  - reason for infraction\n");
				sb.append("&e  [N]  - number(s)\n");
				sb.append("&e  [L]  - limit per page\n");
				sb.append("&e  [D]  - decay (true/false)\n");
				sb.append("&e [PG] - page number");

				sender.sendMessage(colorize(sb.toString()));
			}
		}
		return false;
	}

	private Map<String, Object> parseArgs(String[] args) {
		String mode = "", type = "";
		boolean decay = true;
		int limit = 3, page = 1;
		StringBuilder reason = new StringBuilder();
		Map<String, Object> ret = new HashMap<>();
		Set<String> players = new HashSet<>(),
				playerKeys = Sets.newHashSet("player", "players", "p"),
				typeKeys = Sets.newHashSet("type", "t"),
				reasonKeys = Sets.newHashSet("reason", "reasons", "r"),
				numberKeys = Sets.newHashSet("num", "number", "numbers", "n"),
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
						if (check.equalsIgnoreCase("true")
								|| check.equalsIgnoreCase("false"))
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
