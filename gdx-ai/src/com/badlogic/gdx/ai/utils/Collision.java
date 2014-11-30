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

/** A {@code Collision} is made up of a collision point and the normal at that point of collision.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class Collision<T extends Vector<T>> {

	/** The collision point. */
	public T point;

	/** The normal of this collision. */
	public T normal;

	/** Creates a {@code Collision} with the given {@code point} and {@code normal}.
	 * @param point the point where this collision occurred
	 * @param normal the normal of this collision */
	public Collision (T point, T normal) {
		this.point = point;
		this.normal = normal;
	}

	/** Sets this collision from the given collision.
	 * @param collision The collision
	 * @return this collision for chaining. */
	public Collision<T> set (Collision<T> collision) {
		this.point.set(collision.point);
		this.normal.set(collision.normal);
		return this;
	}

	/** Sets this collision from the given point and normal.
	 * @param point the collision point
	 * @param normal the normal of this collision
	 * @return this collision for chaining. */
	public Collision<T> set (T point, T normal) {
		this.point.set(point);
		this.normal.set(normal);
		return this;
	}
}
