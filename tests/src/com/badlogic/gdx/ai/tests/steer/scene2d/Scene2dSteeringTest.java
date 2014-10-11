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

package com.badlogic.gdx.ai.tests.steer.scene2d;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.SteeringTestBase;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Base class for scene2d steering behavior tests.
 * 
 * @author davebaol */
public abstract class Scene2dSteeringTest extends SteeringTestBase {

	public Scene2dSteeringTest (SteeringBehaviorsTest container, String name) {
		super(container, "Scene2d", name, null);
	}

	public Scene2dSteeringTest (SteeringBehaviorsTest container, String name, InputProcessor inputProcessor) {
		super(container, "Scene2d", name, inputProcessor);
	}

	protected void addMaxLinearAccelerationController (Table table, Limiter limiter) {
		addMaxLinearAccelerationController(table, limiter, 0, 500, 10);
	}

	protected void addMaxLinearSpeedController (Table table, Limiter limiter) {
		addMaxLinearSpeedController(table, limiter, 0, 500, 10);
	}

	protected void addMaxAngularAccelerationController (Table table, Limiter limiter) {
		addMaxAngularAccelerationController(table, limiter, 0, 50, 1);
	}

	protected void addMaxAngularSpeedController (Table table, Limiter limiter) {
		addMaxAngularSpeedController(table, limiter, 0, 20, 1);
	}

	protected void addAlignOrientationToLinearVelocityController (Table table, final SteeringActor character) {
		CheckBox alignOrient = new CheckBox("Align orient.to velocity", container.skin);
		alignOrient.setChecked(character.isIndependentFacing());
		alignOrient.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				character.setIndependentFacing(checkBox.isChecked());
			}
		});
		table.add(alignOrient);
	}

	protected void setRandomNonOverlappingPosition (SteeringActor character, Array<SteeringActor> others,
		float minDistanceFromBoundary) {
		int maxTries = Math.max(100, others.size * others.size); 
		SET_NEW_POS:
		while (--maxTries >= 0) {
			character.setPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight), Align.center);
			character.getPosition().set(character.getX(Align.center), character.getY(Align.center));
			for (int i = 0; i < others.size; i++) {
				SteeringActor other = (SteeringActor)others.get(i);
				if (character.getPosition().dst(other.getPosition()) <= character.getBoundingRadius() + other.getBoundingRadius()
					+ minDistanceFromBoundary) continue SET_NEW_POS;
			}
			return;
		}
		throw new GdxRuntimeException("Probable infinite loop detected");
	}
}
