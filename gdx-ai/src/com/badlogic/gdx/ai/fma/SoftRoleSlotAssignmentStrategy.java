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
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** {@code SoftRoleSlotAssignmentStrategy} is a concrete implementation of {@link BoundedSlotAssignmentStrategy} that supports soft
 * roles, i.e. roles that can be broken. Rather than a member having a list of roles it can fulfill, it has a set of values
 * representing how difficult it would find it to fulfill every role. The value is known as the slot cost. To make a slot
 * impossible for a member to fill, its slot cost should be infinite. Normally, this is just a very large value. The algorithm
 * below works better if the values aren't near to the upper limit {@link Float#MAX_VALUE} because several costs will be added. To
 * make a slot ideal for a member, its slot cost should be zero. We can have different levels of unsuitable assignment for one
 * member.
 * <p>
 * Slot costs do not necessarily have to depend only on the member and the slot roles. They can be generalized to include any
 * difficulty a member might have in taking up a slot. If a formation is spread out, for example, a member may choose a slot that
 * is close by over a more distant slot. Distance can be directly used as a slot cost.
 * <p>
 * <b>IMPORTANT NOTES:</b>
 * <ul>
 * <li>In order for the algorithm to work properly the slot costs can not be negative.</li>
 * <li>This algorithm is often not fast enough to be used regularly. However, slot assignment happens relatively seldom (when the
 * player selects a new pattern, for example, or adds a member to the formation, or a member is removed from the formation).</li>
 * </ul>
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class SoftRoleSlotAssignmentStrategy<T extends Vector<T>> extends BoundedSlotAssignmentStrategy<T> {

	protected SlotCostProvider<T> slotCostProvider;

	private BooleanArray filledSlots;

	public SoftRoleSlotAssignmentStrategy (SlotCostProvider<T> slotCostProvider) {
		this.slotCostProvider = slotCostProvider;

		this.filledSlots = new BooleanArray();
	}

	@Override
	public void updateSlotAssignments (Array<SlotAssignment<T>> assignments) {

		// Calculate slot costs
		slotCostProvider.calculateCosts();

		// Holds a list of member and slot data for each member.
		Array<MemberAndSlots<T>> memberData = new Array<MemberAndSlots<T>>();

		// Compile the member data
		int numberOfAssignments = assignments.size;
		System.out.println("numberOfAsignments =" + numberOfAssignments);
		for (int i = 0; i < numberOfAssignments; i++) {
			SlotAssignment<T> assignment = assignments.get(i);

			// Create a new member datum, and fill it
			MemberAndSlots<T> datum = new MemberAndSlots<T>(assignment.member);

			// Add each valid slot to it
			for (int j = 0; j < numberOfAssignments; j++) {

				// Get the cost of the slot
				float cost = slotCostProvider.getCost(assignment.member, j);

				// Make sure the slot is valid
				if (cost >= Float.MAX_VALUE) continue;
				System.out.println("step1.j=" + j);

				SlotAssignment<T> slot = assignments.get(j);

				// Store the slot information
				CostAndSlot<T> slotDatum = new CostAndSlot<T>(cost, slot.slotNumber);
				datum.costAndSlots.add(slotDatum);

				// Add it to the member's ease of assignment
				datum.assignmentEase += 1f / (1f + cost);
			}

			// Add member datum
			memberData.add(datum);
			System.out.println("step1.i=" + i);
		}

		// Reset the array to keep track of which slots we have already filled.
		if (numberOfAssignments > filledSlots.size) filledSlots.ensureCapacity(numberOfAssignments - filledSlots.size);
		filledSlots.size = numberOfAssignments;
		for (int i = 0; i < numberOfAssignments; i++)
			filledSlots.set(i, false);

		// Arrange members in order of ease of assignment, with the least easy first.
		memberData.sort();
		System.out.println("---------------------");
		MEMBER_LOOP:
		for (int i = 0; i < memberData.size; i++) {
			System.out.println("step2.i=" + i);
			MemberAndSlots<T> memberDatum = memberData.get(i);

			// Choose the first slot in the list that is still empty (non-filled)
			memberDatum.costAndSlots.sort();
			int m = memberDatum.costAndSlots.size;
			for (int j = 0; j < m; j++) {
				int slotNumber = memberDatum.costAndSlots.get(j).slotNumber;

				System.out.println("step2.j=" + j + " filled[" + slotNumber + "]=" + filledSlots.get(slotNumber));

				// Check if this slot is valid
				if (!filledSlots.get(slotNumber)) {
					// Fill this slot
					SlotAssignment<T> slot = assignments.get(slotNumber);
					slot.member = memberDatum.member;
					slot.slotNumber = slotNumber;

					// Reserve the slot
					filledSlots.set(slotNumber, true);

					// Go to the next member
					continue MEMBER_LOOP;
				}
			}

			// If we reach here, it's because a member has no valid assignment.
			//
			// TODO
			// Some sensible action should be taken, such as reporting to the player.
			throw new GdxRuntimeException("SoftRoleSlotAssignmentStrategy cannot find valid slot assignment for member "
				+ memberDatum.member);
		}

	}

	public static class CostAndSlot<T extends Vector<T>> implements Comparable<CostAndSlot<T>> {
		float cost;
		int slotNumber;

		public CostAndSlot (float cost, int slotNumber) {
			this.cost = cost;
			this.slotNumber = slotNumber;
		}

		@Override
		public int compareTo (CostAndSlot<T> other) {
			return cost < other.cost ? -1 : (cost > other.cost ? 1 : 0);
		}
	}

	public static class MemberAndSlots<T extends Vector<T>> implements Comparable<MemberAndSlots<T>> {
		FormationMember<T> member;
		float assignmentEase;
		Array<CostAndSlot<T>> costAndSlots;

		public MemberAndSlots (FormationMember<T> member) {
			this.member = member;
			this.assignmentEase = 0f;
			this.costAndSlots = new Array<CostAndSlot<T>>();
		}

		@Override
		public int compareTo (MemberAndSlots<T> other) {
			return assignmentEase < other.assignmentEase ? -1 : (assignmentEase > other.assignmentEase ? 1 : 0);
		}
	}

	public interface SlotCostProvider<T extends Vector<T>> {

		public void calculateCosts ();

		public float getCost (FormationMember<T> member, int slotNumber);

	}
}
