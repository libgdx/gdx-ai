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

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.tests.SteeringBehaviorTest;
import com.badlogic.gdx.ai.tests.steer.SteeringTest;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** Base class for box2d steering behavior tests.
 * 
 * @author davebaol */
public abstract class Box2dSteeringTest extends SteeringTest {

	public Box2dSteeringTest (SteeringBehaviorTest container, String name) {
		this(container, name, null);
	}

	public Box2dSteeringTest (SteeringBehaviorTest container, String name, InputProcessor inputProcessor) {
		super(container, "Box2d", name, inputProcessor);
	}

	/** Instantiate a new World with no gravity and tell it to sleep when possible. */
	public World createWorld () {
		return createWorld(0);
	}

	/** Instantiate a new World with the given gravity and tell it to sleep when possible. */
	public World createWorld (float y) {
		return new World(new Vector2(0, y), true);
	}

	protected void addMaxLinearAccelerationController (Table table, Limiter limiter) {
		addMaxLinearAccelerationController(table, limiter, 0, 500, 10);
	}

	protected void addMaxSpeedController (Table table, Limiter limiter) {
		addMaxSpeedController(table, limiter, 0, 500, 10);
	}

	protected void addMaxAngularAccelerationController (Table table, Limiter limiter) {
		addMaxAngularAccelerationController(table, limiter, 0, 50, 1);
	}

	protected void addMaxAngularSpeedController (Table table, Limiter limiter) {
		addMaxAngularSpeedController(table, limiter, 0, 20, 1);
	}

//	protected void addAlignOrientationToLinearVelocityController (Table table, final SteeringActor character) {
//		CheckBox alignOrient = new CheckBox("Align orient.to velocity", container.skin);
//		alignOrient.setChecked(character.isIndependentFacing());
//		alignOrient.addListener(new ClickListener() {
//			@Override
//			public void clicked (InputEvent event, float x, float y) {
//				CheckBox checkBox = (CheckBox)event.getListenerActor();
//				character.setIndependentFacing(checkBox.isChecked());
//			}
//		});
//		table.add(alignOrient);
//	}
//
//	protected void setRandomNonOverlappingPosition (SteeringActor character, Array<SteeringActor> others,
//		float minDistanceFromBoundary) {
//		SET_NEW_POS:
//		while (true) {
//			character.setCenterPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight));
//			character.getPosition().set(character.getCenterX(), character.getCenterY());
//			for (int i = 0; i < others.size; i++) {
//				SteeringActor other = (SteeringActor)others.get(i);
//				if (character.getPosition().dst(other.getPosition()) <= character.getBoundingRadius() + other.getBoundingRadius()
//					+ minDistanceFromBoundary) continue SET_NEW_POS;
//			}
//			return;
//		}
//	}
}
