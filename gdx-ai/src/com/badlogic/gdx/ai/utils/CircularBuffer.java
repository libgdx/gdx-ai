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

	/** Creates a resizable {@code CircularBuffer}. */
	public CircularBuffer () {
		this(16, true);
	}

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
			T item = items[head];
			items[head] = null; // Avoid keeping useless references
			if (++head == items.length) head = 0;
			return item;
		}

		return null;
	}

	/** Removes all items from this circular buffer. */
	public void clear () {
		final T[] items = this.items;
		if (tail > head) {
			int i = head, n = tail;
			do {
				items[i++] = null;
			} while (i < n);
		} else if (size > 0) { // NOTE: when head == tail the buffer can be empty or full
			for (int i = head, n = items.length; i < n; i++)
				items[i] = null;
			for (int i = 0, n = tail; i < n; i++)
				items[i] = null;
		}
		this.head = 0;
		this.tail = 0;
		this.size = 0;
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

	/** Increases the size of the backing array (if necessary) to accommodate the specified number of additional items. Useful
	 * before adding many items to avoid multiple backing array resizes.
	 * @param additionalCapacity the number of additional items */
	public void ensureCapacity (int additionalCapacity) {
		int newCapacity = size + additionalCapacity;
		if (items.length < newCapacity) resize(newCapacity);
	}

	/** Creates a new backing array with the specified capacity containing the current items.
	 * @param newCapacity the new capacity */
	protected void resize (int newCapacity) {
		@SuppressWarnings("unchecked")
		T[] newItems = (T[])ArrayReflection.newInstance(items.getClass().getComponentType(), newCapacity);
		if (tail > head) {
			System.arraycopy(items, head, newItems, 0, size);
		} else if (size > 0) { // NOTE: when head == tail the buffer can be empty or full
			System.arraycopy(items, head, newItems, 0, items.length - head);
			System.arraycopy(items, 0, newItems, items.length - head, tail);
		}
		head = 0;
		tail = size;
		items = newItems;
	}
}
