/*
 * Copyright (C) 2004-2014 L2J Unity
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
package org.l2junity.network;

import io.netty.buffer.ByteBuf;

/**
 * @author Nos
 */
public final class PacketWriter
{
	private final ByteBuf _buf;
	
	public PacketWriter(ByteBuf buf)
	{
		_buf = buf;
	}
	
	/**
	 * Gets the writable bytes.
	 * @return the writable bytes
	 */
	public int getWritableBytes()
	{
		return _buf.writableBytes();
	}
	
	/**
	 * Writes a byte.
	 * @param value the byte (The 24 high-order bits are ignored)
	 */
	public void writeC(int value)
	{
		_buf.writeByte(value);
	}
	
	/**
	 * Writes a short.
	 * @param value the short (The 16 high-order bits are ignored)
	 */
	public void writeH(int value)
	{
		_buf.writeShort(value);
	}
	
	/**
	 * Writes an integer.
	 * @param value the integer
	 */
	public void writeD(int value)
	{
		_buf.writeInt(value);
	}
	
	/**
	 * Writes a long.
	 * @param value the long
	 */
	public void writeQ(long value)
	{
		_buf.writeLong(value);
	}
	
	/**
	 * Writes a float.
	 * @param value the float
	 */
	public void writeE(float value)
	{
		_buf.writeFloat(value);
	}
	
	/**
	 * Writes a double.
	 * @param value the double
	 */
	public void writeF(double value)
	{
		_buf.writeDouble(value);
	}
	
	/**
	 * Writes a string.
	 * @param value the string
	 */
	public void writeS(String value)
	{
		if (value != null)
		{
			for (int i = 0; i < value.length(); i++)
			{
				_buf.writeChar(value.charAt(i));
			}
		}
		
		_buf.writeChar(0);
	}
	
	/**
	 * Writes a string with fixed length specified as [short length, char[length] data].
	 * @param value the string
	 */
	public void writeString(String value)
	{
		if (value != null)
		{
			_buf.writeShort(value.length());
			for (int i = 0; i < value.length(); i++)
			{
				_buf.writeChar(value.charAt(i));
			}
		}
		else
		{
			_buf.writeShort(0);
		}
	}
	
	/**
	 * Writes a byte array.
	 * @param bytes the byte array
	 */
	public void writeB(byte[] bytes)
	{
		_buf.writeBytes(bytes);
	}
}
