package com.killerofpie.infractionmanager.objects;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.killerofpie.infractionmanager.InfractionManager;
import com.killerofpie.infractionmanager.configs.PlayerStorage;
import com.killerofpie.infractionmanager.util.CommandType;
import com.killerofpie.infractionmanager.util.InfractionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InfractionCommand {
	final InfractionManager plugin = (InfractionManager) Bukkit.getPluginManager().getPlugin("InfractionManager");

	CommandSender sender;
	CommandType cmdType;
	String[] args;
	String msg;

	Set<String> players = new HashSet<>();
	Set<Integer> numbers = new HashSet<>();
	String reason = "", type = "";
	boolean decay = true, execute = true;
	int limit = 3, page = 1;
	int upperLimit = limit + (limit * (page - 1)),
			lowerLimit = 1 + (limit * (page - 1));


	PluginDescriptionFile pdf = plugin.getDescription();
	StringBuilder sb = new StringBuilder();
	Map<String, PlayerStorage> ps = Maps.newHashMap();
	ConsoleCommandSender console = Bukkit.getConsoleSender();

	LocalDate date = LocalDate.now();
	UUID officer;


	public InfractionCommand(CommandSender sender, CommandType cmdType, String[] args) {
		this.sender = sender;
		this.cmdType = cmdType;
		this.args = args;

		parseArgs();

		if (args.length == 2 && players.size() > 0) {
			if (Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
				players.add(args[1]);
			}
		}

		switch (cmdType) {
			case HELP:
				helpCmd();
				break;
			case NONE:
				helpCmd();
				break;
			case TYPE:
				typeCmd();
				break;
			case VIEW:
				viewCmd();
				break;
			case RESET:
				resetCmd();
				break;
			case CREATE:
				createCmd();
				break;
			case IMPORT:
				importCmd();
				break;
			case PARAMS:
				paramsCmd();
				break;
			case RELOAD:
				reloadCmd();
				break;
			case REMOVE:
				removeCmd();
				break;
			case COMMANDS:
				commandCmd();
				break;
		}
	}

	private void helpCmd() {
		helpHeader();

		sb.append("\n");
		sb.append("&a/infraction      \n  &e- Base plugin command");
		sb.append("\n");
		sb.append("&a/infraction help \n  &e- Display help message");
		sb.append("\n");
		sb.append("&a/infraction params \n  &e- Lists parameters for commands");
		sb.append("\n");
		sb.append("&a/infraction commands \n  &e- Lists available commands");
		sb.append("\n");

		if (sender.hasPermission("InfractionManager.create")) {
			sb.append("&a/infraction types [pg] [l]\n  &e- Lists available infraction types");
			sb.append("\n");
		}

		msg = sb.toString();

		sender.sendMessage(colorize(msg));
	}

	private void commandCmd() {
		helpHeader();

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
		}

		msg = sb.toString();

		sender.sendMessage(colorize(msg));
	}

	private void typeCmd() {
		List<InfractionType> types = plugin.getTypeConfig().getInfractionSet();

		if (types.size() == 0) {
			sender.sendMessage(colorize("&cNo infractions types to display!"));
			return;
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
		sb.append("&2Showing ");
		sb.append(lowerLimit);
		sb.append("-");
		sb.append(upperLimit);
		sb.append(" out of ");
		sb.append(types.size());
		sb.append("\n");

		for (int i = lowerLimit; i <= upperLimit; i++) {
			InfractionType infraction = types.get(i - 1);
			if (infraction != null) {
				sb.append("&aName:&e ");
				sb.append(infraction.getName());
				sb.append("\n");

				if (sender.hasPermission("infractionmanager.types.extra")) {
					sb.append("  &aDecay:&e ");
					sb.append(infraction.getDecay());
					sb.append(" days\n");
					sb.append("  &aPunishments:&e \n");
					for (Map.Entry<Integer, String> entry : infraction.getPunishments().entrySet()) {
						sb.append("    &a");
						sb.append(entry.getKey());
						sb.append(":&e ");
						sb.append(entry.getValue());
						sb.append(" \n");
					}
				}
			}

		}

		if (limit <= types.size()) {
			sb.append("&2To see a different page use the parameter: 'pg <number>'\n");
		} else {
			sb.append("&2To change the results per page use the parameter: 'l <number>'");
		}

		sender.sendMessage(colorize(sb.toString()));
	}

	private void viewCmd() {
		if (players.size() == 0) {
			if (sender instanceof Player) {
				if (sender.hasPermission("InfractionManager.view")) {
					players.add(sender.getName());
				} else {
					sender.sendMessage("&cYou don't have permission for that!");
				}
			} else {
				sender.sendMessage(colorize("You must be a player to do that!"));
			}
		} else if (!sender.hasPermission("InfractionManager.view.other")) {
			sender.sendMessage("&cYou don't have permission for that!");
		}

		for (String str : players) {
			ps.put(str, new PlayerStorage(Bukkit.getOfflinePlayer(str).getUniqueId()));
		}

		if (type.equalsIgnoreCase("")) {
			viewNoType();
		} else {
			viewType();
		}

		sender.sendMessage(colorize(sb.toString()));

	}

	private void resetCmd() {
		if (!sender.hasPermission("InfractionManager.reset")) {
			sender.sendMessage(colorize("&cYou don't have permission for that!"));
			return;
		}

		if (players.size() == 0) {
			sender.sendMessage(colorize("&cYou need to use the parameter 'p' followed by player names!"));
			return;
		}

		sb.append("&aThe following players infractions have been reset: \n&e");

		for (String player : players) {
			new PlayerStorage(Bukkit.getOfflinePlayer(player).getUniqueId()).clearInfractions();
			sb.append(player);
			sb.append(" ");
		}

		sender.sendMessage(colorize(sb.toString()));
		plugin.getLocalLog().logReset(sender.getName(), players);

	}

	private void createCmd() {
		if (!sender.hasPermission("InfractionManager.create")) {
			sender.sendMessage("&cYou don't have permission for that!");
			return;
		}

		if (sender instanceof Player) {
			officer = ((Player) sender).getUniqueId();
		} else {
			officer = Bukkit.getOfflinePlayer("Console").getUniqueId();
		}

		if (players.size() == 0) {
			sender.sendMessage(colorize("&cYou need to use the parameter 'p' followed by player names!"));
			return;
		}

		if (!plugin.getTypeConfig().isInfraction(type)) {
			sender.sendMessage(colorize("&cYou need to use the parameter 't' followed by the type of infraction!"));
			return;
		}

		if (reason.equalsIgnoreCase("")) {
			sender.sendMessage(colorize("&cYou need to use the parameter 'r' followed by the reason for the infraction!"));
			return;
		}

		UUID[] uuids = new UUID[players.size()];
		String[] pls = players.toArray(new String[0]);
		for (int i = 0; i < pls.length; i++) {
			uuids[i] = Bukkit.getOfflinePlayer(pls[i]).getUniqueId();
		}

		Infraction infraction = new Infraction(type, uuids, date, reason, officer);

		for (UUID uuid : uuids) {
			PlayerStorage ps = new PlayerStorage(uuid);

			ps.addInfraction(infraction);

			int num = ps.getInfractionCountOfType(type, plugin.getConfig().getBoolean("enable-decay"));
			InfractionType infractionType = plugin.getTypeConfig().readInfraction(type);

			if ((!infractionType.getPunishment(num).equalsIgnoreCase("")) && execute) {
				plugin.getServer().dispatchCommand(console, infractionType.getPunishment(num)
						.replaceAll("%player%", Bukkit.getOfflinePlayer(uuid).getName())
						.replaceAll("%sender%", sender.getName())
						.replaceAll("%type%", type)
						.replaceAll("%num%", num + ""));
			}
		}

		sb.append("&2Successfully gave out infraction(s), details listed below.\n");
		sb.append("  &aPlayers: &e");
		for (String player : players) {
			sb.append(player);
			sb.append(" ");
		}
		sb.append("\n");
		sb.append("  &aType: &e");
		sb.append(type);
		sb.append("\n");
		sb.append("  &aTime: &e");
		sb.append(date.toString());
		sb.append("\n");
		sb.append("  &aReason: &e");
		sb.append(reason);
		sb.append("\n");

		if (!execute) {
			sb.append("  &aExecuted: &e");
			sb.append(execute);
			sb.append("\n");
		}

		sender.sendMessage(colorize(sb.toString()));

		StringBuilder broadcast = new StringBuilder();

		broadcast.append("&cThe following players have received warnings for ");
		broadcast.append(type);
		broadcast.append("\n");
		broadcast.append("&ePlayers: &e");
		for (String player : players) {
			broadcast.append(player);
			broadcast.append(" ");
		}

		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (players.contains(player.getName().toLowerCase())) {
				PlayerStorage ps = new PlayerStorage(player.getUniqueId());
				player.sendMessage(colorize("&cYou have been warned for &e" + type + " &c! \nYou have &e" + ps.getInfractionCountOfType(type, plugin.getConfig().getBoolean("enable-decay")) + " &cinfractions for &e" + type + "&c."));
			} else if ((plugin.getConfig().getBoolean("broadcast") || player.hasPermission("InfractionManager.notify")) && !player.getName().equalsIgnoreCase(sender.getName())) {
				player.sendMessage(colorize(broadcast.toString()));
			}
		}

		plugin.getLocalLog().logCreate(infraction);
	}

	private void importCmd() {
		sender.sendMessage(colorize("&6Importing currently only works for WarningManager, if there are other plugins you would like to be able to import from please let me know."));
		if (plugin.getConfig().getBoolean("hasImported")) {
			sender.sendMessage(colorize("&4You have already imported infractions from another plugin."));
			return;
		}

		if (Bukkit.getServer().getOnlinePlayers().size() > 0) {
			sender.sendMessage(colorize("&4Please read our wiki page on this feature before using it. https://github.com/KillerOfPie/InfractionManager/wiki/Import"));
			return;
		}

		if (sender instanceof Player) {
			sender.sendMessage(colorize("&4Please read our wiki page on this feature before using it. https://github.com/KillerOfPie/InfractionManager/wiki/Import"));
			return;
		}

		sender.sendMessage(colorize("&6Importing, please do not stop the server or allow players on until the finished message appears."));


		File file = new File("plugins/WarningManager/warnings.yml");
		if (!file.exists()) {
			sender.sendMessage(colorize("&4Could not find a file to import. Import cancelled!"));
			return;
		}
		FileConfiguration importConfig = YamlConfiguration.loadConfiguration(file);

		int importedWarnings = 0,
				importedPlayers = 0;

		for (String key : importConfig.getKeys(false)) {

			sender.sendMessage(colorize("&eMoving player: " + key));
			importedPlayers++;
			ConfigurationSection sec = importConfig.getConfigurationSection(key);
			PlayerStorage ps = new PlayerStorage(Bukkit.getOfflinePlayer(key).getUniqueId());

			for (int i = 1; i <= sec.getInt("Total-Warnings"); i++) {

				sender.sendMessage(colorize("    - &eMoving warning: " + i));
				importedWarnings++;
				ConfigurationSection num = sec.getConfigurationSection(i + "");
				String type = num.getString("Type"),
						reason = num.getString("Reason");

				UUID receiver = Bukkit.getOfflinePlayer(key).getUniqueId(),
						officer = Bukkit.getOfflinePlayer(num.getString("Sender")).getUniqueId();

				LocalDate date = LocalDateTime.ofInstant(((Date) num.get("Date")).toInstant(), ZoneId.systemDefault()).toLocalDate();

				ps.addInfraction(new Infraction(type, receiver, date, reason, officer));
			}
		}

		plugin.getConfig().set("hasImported", true);
		plugin.saveConfig();
		sender.sendMessage(colorize("&2Import complete."));

		plugin.getLocalLog().log("Imported " + importedPlayers + " players with " + importedWarnings + " warnings! <- End Of Import");
	}

	private void reloadCmd() {
		if (!sender.hasPermission("InfractionManager.reload")) {
			sender.sendMessage(colorize("&cYou don't have permission for that!"));
			return;
		}

		plugin.reloadConfig();
		plugin.getTypeConfig().reload();
		sender.sendMessage(colorize("&aConfigs Reloaded!"));
	}

	private void removeCmd() {
		if (!sender.hasPermission("InfractionManager.remove")) {
			sender.sendMessage("&cYou don't have permission for that!");
			return;
		}

		if (players.size() == 0) {
			sender.sendMessage(colorize("&cYou need to use the parameter 'p' followed by player names!"));
			return;
		}

		if (!plugin.getTypeConfig().isInfraction(type)) {
			sender.sendMessage(colorize("&cYou need to use the parameter 't' followed by the type of infraction!"));
			return;
		}

		if (numbers.size() == 0) {
			sender.sendMessage(colorize("&cYou need to use the parameter 'n' followed by the numbers to delete!"));
			return;
		}

		sb.append("&aAny infractions matching these parameters have been removed.\n");
		sb.append("&aPlayers: &e");


		for (String player : players) {
			PlayerStorage ps = new PlayerStorage(Bukkit.getOfflinePlayer(player).getUniqueId());

			for (int i : numbers) {
				plugin.getLocalLog().logRemove(sender.getName(), player, ps.getInfraction(type, i));
				ps.removeInfraction(type, i);
			}

			sb.append(player + " ");
		}

		sb.append("\n&aType: &e");
		sb.append(type);
		sb.append("\n&aNumbers: &e");
		sb.append(numbers);

		sender.sendMessage(colorize(sb.toString()));
	}

	private void paramsCmd() {
		sb.append("&2List of usable parameters\n");
		sb.append("&2Parameters and values should be separated by spaces\n");
		sb.append("&aParam - Value\n");

		sb.append("&e  [P]  - player name(s)\n");
		sb.append("&e  [T]  - infraction type\n");
		sb.append("&e  [R]  - reason for infraction\n");
		sb.append("&e  [N]  - number(s)\n");
		sb.append("&e  [L]  - limit per page\n");
		sb.append("&e  [D]  - decay (true/false)\n");
		sb.append("&e  [E]  - execute (true/false)\n");
		sb.append("&e [PG] - page number");

		sender.sendMessage(colorize(sb.toString()));
	}

	private void viewNoType() {
		for (String player : players) {
			sb.append("&2Infractions of ");
			sb.append(player);
			sb.append("\n");
			sb.append("&aInfraction Type-Total-Active \n&0");

			Map<String, Integer> countsTotal = ps.get(player).getTotalInfractionCount(),
					countsDecay = ps.get(player).getDecayInfractionCount();

			for (String key : countsTotal.keySet()) {
				if (countsDecay.containsKey(key)) {
					sb.append("&e");
					sb.append(key);
					sb.append("-");
					sb.append(countsTotal.get(key));
					sb.append("-");
					sb.append(countsDecay.get(key));
					sb.append("\n");
				} else {
					sb.append("&e");
					sb.append(key);
					sb.append("-");
					sb.append(countsTotal.get(key));
					sb.append("-0\n");
				}
			}

			if ((sb.charAt(sb.length() - 1) + "").equalsIgnoreCase("0")) {
				sb.append("&e");
				sb.append(player);
				sb.append(" has no infractions.\n");
			}

		}
		sb.append("&2To see a list of infractions use the parameter: 't <type>'");
	}

	private void viewType() {
		for (String player : players) {
			Map<String, Integer> countsTotal = ps.get(player).getTotalInfractionCount(),
					countsDecay = ps.get(player).getDecayInfractionCount();
			Map<String, Infraction> infractions = ps.get(player).getInfractionsOfType(type, decay);

			if (infractions.size() == 0) {
				sender.sendMessage(colorize("&aNo " + type + " infractions for " + player));
				return;
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

			sb.append("&2Infractions of &e");
			sb.append(player);
			sb.append("\n");
			sb.append("&aType: ");
			sb.append(type);
			sb.append("-Total: ");
			sb.append(countsTotal.get(type));
			sb.append("-Active: ");
			sb.append(countsDecay.get(type));
			sb.append(" \n&0");

			for (int i = lowerLimit; i <= upperLimit; i++) {
				Infraction infraction = infractions.get(i + "");
				sb.append("&e");
				sb.append(i);
				sb.append(":\n");
				sb.append("  &aPlayers: ");
				for (UUID uuid : infraction.getPlayers()) {
					sb.append("&e");
					sb.append(Bukkit.getOfflinePlayer(uuid).getName());
					sb.append(" ");
				}
				sb.append("\n");
				sb.append("  &aTime: &e");
				sb.append(infraction.getTime().toString());
				sb.append("\n");
				sb.append("  &aReason: &e");
				sb.append(infraction.getReason());
				sb.append("\n");

			}

			if ((sb.charAt(sb.length() - 1) + "").equalsIgnoreCase("0")) {
				sb.append("&e");
				sb.append(player);
				sb.append(" has no infractions.\n");
			}

			if (limit <= infractions.size()) {
				sb.append("&2To see a different page use the parameter: 'pg <number>'\n");
			} else {
				sb.append("&2To change the results per page use the parameter: 'l <number>'");
			}
		}
	}

	private void helpHeader() {
		sb.append("\n");
		sb.append("&2");
		sb.append(pdf.getName());
		sb.append(" ");
		sb.append(pdf.getVersion());
		sb.append(" by ");
		sb.append(pdf.getAuthors().get(0));
		sb.append("\n");
		sb.append("\n");
	}

	/**
	 * This function takes an arg array and sorts it into a map of all arguments
	 *
	 * @return Argument map.
	 */
	private void parseArgs() {
		String mode = "";
		StringBuilder reasonb = new StringBuilder();
		Set<String> playerKeys = Sets.newHashSet("player", "players", "p"),
				typeKeys = Sets.newHashSet("type", "t"),
				reasonKeys = Sets.newHashSet("reason", "reasons", "r"),
				numberKeys = Sets.newHashSet("num", "number", "numbers", "n"),
				limitKeys = Sets.newHashSet("limit", "l"),
				decayKeys = Sets.newHashSet("decay", "decayed", "d"),
				pageKeys = Sets.newHashSet("page", "pg"),
				executeKeys = Sets.newHashSet("execute", "e");

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
			} else if (executeKeys.contains(check)) {
				mode = "e";
			} else {

				switch (mode) {
					case "p":
						OfflinePlayer pl = Bukkit.getOfflinePlayer(check);
						if (pl.hasPlayedBefore())
							players.add(check);
						break;
					case "t":
						type = check;
						break;
					case "r":
						reasonb.append(check + " ");
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
						if (check.equalsIgnoreCase("no")
								|| check.equalsIgnoreCase("false"))
							decay = false;
						break;
					case "e":
						if (check.equalsIgnoreCase("no")
								|| check.equalsIgnoreCase("false"))
							execute = false;
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

		reason = reasonb.toString();
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
