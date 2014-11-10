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

/** A circular buffer.
 * 
 * @author davebaol */
public class CircularBuffer<T> {
	private T[] items;
	public boolean resizable;
	private int head;
	private int tail;

	public CircularBuffer (int initialCapacity) {
		this(initialCapacity, true);
	}

	@SuppressWarnings("unchecked")
	public CircularBuffer (int initialCapacity, boolean resizable) {
		this.items = (T[])new Object[initialCapacity];
		this.resizable = resizable;
		this.head = 0;
		this.tail = 0;
	}

	public boolean store (T value) {
		if (isFull()) {
			if (!resizable) return false;

			// Resize this queue
			resize(Math.max(8, (int)(items.length * 1.75f)));
		}
		items[tail++] = value;
		if (tail == items.length) tail = 0;
		return true;
	}

	public T read () {
		if (head != tail) {
			T value = items[head++];
			if (head == items.length) head = 0;
			return value;
		}

		return null;
	}

	public boolean isEmpty () {
		return head == tail;
	}

	public int size () {
		return tail >= head ? tail - head : items.length - head + tail;
	}

	private boolean isFull () {
		if (tail + 1 == head) return true;
		if (tail == items.length - 1 && head == 0) return true;
		return false;
	}

	/** Creates a new backing array with the specified size containing the current items. */
	protected void resize (int newSize) {
		System.out.println("resize: " + newSize);
		T[] items = this.items;
		@SuppressWarnings("unchecked")
		T[] newItems = (T[])ArrayReflection.newInstance(items.getClass().getComponentType(), newSize);
		if (tail >= head) {
			System.arraycopy(items, head, newItems, 0, tail - head - 1);
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
