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
package handlers.targethandlers.affectscope;

import java.util.function.Consumer;

import org.l2junity.gameserver.handler.AffectObjectHandler;
import org.l2junity.gameserver.handler.IAffectObjectHandler;
import org.l2junity.gameserver.handler.IAffectScopeHandler;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.skills.targets.AffectScope;
import org.l2junity.gameserver.model.skills.targets.TargetType;

/**
 * Single target affect scope implementation.
 * @author Nik
 */
public class Single implements IAffectScopeHandler
{
	@Override
	public void forEachAffected(Creature activeChar, WorldObject target, Skill skill, Consumer<? super WorldObject> action)
	{
		final IAffectObjectHandler affectObject = AffectObjectHandler.getInstance().getHandler(skill.getAffectObject());
		
		if (target.isCreature())
		{
			if (skill.getTargetType() == TargetType.GROUND)
			{
				action.accept(activeChar); // Return yourself to mark that effects can use your current skill's world position.
			}
			if (((affectObject == null) || affectObject.checkAffectedObject(activeChar, (Creature) target)))
			{
				action.accept(target); // Return yourself to mark that effects can use your current skill's world position.
			}
		}
		else if (target.isItem())
		{
			action.accept(target); // Return yourself to mark that effects can use your current skill's world position.
		}
	}
	
	@Override
	public Enum<AffectScope> getAffectScopeType()
	{
		return AffectScope.SINGLE;
	}
}