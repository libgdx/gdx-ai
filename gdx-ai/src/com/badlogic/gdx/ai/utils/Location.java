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

package com.badlogic.gdx.ai.utils;

import com.badlogic.gdx.math.Vector;

/** The {@code Location} interface represents any game object having a position and an orientation.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public interface Location<T extends Vector<T>> {

	/** Returns the vector indicating the position of this location. */
	public T getPosition ();

	/** Returns the float value indicating the orientation of this location. The orientation is the angle in radians representing
	 * the direction that this location is facing. */
	public float getOrientation ();

	/** Sets the orientation of this location, i.e. the angle in radians representing the direction that this location is facing.
	 * @param orientation the orientation in radians */
	public void setOrientation (float orientation);

	/** Returns the angle in radians pointing along the specified vector.
	 * @param vector the vector */
	public float vectorToAngle (T vector);

	/** Returns the unit vector in the direction of the specified angle expressed in radians.
	 * @param outVector the output vector.
	 * @param angle the angle in radians.
	 * @return the output vector for chaining. */
	public T angleToVector (T outVector, float angle);

	/** Creates a new location.
	 * <p>
	 * This method is used internally to instantiate locations of the correct type parameter {@code T}. This technique keeps the API
	 * simple and makes the API easier to use with the GWT backend because avoids the use of reflection.
	 * @return the newly created location. */
	public Location<T> newLocation ();
}
