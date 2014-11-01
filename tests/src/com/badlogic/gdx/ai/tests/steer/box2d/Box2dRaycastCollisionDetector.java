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

package com.badlogic.gdx.ai.tests.steer.box2d;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

/** A raycast collision detector for box2d.
 * 
 * @author davebaol */
public class Box2dRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {

	World world;
	Box2dRaycastCallback callback;

	public Box2dRaycastCollisionDetector (World world) {
		this(world, new Box2dRaycastCallback());
	}

	public Box2dRaycastCollisionDetector (World world, Box2dRaycastCallback callback) {
		this.world = world;
		this.callback = callback;
	}

	@Override
	public boolean collides (Ray<Vector2> ray) {
		return findCollision(null, ray);
	}

	@Override
	public boolean findCollision (Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
		callback.collided = false;
		if (!inputRay.start.epsilonEquals(inputRay.end, MathUtils.FLOAT_ROUNDING_ERROR)) {
			callback.outputCollision = outputCollision;
			world.rayCast(callback, inputRay.start, inputRay.end);
		}
		return callback.collided;
	}

	public static class Box2dRaycastCallback implements RayCastCallback {
		public Collision<Vector2> outputCollision;
		public boolean collided;

		public Box2dRaycastCallback () {
		}

		@Override
		public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			if (outputCollision != null) outputCollision.set(point, normal);
			collided = true;
			return fraction;
		}
	}
}
