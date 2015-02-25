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

import com.badlogic.gdx.ai.fma.FormationPattern;
import com.badlogic.gdx.ai.fma.SlotAssignment;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** The defensive circle posts members around the circumference of a circle, so their backs are to the center of the circle. The
 * circle can consist of any number of members. Although a huge number of members might look silly, this implementation doesn't
 * put any fixed limit.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class DefensiveCircleFormationPattern<T extends Vector<T>> implements FormationPattern<T> {
	/** The number of slots currently in the pattern. */
	int numberOfSlots;

	/** The radius of one member. This is needed to determine how close we can pack a given number of members around circle. */
	float memberRadius;

	private Location<T> tempLocation;
	private float centerOfMassOrientation;

	/** Creates a {@code DefensiveCircleFormationPattern}
	 * @param memberRadius */
	public DefensiveCircleFormationPattern (float memberRadius) {
		this.memberRadius = memberRadius;
	}

	@Override
	public void setNumberOfSlots (int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	@Override
	public Location<T> calculateDriftOffset (Location<T> centerOfMass, Array<SlotAssignment<T>> assignments) {

		// Clear the center of mass
		centerOfMass.getPosition().setZero();
		centerOfMassOrientation = 0;

		// Make sure tempLocation is instantiated
		if (tempLocation == null) tempLocation = centerOfMass.newLocation();

		T centerOfMassPos = centerOfMass.getPosition();
		T tempLocationPos = tempLocation.getPosition();

		// Go through each assignment and add its contribution to the center
		float numberOfAssignments = assignments.size;
		for (int i = 0; i < numberOfAssignments; i++) {
			calculateSlotLocation(tempLocation, assignments.get(i).slotNumber);
			centerOfMassPos.add(tempLocationPos);
			centerOfMassOrientation +=  tempLocation.getOrientation();
		}

		// Divide through to get the drift offset.
		centerOfMassPos.scl(1f / numberOfAssignments);
		centerOfMassOrientation /= numberOfAssignments;
		centerOfMass.setOrientation(centerOfMassOrientation);

		return centerOfMass;
	}

	@Override
	public Location<T> calculateSlotLocation (Location<T> outLocation, int slotNumber) {
		// Place the slot around the circle based on its slot number
		float angleAroundCircle = (MathUtils.PI2 * slotNumber) / numberOfSlots;

		// The radius depends on the radius of the member,
		// and the number of members in the circle:
		// we want there to be no gap between member's shoulders.
		float radius = memberRadius / (float)Math.sin(Math.PI / numberOfSlots);

		// Fill location components based on the angle around circle.
		outLocation.angleToVector(outLocation.getPosition(), angleAroundCircle).scl(radius);

		// The members should be facing out
		outLocation.setOrientation(centerOfMassOrientation + angleAroundCircle);

		// Return the slot location
		return outLocation;
	}

	@Override
	public boolean supportsSlots (int slotCount) {
		// In this case we support any number of slots.
		return true;
	}

}
