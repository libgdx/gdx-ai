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

package com.badlogic.gdx.ai.fma;

import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** {@code FreeSlotAssignmentStrategy} is the simplest implementation of {@link SlotAssignmentStrategy}. It simply go through
 * each assignment in the list and assign sequential slot numbers. The number of slots is just the length of the list.
 * <p>
 * Because each member can occupy any slot this implementation does not support roles.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class FreeSlotAssignmentStrategy<T extends Vector<T>> implements SlotAssignmentStrategy<T> {

	@Override
	public void updateSlotAssignments (Array<SlotAssignment<T>> assignments) {
		// A very simple assignment algorithm: we simply go through
		// each assignment in the list and assign sequential slot numbers
		for (int i = 0; i < assignments.size; i++)
			assignments.get(i).slotNumber = i;
	}

	@Override
	public int calculateNumberOfSlots (Array<SlotAssignment<T>> assignments) {
		return assignments.size;
	}

	@Override
	public void removeSlotAssignment (Array<SlotAssignment<T>> assignments, int index) {
		assignments.removeIndex(index);
	}

}
