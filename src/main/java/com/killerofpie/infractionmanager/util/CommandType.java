package com.killerofpie.infractionmanager.util;

import java.util.Arrays;
import java.util.List;

public enum CommandType {

	VIEW(Arrays.asList("CHECK", "VIEW")),
	CREATE(Arrays.asList("NEW", "CREATE")),
	HELP(Arrays.asList("HELP")),
	RELOAD(Arrays.asList("RELOAD")),
	RESET(Arrays.asList("RESET")),
	REMOVE(Arrays.asList("REMOVE", "DELETE")),
	TYPE(Arrays.asList("TYPE", "TYPES")),
	PARAMS(Arrays.asList("PARAM", "PARAMS")),
	IMPORT(Arrays.asList("IMPORT")),
	COMMANDS(Arrays.asList("COMMANDS", "CMDS", "COMMAND")),
	NONE(Arrays.asList(""));

	List<String> commands;

	CommandType(List<String> commands) {
		this.commands = commands;
	}

	public List<String> getCommands() {
		return commands;
	}

	public static CommandType getCommandType(String cmd) {
		for (CommandType cmdType : values()) {
			if (cmdType.getCommands().contains(cmd.toUpperCase())) {
				return cmdType;
			}
		}

		return NONE;
	}
}
