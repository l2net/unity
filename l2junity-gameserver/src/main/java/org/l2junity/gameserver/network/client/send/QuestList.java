/*
 * Copyright (C) 2004-2015 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.network.client.send;

import java.util.List;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class QuestList implements IClientOutgoingPacket
{
	private final List<Quest> _quests;
	private final PlayerInstance _player;
	
	public QuestList(PlayerInstance player)
	{
		_player = player;
		_quests = player.getAllActiveQuests();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.QUEST_LIST.writeId(packet);
		packet.writeH(_quests.size());
		for (Quest quest : _quests)
		{
			packet.writeD(quest.getId());
			
			final QuestState qs = _player.getQuestState(quest.getName());
			if (qs == null)
			{
				packet.writeD(0);
				continue;
			}
			
			final int states = qs.getInt("__compltdStateFlags");
			if (states != 0)
			{
				packet.writeD(states);
			}
			else
			{
				packet.writeD(qs.getCond());
			}
		}

		final byte[] oneTimeQuestMask = new byte[128];
		for (QuestState questState : _player.getAllQuestStates())
		{
			if (questState.isCompleted())
			{
				final int questId = questState.getQuest().getId();
				if (questId < 0 || (questId > 255 && questId < 10256) || questId > 11023)
				{
					continue;
				}

				oneTimeQuestMask[(questId % 10000) / 8] |= 1 << (questId % 8);
			}
		}
		packet.writeB(oneTimeQuestMask);

		return true;
	}
}
