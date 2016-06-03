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

import org.l2junity.gameserver.model.Shortcut;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class ShortCutRegister implements IClientOutgoingPacket
{
	private final Shortcut _shortcut;
	
	/**
	 * Register new skill shortcut
	 * @param shortcut
	 */
	public ShortCutRegister(Shortcut shortcut)
	{
		_shortcut = shortcut;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SHORT_CUT_REGISTER.writeId(packet);
		
		packet.writeD(_shortcut.getType().ordinal());
		packet.writeD(_shortcut.getSlot() + (_shortcut.getPage() * 12)); // C4 Client
		switch (_shortcut.getType())
		{
			case ITEM:
			{
				packet.writeD(_shortcut.getId());
				packet.writeD(_shortcut.getCharacterType());
				packet.writeD(_shortcut.getSharedReuseGroup());
				packet.writeD(0x00); // unknown
				packet.writeD(0x00); // unknown
				packet.writeD(0x00); // item augment id
				packet.writeD(0x00); // TODO: Find me, item visual id ?
				break;
			}
			case SKILL:
			{
				packet.writeD(_shortcut.getId());
				packet.writeH(_shortcut.getLevel());
				packet.writeH(0x00); // Sub level
				packet.writeD(_shortcut.getSharedReuseGroup());
				packet.writeC(0x00); // C5
				packet.writeD(_shortcut.getCharacterType());
				packet.writeD(0x00); // TODO: Find me
				packet.writeD(0x00); // TODO: Find me
				break;
			}
			case ACTION:
			case MACRO:
			case RECIPE:
			case BOOKMARK:
			{
				packet.writeD(_shortcut.getId());
				packet.writeD(_shortcut.getCharacterType());
			}
		}
		return true;
	}
}
