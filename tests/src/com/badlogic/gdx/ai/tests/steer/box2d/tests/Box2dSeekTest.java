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

package com.badlogic.gdx.ai.tests.steer.box2d.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringEntity;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dTargetInputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** A class to test and experiment with the {@link Seek} behavior.
 * 
 * @autor davebaol */
public class Box2dSeekTest extends Box2dSteeringTest {

	Box2dSteeringEntity character;
	Box2dSteeringEntity target;

	private Batch spriteBatch;

	public Box2dSeekTest (SteeringBehaviorsTest container) {
		super(container, "Seek");
	}

	@Override
	public void create () {
		super.create();

		spriteBatch = new SpriteBatch();

		// Create character
		character = createSteeringEntity(world, container.greenFish);
		character.setMaxLinearSpeed(5);
		character.setMaxLinearAcceleration(10);

		// Create target
		target = createSteeringEntity(world, container.target);
		markAsSensor(target);
		inputProcessor = new Box2dTargetInputProcessor(target);

		// Create character's steering behavior
		final Seek<Vector2> seekSB = new Seek<Vector2>(character, target);
		character.setSteeringBehavior(seekSB);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 200, 1);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		addMaxLinearSpeedController(detailTable, character, 0, 30, .5f);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void update () {
		super.update();

		// Update the character
		character.update(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void draw () {
		// Draw the character and the target
		spriteBatch.begin();
		character.draw(spriteBatch);
		target.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void dispose () {
		super.dispose();
		spriteBatch.dispose();
	}

}
