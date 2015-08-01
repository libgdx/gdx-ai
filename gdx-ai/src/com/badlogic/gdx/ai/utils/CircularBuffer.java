/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.utils;

import com.badlogic.gdx.utils.reflect.ArrayReflection;

/** A circular buffer, possibly resizable.
 * 
 * @author davebaol */
public class CircularBuffer<T> {
	private T[] items;
	private boolean resizable;
	private int head;
	private int tail;
	private int size;

	/** Creates a resizable {@code CircularBuffer} with the given initial capacity.
	 * @param initialCapacity the initial capacity of this circular buffer */
	public CircularBuffer (int initialCapacity) {
		this(initialCapacity, true);
	}

	/** Creates a {@code CircularBuffer} with the given initial capacity.
	 * @param initialCapacity the initial capacity of this circular buffer
	 * @param resizable whether this buffer is resizable or has fixed capacity */
	@SuppressWarnings("unchecked")
	public CircularBuffer (int initialCapacity, boolean resizable) {
		this.items = (T[])new Object[initialCapacity];
		this.resizable = resizable;
		this.head = 0;
		this.tail = 0;
		this.size = 0;
	}

	/** Adds the given item to the tail of this circular buffer.
	 * @param item the item to add
	 * @return {@code true} if the item has been successfully added to this circular buffer; {@code false} otherwise. */
	public boolean store (T item) {
		if (size == items.length) {
			if (!resizable) return false;

			// Resize this queue
			resize(Math.max(8, (int)(items.length * 1.75f)));
		}
		size++;
		items[tail++] = item;
		if (tail == items.length) tail = 0;
		return true;
	}

	/** Removes and returns the item at the head of this circular buffer (if any).
	 * @return the item just removed or {@code null} if this circular buffer is empty. */
	public T read () {
		if (size > 0) {
			size--;
			T item = items[head++];
			if (head == items.length) head = 0;
			return item;
		}

		return null;
	}

	/** Returns {@code true} if this circular buffer is empty; {@code false} otherwise. */
	public boolean isEmpty () {
		return size == 0;
	}

	/** Returns {@code true} if this circular buffer contains as many items as its capacity; {@code false} otherwise. */
	public boolean isFull () {
		return size == items.length;
	}

	/** Returns the number of elements in this circular buffer. */
	public int size () {
		return size;
	}

	/** Returns {@code true} if this circular buffer can be resized; {@code false} otherwise. */
	public boolean isResizable () {
		return resizable;
	}

	/** Sets the flag specifying whether this circular buffer can be resized or not.
	 * @param resizable the flag */
	public void setResizable (boolean resizable) {
		this.resizable = resizable;
	}

	/** Creates a new backing array with the specified capacity containing the current items. */
	protected void resize (int newCapacity) {
		T[] items = this.items;
		@SuppressWarnings("unchecked")
		T[] newItems = (T[])ArrayReflection.newInstance(items.getClass().getComponentType(), newCapacity);
		if (tail >= head) {
			System.arraycopy(items, head, newItems, 0, size);
			tail = tail - head;
			head = 0;
		} else {
			System.arraycopy(items, head, newItems, 0, items.length - head);
			System.arraycopy(items, 0, newItems, items.length - head, tail);
			tail = items.length - head + tail;
			head = 0;
		}
		this.items = newItems;
	}
}
