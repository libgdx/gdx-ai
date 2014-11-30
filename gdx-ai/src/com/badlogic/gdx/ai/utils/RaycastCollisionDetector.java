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

/** A {@code RaycastCollisionDetector} finds the closest intersection between a ray and any object in the game world.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public interface RaycastCollisionDetector<T extends Vector<T>> {

	/** Casts the given ray to test if it collides with any objects in the game world.
	 * @param ray the ray to cast.
	 * @return {@code true} in case of collision; {@code false} otherwise. */
	public boolean collides (Ray<T> ray);

	/** Find the closest collision between the given input ray and the objects in the game world. In case of collision,
	 * {@code outputCollision} will contain the collision point and the normal vector of the obstacle at the point of collision.
	 * @param outputCollision the output collision.
	 * @param inputRay the ray to cast.
	 * @return {@code true} in case of collision; {@code false} otherwise. */
	public boolean findCollision (Collision<T> outputCollision, Ray<T> inputRay);
}
