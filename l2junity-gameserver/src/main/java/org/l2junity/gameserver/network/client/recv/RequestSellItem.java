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
package org.l2junity.gameserver.network.client.recv;

import static org.l2junity.gameserver.model.actor.Npc.INTERACTION_DISTANCE;
import static org.l2junity.gameserver.model.itemcontainer.Inventory.MAX_ADENA;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.BuyListData;
import org.l2junity.gameserver.enums.TaxType;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.L2MerchantInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.buylist.ProductList;
import org.l2junity.gameserver.model.holders.UniqueItemHolder;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.ExBuySellList;
import org.l2junity.gameserver.network.client.send.ExUserInfoInvenWeight;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * RequestSellItem client packet class.
 */
public final class RequestSellItem implements IClientIncomingPacket
{
	private static final int BATCH_LENGTH = 16;
	
	private int _listId;
	private List<UniqueItemHolder> _items = null;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_listId = packet.readD();
		int size = packet.readD();
		if ((size <= 0) || (size > Config.MAX_ITEM_IN_PACKET) || ((size * BATCH_LENGTH) != packet.getReadableBytes()))
		{
			return false;
		}
		
		_items = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
		{
			int objectId = packet.readD();
			int itemId = packet.readD();
			long count = packet.readQ();
			if ((objectId < 1) || (itemId < 1) || (count < 1))
			{
				_items = null;
				return false;
			}
			_items.add(new UniqueItemHolder(itemId, objectId, count));
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			player.sendMessage("You are buying too fast.");
			return;
		}
		
		if (_items == null)
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (player.getReputation() < 0))
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		WorldObject target = player.getTarget();
		Creature merchant = null;
		if (!player.isGM())
		{
			if ((target == null) || (!player.isInsideRadius(target, INTERACTION_DISTANCE, true, false)) || (player.getInstanceWorld() != target.getInstanceWorld()))
			{
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (target instanceof L2MerchantInstance)
			{
				merchant = (Creature) target;
			}
			else
			{
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if ((merchant == null) && !player.isGM())
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final ProductList buyList = BuyListData.getInstance().getBuyList(_listId);
		if (buyList == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (merchant != null)
		{
			if (!buyList.isNpcAllowed(merchant.getId()))
			{
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		long totalPrice = 0;
		// Proceed the sell
		for (UniqueItemHolder i : _items)
		{
			ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "sell");
			if ((item == null) || (!item.isSellable()))
			{
				continue;
			}
			
			long price = item.getReferencePrice() / 2;
			totalPrice += price * i.getCount();
			if (((MAX_ADENA / i.getCount()) < price) || (totalPrice > MAX_ADENA))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (Config.ALLOW_REFUND)
			{
				item = player.getInventory().transferItem("Sell", i.getObjectId(), i.getCount(), player.getRefund(), player, merchant);
			}
			else
			{
				item = player.getInventory().destroyItem("Sell", i.getObjectId(), i.getCount(), player, merchant);
			}
		}
		
		// add to castle treasury
		if (merchant instanceof L2MerchantInstance)
		{
			final L2MerchantInstance npc = ((L2MerchantInstance) merchant);
			final long taxCollection = (long) (totalPrice * npc.getMpc().getTotalTaxRate(TaxType.SELL));
			npc.getCastle().addToTreasury(taxCollection);
			totalPrice -= taxCollection;
		}
		
		player.addAdena("Sell", totalPrice, merchant, false);
		
		// Update current load as well
		client.sendPacket(new ExUserInfoInvenWeight(player));
		client.sendPacket(new ExBuySellList(player, true));
	}
}
