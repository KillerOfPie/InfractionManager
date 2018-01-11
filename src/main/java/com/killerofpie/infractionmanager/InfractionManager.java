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

import com.killerofpie.infractionmanager.configs.TypeConfig;
import com.killerofpie.infractionmanager.util.BukkitVersion;
import com.killerofpie.infractionmanager.util.Updater;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class InfractionManager extends JavaPlugin {
	private final BukkitVersion VERSION = new BukkitVersion();
	private Metrics metrics;
	private TypeConfig typeConfig;

	public TypeConfig getTypeConfig() {
		return typeConfig;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		typeConfig = new TypeConfig(this);

		getCommand("infraction").setExecutor(new Executor(this));

		if (getConfig().getBoolean("check-for-updates")) {
			new Thread(() -> new Updater(getDescription()).checkCurrentVersion()).start();
		}

		if (getConfig().getBoolean("allow-metrics")) {
			metrics = new Metrics(this);
			getLogger().info("Metrics successfully initialized!");

		} else {
			getLogger().warning("Metrics are disabled! Please consider enabling them to support the authors!");
		}
	}

	@Override
	public void onDisable() {

	}
}