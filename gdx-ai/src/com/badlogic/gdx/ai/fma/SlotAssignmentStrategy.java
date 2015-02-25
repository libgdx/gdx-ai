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

/** This interface defines how each {@link FormationMember} is assigned to a slot in the {@link Formation}.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public interface SlotAssignmentStrategy<T extends Vector<T>> {

	/** Updates the assignment of members to slots */
	public void updateSlotAssignments (Array<SlotAssignment<T>> assignments);

	/** Calculates the number of slots from the assignment data. */
	public int calculateNumberOfSlots (Array<SlotAssignment<T>> assignments);

	/** Removes the slot assignment at the specified index. */
	public void removeSlotAssignment (Array<SlotAssignment<T>> assignments, int index);

}
