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

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

/** A {@code Box2dRadiusProximity} is a {@link Proximity} that queries the world for all fixtures that potentially overlap the
 * circle having the specified detection radius and whose center is the owner position.
 * 
 * @author davebaol */
public class Box2dRadiusProximity extends Box2dSquareAABBProximity {

	public Box2dRadiusProximity (Steerable<Vector2> owner, World world, float detectionRadius) {
		super(owner, world, detectionRadius);
	}

	@SuppressWarnings("unchecked")
	protected Steerable<Vector2> getSteerable (Fixture fixture) {
		return (Steerable<Vector2>)fixture.getBody().getUserData();
	}

	@Override
	protected boolean accept (Steerable<Vector2> steerable) {
		// The bounding radius of the current body is taken into account
		// by adding it to the radius proximity
		float range = detectionRadius + steerable.getBoundingRadius();

		// Make sure the current body is within the range.
		// Notice we're working in distance-squared space to avoid square root.
		float distanceSquare = steerable.getPosition().dst2(owner.getPosition());

		return distanceSquare <= range * range;
	}

}
