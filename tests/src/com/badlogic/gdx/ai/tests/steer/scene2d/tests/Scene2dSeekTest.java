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

package com.badlogic.gdx.ai.tests.steer.scene2d.tests;

import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.Scene2dSteeringTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.SteeringActor;
import com.badlogic.gdx.ai.tests.steer.scene2d.Scene2dTargetInputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

/** A class to test and experiment with the {@link Seek} behavior.
 * 
 * @autor davebaol */
public class Scene2dSeekTest extends Scene2dSteeringTest {

	SteeringActor character;
	SteeringActor target;

	public Scene2dSeekTest (SteeringBehaviorsTest container) {
		super(container, "Seek");
	}

	@Override
	public void create (Table table) {
		character = new SteeringActor(container.badlogicSmall, false);
		target = new SteeringActor(container.target);
		inputProcessor = new Scene2dTargetInputProcessor(target);

		character.setMaxLinearSpeed(250);
		character.setMaxLinearAcceleration(2000);

		final Seek<Vector2> seekSB = new Seek<Vector2>(character, target);
		character.setSteeringBehavior(seekSB);

		table.addActor(character);
		table.addActor(target);

		character.setPosition(container.stageWidth / 2, container.stageHeight / 2, Align.center);
		target.setPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight), Align.center);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 10000, 20);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		addMaxLinearSpeedController(detailTable, character);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
	}

	@Override
	public void dispose () {
	}

}
