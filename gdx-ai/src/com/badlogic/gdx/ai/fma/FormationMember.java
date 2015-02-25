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

/** Game characters coordinated by a {@link Formation} must implement this interface. Any {@code FormationMember} has a target
 * location which is the place where it should be in order to stay in formation. This target location is calculated by the
 * formation itself.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public interface FormationMember<T extends Vector<T>> {

	/** Returns the target location of this formation member. */
	public Location<T> getTargetLocation ();
}
