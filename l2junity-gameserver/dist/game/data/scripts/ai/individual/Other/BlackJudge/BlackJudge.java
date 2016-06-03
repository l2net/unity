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
package ai.individual.Other.BlackJudge;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

import ai.AbstractNpcAI;

/**
 * Black Judge AI.
 * @author St3eT
 */
public final class BlackJudge extends AbstractNpcAI
{
	// NPC
	private static final int BLACK_JUDGE = 30981;
	
	private BlackJudge()
	{
		addStartNpc(BLACK_JUDGE);
		addTalkId(BLACK_JUDGE);
		addFirstTalkId(BLACK_JUDGE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		
		if (event.equals("weakenBreath"))
		{
			if (player.getShilensBreathDebuffLevel() >= 3)
			{
				player.setShilensBreathDebuffLevel(2);
				htmltext = "30981-01.html";
			}
			else
			{
				htmltext = "30981-02.html";
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new BlackJudge();
	}
}