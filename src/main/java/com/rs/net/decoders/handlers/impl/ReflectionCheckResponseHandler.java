// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.net.decoders.handlers.impl;

import java.util.ArrayList;

import com.rs.game.World;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.game.Rights;
import com.rs.lib.net.packets.PacketHandler;
import com.rs.lib.net.packets.decoders.ReflectionCheckResponse;
import com.rs.lib.util.reflect.ReflectionCheck;
import com.rs.utils.reflect.ReflectionAnalysis;
import com.rs.utils.reflect.ReflectionTest;

public class ReflectionCheckResponseHandler implements PacketHandler<Player, ReflectionCheckResponse> {

	@Override
	public void handle(Player player, ReflectionCheckResponse packet) {
		ReflectionAnalysis analysis = player.getReflectionAnalysis(packet.getId());
		if (analysis == null) {
			World.sendWorldMessage("<col=FF0000>" + player.getDisplayName() + " failed reflection check. Reason: Check id not found.", true);
			return;
		}
		
		for (ReflectionCheck check : analysis.getChecks().getChecks())
			check.decode(packet.getData());
		
		for (Player staff : World.getPlayers()) {
			if (staff == null || !staff.hasStarted() || staff.hasFinished() || !staff.hasRights(Rights.ADMIN))
				continue;
			ArrayList<String> lines = new ArrayList<>();
			for (ReflectionTest check : analysis.getTests()) {
				boolean pass = check.getValidation().apply(check.getCheck());
				lines.add("<u>" + check.getName() + "</u> (" + check.getDescription() + "): <shad=000000><col=" + (pass ? "00FF00>passed</col></shad>" : "FF0000>failed</col></shad>"));
				lines.add(check.getCheck().getResponse().toString());
				lines.add("");
			}
			staff.getInterfaceManager().sendInterface(275);
			staff.getPackets().sendRunScriptReverse(1207, lines.size());
			staff.getPackets().setIFText(275, 1, "Client Analysis Results for " + player.getDisplayName());
	        for (int i = 10; i < 289; i++)
	        	staff.getPackets().setIFText(275, i, ((i - 10) >= lines.size() ? " " : lines.get(i - 10)));
		}
	}

}
