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

package com.badlogic.gdx.ai.tests.steer.box2d.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.limiters.NullLimiter;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringEntity;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dTargetInputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** A class to test and experiment with the {@link LookWhereYouAreGoing} behavior.
 * 
 * @autor davebaol */
public class Box2dLookWhereYouAreGoingTest extends Box2dSteeringTest {

	Box2dSteeringEntity character;
	Box2dSteeringEntity target;

	private World world;
	private Batch spriteBatch;

	public Box2dLookWhereYouAreGoingTest (SteeringBehaviorsTest container) {
		super(container, "Look Where You're Going");
	}

	@Override
	public void create (Table table) {
		spriteBatch = new SpriteBatch();

		// Instantiate a new World with no gravity
		world = createWorld();

		// Create character
		character = createSteeringEntity(world, container.greenFish, true);
		character.setMaxLinearSpeed(5);
		character.setMaxLinearAcceleration(500);
		character.setMaxAngularAcceleration(40);
		character.setMaxAngularSpeed(15);

		// Create target
		target = createSteeringEntity(world, container.target);
		markAsSensor(target);
		inputProcessor = new Box2dTargetInputProcessor(target);

		final LookWhereYouAreGoing<Vector2> lookWhereYouAreGoingSB = new LookWhereYouAreGoing<Vector2>(character) //
			.setTimeToTarget(0.1f) //
			.setAlignTolerance(0.001f) //
			.setDecelerationRadius(MathUtils.PI);

		final Arrive<Vector2> arriveSB = new Arrive<Vector2>(character, target) //
			.setTimeToTarget(0.01f) //
			.setArrivalTolerance(0.0002f) //
			.setDecelerationRadius(3);

		BlendedSteering<Vector2> blendedSteering = new BlendedSteering<Vector2>(character) //
			.setLimiter(NullLimiter.NEUTRAL_LIMITER) //
			.add(arriveSB, 1f) //
			.add(lookWhereYouAreGoingSB, 1f);
		character.setSteeringBehavior(blendedSteering);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxAngularAccelerationController(detailTable, character, 0, 50, 1);

		detailTable.row();
		addMaxAngularSpeedController(detailTable, character, 0, 20, 1);

		detailTable.row();
		final Label labelDecelerationRadius = new Label("Deceleration Radius [" + lookWhereYouAreGoingSB.getDecelerationRadius()
			+ "]", container.skin);
		detailTable.add(labelDecelerationRadius);
		detailTable.row();
		Slider decelerationRadius = new Slider(0, MathUtils.PI2, MathUtils.degreesToRadians, false, container.skin);
		decelerationRadius.setValue(lookWhereYouAreGoingSB.getDecelerationRadius());
		decelerationRadius.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				lookWhereYouAreGoingSB.setDecelerationRadius(slider.getValue());
				labelDecelerationRadius.setText("Deceleration Radius [" + slider.getValue() + "]");
			}
		});
		detailTable.add(decelerationRadius);

		detailTable.row();
		final Label labelAlignTolerance = new Label("Align tolerance [" + lookWhereYouAreGoingSB.getAlignTolerance() + "]",
			container.skin);
		detailTable.add(labelAlignTolerance);
		detailTable.row();
		Slider alignTolerance = new Slider(0, 1, 0.0001f, false, container.skin);
		alignTolerance.setValue(lookWhereYouAreGoingSB.getAlignTolerance());
		alignTolerance.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				lookWhereYouAreGoingSB.setAlignTolerance(slider.getValue());
				labelAlignTolerance.setText("Align tolerance [" + slider.getValue() + "]");
			}
		});
		detailTable.add(alignTolerance);

		detailTable.row();
		final Label labelTimeToTarget = new Label("Time to Target [" + lookWhereYouAreGoingSB.getTimeToTarget() + " sec.]",
			container.skin);
		detailTable.add(labelTimeToTarget);
		detailTable.row();
		Slider timeToTarget = new Slider(0, 3, 0.1f, false, container.skin);
		timeToTarget.setValue(lookWhereYouAreGoingSB.getTimeToTarget());
		timeToTarget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				lookWhereYouAreGoingSB.setTimeToTarget(slider.getValue());
				labelTimeToTarget.setText("Time to Target [" + slider.getValue() + " sec.]");
			}
		});
		detailTable.add(timeToTarget);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		float deltaTime = Gdx.graphics.getDeltaTime();

		world.step(deltaTime, 8, 3);

		// Update and draw the character
		character.update(deltaTime);
		spriteBatch.begin();
		character.draw(spriteBatch);
		target.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void dispose () {
		world.dispose();
		spriteBatch.dispose();
	}

}
