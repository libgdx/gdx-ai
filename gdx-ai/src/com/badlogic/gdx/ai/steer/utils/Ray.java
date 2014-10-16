/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.ai.steer.utils;

import com.badlogic.gdx.math.Vector;

/** A {@code Ray} is made up of a starting position and a vector whose direction and length define the end point of the ray.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class Ray<T extends Vector<T>> {

	/** The starting point of this ray. */
	public T origin;

	/** The direction of this ray. */
	public T direction;

	/** Creates a {@code Ray} with the given {@code origin} and {@code direction}.
	 * @param origin the starting point of this ray
	 * @param direction the direction of this ray */
	public Ray (T origin, T direction) {
		this.origin = origin;
		this.direction = direction;
	}

	/** Sets this ray from the given ray.
	 * @param ray The ray
	 * @return this ray for chaining. */
	public Ray<T> set (Ray<T> ray) {
		origin.set(ray.origin);
		direction.set(ray.direction);
		return this;
	}

	/** Sets this Ray from the given origin and direction.
	 * @param origin the origin
	 * @param direction the direction
	 * @return this ray for chaining. */
	public Ray<T> set (T origin, T direction) {
		this.origin.set(origin);
		this.direction.set(direction);
		return this;
	}
}
