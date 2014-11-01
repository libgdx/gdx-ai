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

package com.badlogic.gdx.ai.tests.steer.bullet.tests;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestNotMeRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;

/** A 3D {@link RaycastCollisionDetector} to be used with bullet physics. It reports the closest collision which is not the
 * supplied "me" collision object.
 * @author Daniel Holderbaum
 * @author davebaol */
public class BulletRaycastCollisionDetector implements RaycastCollisionDetector<Vector3> {

	btCollisionWorld world;

	ClosestRayResultCallback callback;

	public BulletRaycastCollisionDetector (btCollisionWorld world, btCollisionObject me) {
		this.world = world;
		this.callback = new ClosestNotMeRayResultCallback(me);
	}

	@Override
	public boolean collides (Ray<Vector3> ray) {
		return findCollision(null, ray);
	}

	@Override
	public boolean findCollision (Collision<Vector3> outputCollision, Ray<Vector3> inputRay) {
		// reset because we reuse the callback
		callback.setCollisionObject(null);

		world.rayTest(inputRay.start, inputRay.end, callback);

		if (outputCollision != null) {
			callback.getHitPointWorld(outputCollision.point);
			callback.getHitNormalWorld(outputCollision.normal);
		}

		return callback.hasHit();
	}

}
