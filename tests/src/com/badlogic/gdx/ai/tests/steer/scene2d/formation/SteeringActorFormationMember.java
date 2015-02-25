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

package com.badlogic.gdx.ai.tests.steer.scene2d.formation;

import com.badlogic.gdx.ai.fma.FormationMember;
import com.badlogic.gdx.ai.tests.steer.scene2d.Scene2dLocation;
import com.badlogic.gdx.ai.tests.steer.scene2d.SteeringActor;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/** A {@code SteeringActorFormationMember} is a scene2d {@link SteeringActor} implementing the {@link FormationMember} interface.
 * 
 * @autor davebaol */
public class SteeringActorFormationMember extends SteeringActor implements FormationMember<Vector2> {

	public Scene2dLocation target;

	public SteeringActorFormationMember (TextureRegion region) {
		this(region, false);
	}

	public SteeringActorFormationMember (TextureRegion region, boolean independentFacing) {
		super(region, independentFacing);
		this.target = new Scene2dLocation();
	}

	@Override
	public Location<Vector2> getTargetLocation () {
		return target;
	}
}
