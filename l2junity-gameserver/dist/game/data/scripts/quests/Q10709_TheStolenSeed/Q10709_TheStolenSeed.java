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
package quests.Q10709_TheStolenSeed;

import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import quests.Q10403_TheGuardianGiant.Q10403_TheGuardianGiant;

/**
 * The Stolen Seed (10709)
 * @author St3eT
 */
public final class Q10709_TheStolenSeed extends Quest
{
	// NPCs
	private static final int NOVIAN = 33866;
	private static final int CONTROL_DEVICE = 33961; // Magic Circle Control Device
	private static final int REMEMBERED_AKUM = 27524; // Remembered Giant Akum
	private static final int REMEMBERED_EMBRYO = 27525; // Remembered Embryo
	private static final int CURSED_AKUM = 27520; // Cursed Giant Akum
	// Items
	private static final int FRAGMENT = 39511; // Normal Fragment
	private static final int MEMORY_FRAGMENT = 39510; // Akum's Memory Fragment
	private static final int EAB = 948; // Scroll: Enchant Armor (B-grade)
	// Misc
	private static final int MIN_LEVEL = 58;
	private static final int MAX_LEVEL = 61;
	
	public Q10709_TheStolenSeed()
	{
		super(10709);
		addStartNpc(NOVIAN);
		addTalkId(NOVIAN, CONTROL_DEVICE);
		addKillId(CURSED_AKUM);
		registerQuestItems(FRAGMENT, MEMORY_FRAGMENT);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33866-08.htm");
		addCondCompletedQuest(Q10403_TheGuardianGiant.class.getSimpleName(), "33866-08.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		
		if (event.equals("action"))
		{
			if ((st != null) && st.isCond(1))
			{
				// Take items
				takeItems(player, MEMORY_FRAGMENT, -1);
				
				// Spawn + chat
				final Npc akum = addSpawn(REMEMBERED_AKUM, npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0, false, 0);
				akum.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.ARGH_WHO_IS_HIDING_THERE);
				final Npc embryo = addSpawn(REMEMBERED_EMBRYO, akum.getX() + 100, akum.getY() + 100, akum.getZ(), 0, false, 0);
				embryo.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.A_SMART_GIANT_HUH_WELL_HAND_IT_OVER_THE_KARTIA_S_SEED_IS_OURS);
				
				// Attack + invul
				akum.reduceCurrentHp(1, embryo, null);
				embryo.reduceCurrentHp(1, akum, null); // TODO: Find better way for attack
				
				embryo.setIsInvul(true);
				akum.setIsInvul(true);
				
				getTimers().addTimer("EMBRYO_DELAY", 3000, embryo, player);
			}
			else
			{
				return "33961-01.html";
			}
		}
		
		if (st == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33866-02.htm":
			case "33866-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33866-04.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "33866-07.html":
			{
				if (st.isCond(3))
				{
					st.exitQuest(false, true);
					giveItems(player, EAB, 5);
					giveStoryQuestReward(player, 30);
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 731_010, 175);
					}
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		if (event.equals("EMBRYO_DELAY"))
		{
			final Npc akum = (Npc) npc.getTarget();
			final QuestState st = getQuestState(player, true);
			
			if ((akum != null) && (st != null))
			{
				st.setCond(2, true);
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.KARTIA_S_SEED_GOT_IT);
				akum.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.ARGHH);
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOU_WORTHLESS_GIANT_CURSE_YOU_FOR_ETERNITY);
				addSpawn(CURSED_AKUM, akum);
				npc.deleteMe();
				akum.deleteMe();
			}
		}
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == NOVIAN)
				{
					htmltext = "33866-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == NOVIAN)
				{
					switch (st.getCond())
					{
						case 1:
						case 2:
						{
							htmltext = "33866-05.html";
							break;
						}
						case 3:
						{
							htmltext = "33866-06.html";
							break;
						}
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == NOVIAN)
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState st = getQuestState(killer, false);
		
		if ((st != null) && st.isStarted() && st.isCond(2))
		{
			st.setCond(3, true);
			giveItems(killer, FRAGMENT, 1);
		}
		return super.onKill(npc, killer, isSummon);
	}
}