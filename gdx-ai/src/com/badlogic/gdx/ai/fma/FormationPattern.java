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

/** The {@code FormationPattern} interface represents the shape of a formation and generates the slot offsets, relative to its
 * anchor point. Since formations can be scalable the pattern must be able to determine if a given number of slots is supported.
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

	/** Returns the location of the given slot index. */
	public Location<T> calculateSlotLocation (Location<T> outLocation, int slotNumber);

	/** Returns true if the pattern can support the given number of slots
	 * @param slotCount the number of slots
	 * @return {@code true} if this pattern can support the given number of slots; {@code false} othervwise. */
	public boolean supportsSlots (int slotCount);
}
