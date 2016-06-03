/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.chathandlers;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.Config;
import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.handler.IChatHandler;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.CreatureSay;
import org.l2junity.gameserver.network.client.send.ExWorldChatCnt;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * World chat handler.
 * @author UnAfraid
 */
public final class ChatWorld implements IChatHandler
{
	private static final Map<Integer, Instant> REUSE = new ConcurrentHashMap<>();
	
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.WORLD,
	};
	
	@Override
	public void handleChat(ChatType type, PlayerInstance activeChar, String target, String text)
	{
		final Instant now = Instant.now();
		if (!REUSE.isEmpty())
		{
			REUSE.values().removeIf(now::isAfter);
		}
		
		if (activeChar.getLevel() < Config.WORLD_CHAT_MIN_LEVEL)
		{
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_USE_WORLD_CHAT_FROM_LV_S1);
			msg.addInt(Config.WORLD_CHAT_MIN_LEVEL);
			activeChar.sendPacket(msg);
		}
		else if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER);
		}
		else if (activeChar.getWorldChatPoints() < 1)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_SPENT_YOUR_WORLD_CHAT_QUOTA_FOR_THE_DAY_A_NEW_DAY_STARTS_EVERY_DAY_AT_18_30);
		}
		else
		{
			// Verify if player is not spaming.
			if (Config.WORLD_CHAT_INTERVAL.getSeconds() > 0)
			{
				final Instant instant = REUSE.getOrDefault(activeChar.getObjectId(), null);
				if ((instant != null) && instant.isAfter(now))
				{
					final Duration timeDiff = Duration.between(now, instant);
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_S1_SEC_UNTIL_YOU_ARE_ABLE_TO_USE_WORLD_CHAT);
					msg.addInt((int) timeDiff.getSeconds());
					activeChar.sendPacket(msg);
					return;
				}
			}
			
			final CreatureSay cs = new CreatureSay(activeChar, type, text);
			World.getInstance().getPlayers().stream().filter(activeChar::isNotBlocked).forEach(cs::sendTo);
			
			activeChar.setWorldChatPoints(activeChar.getWorldChatPoints() - 1);
			activeChar.sendPacket(new ExWorldChatCnt(activeChar));
			if (Config.WORLD_CHAT_INTERVAL.getSeconds() > 0)
			{
				REUSE.put(activeChar.getObjectId(), now.plus(Config.WORLD_CHAT_INTERVAL));
			}
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}