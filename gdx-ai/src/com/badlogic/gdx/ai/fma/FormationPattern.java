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

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** The {@code FormationPattern} interface represents the shape of a formation and generates the slot offsets, relative to its
 * anchor point. It does this after being asked for its drift offset, given a set of assignments. In calculating the drift offset,
 * the pattern works out which slots are needed. If the formation is scalable and returns different slot locations depending on
 * the number of slots occupied, it can use the slot assignments passed into the
 * {@link FormationPattern#calculateDriftOffset(Location, Array) calculateDriftOffset(outLocation, slotAssignments)} method to work
 * out how many slots are used and therefore what positions each slot should occupy.
 * <p>
 * Each particular pattern (such as a V, wedge, circle) needs its own instance of a class that implements this
 * {@code FormationPattern} interface.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public interface FormationPattern<T extends Vector<T>> {

	/** Sets the number of slots.
	 * @param numberOfSlots the number of slots to set */
	public void setNumberOfSlots (int numberOfSlots);

	/** Calculates the drift offset when members are in the given set of slots.
	 * @param outLocation the output location set to the calculated drift offset
	 * @param slotAssignments the set of slots
	 * @return the given {@code outLocation} for chaining. */
	public Location<T> calculateDriftOffset (Location<T> outLocation, Array<SlotAssignment<T>> slotAssignments);

	/** Returns the location of the given slot index. */
	public Location<T> calculateSlotLocation (Location<T> outLocation, int slotNumber);

	/** Returns true if the pattern can support the given number of slots
	 * @param slotCount the number of slots
	 * @return {@code true} if this pattern can support the given number of slots; {@code false} othervwise. */
	public boolean supportsSlots (int slotCount);
}
