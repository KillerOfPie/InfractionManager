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

package com.killerofpie.infractionmanager.objects;

import com.killerofpie.infractionmanager.util.InfractionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class Infraction {

	private UUID[] players;
	private String[] playersString;
	private LocalDate time;
	private String reason;
	private InfractionType type;
	private UUID sender;

	public Infraction(String type, UUID[] players, LocalDate time, String reason, UUID sender) {
		this.type = new InfractionType(type);
		this.players = players;
		this.time = time;
		this.reason = reason;
		this.sender = sender;

		uuidToStrings();
	}

	public Infraction(String type, UUID player, LocalDate time, String reason, UUID sender) {
		this.type = new InfractionType(type);
		this.players = new UUID[]{player};
		this.time = time;
		this.reason = reason;
		this.sender = sender;

		uuidToStrings();
	}

	public Infraction(String type, String[] players, LocalDate time, String reason, UUID sender) {
		this.type = new InfractionType(type);
		this.playersString = players;
		this.time = time;
		this.reason = reason;
		this.sender = sender;

		stringToUUIDs();
	}

	public Infraction(String type, String player, LocalDate time, String reason, UUID sender) {
		this.type = new InfractionType(type);
		this.playersString = new String[]{player};
		this.time = time;
		this.reason = reason;
		this.sender = sender;

		stringToUUIDs();
	}

	public UUID[] getPlayers() {
		return players;
	}

	public LocalDate getTime() {
		return time;
	}

	public String getReason() {
		return reason;
	}

	public InfractionType getType() {
		return type;
	}

	public UUID getSenderUUID() {
		return sender;
	}

	public Player getSenderPlayer() {
		return Bukkit.getPlayer(sender);
	}

	public Map<String, Object> toMap() {
		Map<String, Object> tempMap = new TreeMap<>();
		tempMap.put("type", type.getName());
		tempMap.put("players", playersString);
		tempMap.put("reason", reason);
		tempMap.put("time", time.toString());
		tempMap.put("sender", sender.toString());

		return tempMap;
	}

	public static Infraction fromMap(Map<String, Object> map) {
		ArrayList<String> pls = (ArrayList<String>) map.get("players");
		String[] players = new String[pls.size()];
		for (int i = 0; i < pls.size(); i++) {
			players[i] = pls.get(i);
		}

		LocalDate time = LocalDate.parse(map.get("time").toString());
		String reason = map.get("reason").toString();
		String type = map.get("type").toString();
		UUID sender = UUID.fromString(map.get("sender").toString());

		if (sender == null) {
			sender = Bukkit.getOfflinePlayer("Server").getUniqueId();
		}

		return new Infraction(type, players, time, reason, sender);
	}

	private void uuidToStrings() {
		playersString = new String[players.length];
		for (int i = 0; i < players.length; i++) {
			playersString[i] = players[i].toString();
		}
	}

	private void stringToUUIDs() {
		players = new UUID[playersString.length];
		for (int i = 0; i < playersString.length; i++) {
			players[i] = UUID.fromString(playersString[i]);
		}
	}
}
