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

/** A {@code SlotAssignment} instance represents the assignment of a single {@link FormationMember} to its slot in the
 * {@link Formation}.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class SlotAssignment<T extends Vector<T>> {
	public FormationMember<T> member;
	public int slotNumber;

	/** Creates a {@code SlotAssignment} for the given {@code member}.
	 * @param member the member of this slot assignment */
	public SlotAssignment (FormationMember<T> member) {
		this(member, 0);
	}

	/** Creates a {@code SlotAssignment} for the given {@code member} and {@code slotNumber}.
	 * @param member the member of this slot assignment */
	public SlotAssignment (FormationMember<T> member, int slotNumber) {
		this.member = member;
		this.slotNumber = slotNumber;
	}
}
