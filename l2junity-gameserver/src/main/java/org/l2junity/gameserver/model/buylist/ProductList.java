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
package org.l2junity.gameserver.model.buylist;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author NosBit
 */
public final class ProductList
{
	private final int _listId;
	private final Map<Integer, Product> _products = new LinkedHashMap<>();
	private Set<Integer> _allowedNpcs = null;
	
	public ProductList(int listId)
	{
		_listId = listId;
	}
	
	public int getListId()
	{
		return _listId;
	}
	
	public Collection<Product> getProducts()
	{
		return _products.values();
	}
	
	public Product getProductByItemId(int itemId)
	{
		return _products.get(itemId);
	}
	
	public void addProduct(Product product)
	{
		_products.put(product.getItemId(), product);
	}
	
	public void addAllowedNpc(int npcId)
	{
		if (_allowedNpcs == null)
		{
			_allowedNpcs = new HashSet<>();
		}
		_allowedNpcs.add(npcId);
	}
	
	public boolean isNpcAllowed(int npcId)
	{
		if (_allowedNpcs == null)
		{
			return false;
		}
		return _allowedNpcs.contains(npcId);
	}
}
