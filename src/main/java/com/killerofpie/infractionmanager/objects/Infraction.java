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

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class Infraction {

	private UUID[] players;
	private LocalDate time;
	private String reason;
	private InfractionType type;

	public Infraction(String type, UUID[] players, LocalDate time, String reason) {
		this.type = new InfractionType(type);
		this.players = players;
		this.time = time;
		this.reason = reason;
	}

	public Infraction(String type, UUID player, LocalDate time, String reason) {
		this.type = new InfractionType(type);
		this.players[0] = player;
		this.time = time;
		this.reason = reason;
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

	public Map<String, Object> toMap() {
		Map<String, Object> tempMap = new TreeMap<>();
		tempMap.put("type", type.getName());
		tempMap.put("players", players);
		tempMap.put("reason", reason);
		tempMap.put("time", time.toString());

		return tempMap;
	}

	public static Infraction fromMap(Map<String, Object> map) {
		UUID[] players = (UUID[]) map.get("players");
		LocalDate time = LocalDate.parse(map.get("time").toString());
		String reason = map.get("reason").toString();
		String type = map.get("type").toString();

		return new Infraction(type, players, time, reason);
	}
}
