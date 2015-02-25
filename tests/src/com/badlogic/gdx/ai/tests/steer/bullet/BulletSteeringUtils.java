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

package com.badlogic.gdx.ai.tests.steer.bullet;

import com.badlogic.gdx.math.Vector3;

public final class BulletSteeringUtils {

	private BulletSteeringUtils () {
	}

	public static float vectorToAngle (Vector3 vector) {
// return (float)Math.atan2(vector.z, vector.x);
		return (float)Math.atan2(-vector.z, vector.x);
	}

	public static Vector3 angleToVector (Vector3 outVector, float angle) {
// outVector.set(MathUtils.cos(angle), 0f, MathUtils.sin(angle));
		outVector.z = -(float)Math.sin(angle);
		outVector.y = 0;
		outVector.x = (float)Math.cos(angle);
		return outVector;
	}

}
