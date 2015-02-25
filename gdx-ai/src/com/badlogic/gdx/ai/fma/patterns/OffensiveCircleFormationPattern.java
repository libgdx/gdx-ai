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

package com.badlogic.gdx.ai.fma.patterns;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;

/** The offensive circle posts members around the circumference of a circle, so their fronts are to the center of the circle. The
 * circle can consist of any number of members. Although a huge number of members might look silly, this implementation doesn't
 * put any fixed limit.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class OffensiveCircleFormationPattern<T extends Vector<T>> extends DefensiveCircleFormationPattern<T> {

	/** Creates a {@code OffensiveCircleFormationPattern}
	 * @param memberRadius */
	public OffensiveCircleFormationPattern (float memberRadius) {
		super(memberRadius);
	}

	@Override
	public Location<T> calculateSlotLocation (Location<T> outLocation, int slotNumber) {
		super.calculateSlotLocation(outLocation, slotNumber);
		outLocation.setOrientation(outLocation.getOrientation() + MathUtils.PI);
		return outLocation;
	}

}
