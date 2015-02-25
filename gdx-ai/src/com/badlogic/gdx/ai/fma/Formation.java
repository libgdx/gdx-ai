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

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/** A {@code Formation} coordinates the movement of a group of characters so that they retain some group organization. Characters
 * belonging to a formation must implement the {@link FormationMember} interface. At its simplest, a formation can consist of
 * moving in a fixed geometric pattern such as a V or line abreast, but it is not limited to that. Formations can also make use of
 * the environment. Squads of characters can move between cover points using formation steering with only minor modifications, for
 * example.
 * <p>
 * Formation motion is used in team sports games, squad-based games, real-time strategy games, and sometimes in first-person
 * shooters, driving games, and action adventures too. It is a simple and flexible technique that is much quicker to write and
 * execute and can produce much more stable behavior than collaborative tactical decision making.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class Formation<T extends Vector<T>> {

	/** A list of slots assignments. */
	Array<SlotAssignment<T>> slotAssignments;

	/** The location representing the drift offset for the currently filled slots. */
	Location<T> driftOffset;

	/** The anchor point of this formation. */
	protected Location<T> anchor;

	/** The formation pattern */
	protected FormationPattern<T> pattern;

	/** The strategy used to assign a member to his slot */
	protected SlotAssignmentStrategy<T> slotAssignmentStrategy;

	private T anchorLessDriftPosition;
	private static final Matrix3 orientationMatrix = new Matrix3();

	/** Creates a {@code Formation} for the specified {@code pattern} using a {@link FreeSlotAssignmentStrategy}.
	 * @param anchor the anchor point of this formation, usually a {@link Steerable}. Cannot be {@code null}.
	 * @param pattern the pattern of this formation
	 * @throws IllegalArgumentException if the anchor point is {@code null} */
	public Formation (Location<T> anchor, FormationPattern<T> pattern) {
		this(anchor, pattern, new FreeSlotAssignmentStrategy<T>());
	}

	/** Creates a {@code Formation} for the specified {@code pattern} and {@code slotAssignmentStrategy}.
	 * @param anchor the anchor point of this formation, usually a {@link Steerable}. Cannot be {@code null}.
	 * @param pattern the pattern of this formation
	 * @param slotAssignmentStrategy the strategy used to assign a member to his slot
	 * @throws IllegalArgumentException if the anchor point is {@code null} */
	public Formation (Location<T> anchor, FormationPattern<T> pattern, SlotAssignmentStrategy<T> slotAssignmentStrategy) {
		if (anchor == null) throw new IllegalArgumentException("The anchor point cannot be null");
		this.anchor = anchor;
		this.pattern = pattern;
		this.slotAssignmentStrategy = slotAssignmentStrategy;

		this.slotAssignments = new Array<SlotAssignment<T>>();
		this.driftOffset = anchor.newLocation();
		this.anchorLessDriftPosition = anchor.getPosition().cpy();
	}

	/** Returns the current anchor point of the formation. This can be the location (i.e. position and orientation) of a leader
	 * member, a modified center of mass of the members in the formation, or an invisible but steered anchor point for a two-level
	 * steering system. */
	public Location<T> getAnchorPoint () {
		return anchor;
	}

	/** Sets the anchor point of the formation.
	 * @param anchor the anchor point to set */
	public void setAnchorPoint (Location<T> anchor) {
		this.anchor = anchor;
	}

	/** @return the pattern of this formation */
	public FormationPattern<T> getPattern () {
		return pattern;
	}

	/** Sets the pattern of this formation
	 * @param pattern the pattern to set */
	public void setPattern (FormationPattern<T> pattern) {
		this.pattern = pattern;
	}

	/** @return the slotAssignmentStrategy */
	public SlotAssignmentStrategy<T> getSlotAssignmentStrategy () {
		return slotAssignmentStrategy;
	}

	/** @param slotAssignmentStrategy the slotAssignmentStrategy to set */
	public void setSlotAssignmentStrategy (SlotAssignmentStrategy<T> slotAssignmentStrategy) {
		this.slotAssignmentStrategy = slotAssignmentStrategy;
	}

	/** Updates the assignment of members to slots */
	public void updateSlotAssignments () {
		// Apply the strategy to update slot assignments
		slotAssignmentStrategy.updateSlotAssignments(slotAssignments);

		// Set the newly calculated number of slots
		pattern.setNumberOfSlots(slotAssignmentStrategy.calculateNumberOfSlots(slotAssignments));

		// Update the drift offset
		pattern.calculateDriftOffset(driftOffset, slotAssignments);
	}

	/** Add a new member to the first available slot. Returns false if no more slots are available. */
	public boolean addMember (FormationMember<T> member) {
		// Find out how many slots we have occupied
		int occupiedSlots = slotAssignments.size;

		// Check if the pattern supports one more slot
		if (pattern.supportsSlots(occupiedSlots + 1)) {
			// Add a new slot assignment
			// TODO is occupiedSlots below correct? Maybe we have to find the first free slot.
			slotAssignments.add(new SlotAssignment<T>(member, occupiedSlots));

			// Update the slot assignments and return success
			updateSlotAssignments();
			return true;
		}

		return false;
	}

	/** Removes a member from its slot. */
	public void removeMember (FormationMember<T> member) {
		// Find the member's slot
		int slot = findMemberSlot(member);

		// Make sure we've found a valid result
		if (slot >= 0) {
			// Remove the slot
			//slotAssignments.removeIndex(slot);
			slotAssignmentStrategy.removeSlotAssignment(slotAssignments, slot);

			// Update the assignments
			updateSlotAssignments();
		}
	}

	private int findMemberSlot (FormationMember<T> member) {
		for (int i = 0; i < slotAssignments.size; i++) {
			if (slotAssignments.get(i).member == member) return i;
		}
		return -1;
	}

	// debug
	public SlotAssignment<T> getSlot (int index) {
		return slotAssignments.get(index);
	}

	// debug
	public int getSlotCount () {
		return slotAssignments.size;
	}

	/** Writes new slot locations to each member */
	public void updateSlots () {
		// Find the anchor point
		Location<T> anchor = getAnchorPoint();

		anchorLessDriftPosition.set(anchor.getPosition()).sub(driftOffset.getPosition());
		float anchorLessDriftOrientation = anchor.getOrientation() - driftOffset.getOrientation();

		// Get the orientation of the anchor point as a matrix
		orientationMatrix.idt().rotateRad(anchor.getOrientation());

		// Go through each member in turn
		for (int i = 0; i < slotAssignments.size; i++) {
			SlotAssignment<T> slotAssignment = slotAssignments.get(i);

			// Retrieve the location reference of the formation member to calculate the new value
			Location<T> relativeLoc = slotAssignment.member.getTargetLocation();

			// Ask for the location of the slot relative to the anchor point
			pattern.calculateSlotLocation(relativeLoc, slotAssignment.slotNumber);

			T relativeLocPosition = relativeLoc.getPosition();

// System.out.println("relativeLoc.position = " + relativeLocPosition);

// [17:31] <@Xoppa> davebaol, interface Transform<T extends Vector<T>> { T getTranslation(); T getScale(); float getRotation();
// void transform(T val); }
// [17:31] <@Xoppa>
// https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/g3d/utils/BaseAnimationController.java#L40
// [17:34] * ThreadL0ck (~ThreadL0c@197.220.114.182) Quit (Remote host closed the connection)
// [17:35] <davebaol> thanks Xoppa, sounds interesting

			// TODO Valutare se portare mul(orientationMatrix) in Vector
			// Transform it by the anchor point's position and orientation
// relativeLocPosition.mul(orientationMatrix).add(anchor.position);
			if (relativeLocPosition instanceof Vector2)
				((Vector2)relativeLocPosition).mul(orientationMatrix);
			else if (relativeLocPosition instanceof Vector3) ((Vector3)relativeLocPosition).mul(orientationMatrix);

			// Add the anchor and drift components
			relativeLocPosition.add(anchorLessDriftPosition);
			relativeLoc.setOrientation(relativeLoc.getOrientation() + anchorLessDriftOrientation);
		}
	}
}
