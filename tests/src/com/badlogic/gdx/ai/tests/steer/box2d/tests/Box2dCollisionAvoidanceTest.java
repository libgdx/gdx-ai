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
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.CollisionAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dRadiusProximity;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringEntity;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringTest;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

/** A class to test and experiment with the {@link CollisionAvoidance} behavior.
 * 
 * @autor davebaol */
public class Box2dCollisionAvoidanceTest extends Box2dSteeringTest {
	Array<Box2dSteeringEntity> characters;
	Box2dRadiusProximity char0Proximity;
	Array<Box2dRadiusProximity> proximities;
	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	private World world;
	private Batch spriteBatch;

	public Box2dCollisionAvoidanceTest (SteeringBehaviorsTest container) {
		super(container, "Collision Avoidance");
	}

	@Override
	public void create (Table table) {
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		spriteBatch = new SpriteBatch();

		// Instantiate a new World with no gravity
		world = createWorld();

		characters = new Array<Box2dSteeringEntity>();
		proximities = new Array<Box2dRadiusProximity>();

		for (int i = 0; i < 60; i++) {
			final Box2dSteeringEntity character = createSteeringEntity(world, container.greenFish, false);
			character.setMaxLinearSpeed(1.5f);
			character.setMaxLinearAcceleration(40);

			Box2dRadiusProximity proximity = new Box2dRadiusProximity(character, world,
				character.getBoundingRadius() * 4);
			proximities.add(proximity);
			if (i == 0) char0Proximity = proximity;
			CollisionAvoidance<Vector2> collisionAvoidanceSB = new CollisionAvoidance<Vector2>(character, proximity);

			Wander<Vector2> wanderSB = new Wander<Vector2>(character) //
				// Don't use Face internally because independent facing is off
				.setFaceEnabled(false) //
				// We don't need a limiter supporting angular components because Face is not used
				// No need to call setAlignTolerance, setDecelerationRadius and setTimeToTarget for the same reason
				.setLimiter(new LinearAccelerationLimiter(30)) //
				.setWanderOffset(60) //
				.setWanderOrientation(10) //
				.setWanderRadius(40) //
				.setWanderRate(MathUtils.PI / 5);

			PrioritySteering<Vector2> prioritySteeringSB = new PrioritySteering<Vector2>(character, 0.0001f);
			prioritySteeringSB.add(collisionAvoidanceSB);
			prioritySteeringSB.add(wanderSB);

			character.setSteeringBehavior(prioritySteeringSB);

			setRandomNonOverlappingPosition(character, characters, Box2dSteeringTest.pixelsToMeters(5));

			characters.add(character);
		}

		inputProcessor = null;

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelMaxLinAcc = new Label("Max.Linear Acc.[" + characters.get(0).getMaxLinearAcceleration() + "]",
			container.skin);
		detailTable.add(labelMaxLinAcc);
		detailTable.row();
		Slider maxLinAcc = new Slider(0, 300, 1, false, container.skin);
		maxLinAcc.setValue(characters.get(0).getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < characters.size; i++)
					characters.get(i).setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.Linear Acc.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinAcc);

		detailTable.row();
		final Label labelProximityRadius = new Label("Proximity Radius [" + proximities.get(0).getDetectionRadius() + "]", container.skin);
		detailTable.add(labelProximityRadius);
		detailTable.row();
		Slider proximityRadius = new Slider(0, 10, .1f, false, container.skin);
		proximityRadius.setValue(proximities.get(0).getDetectionRadius());
		proximityRadius.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < proximities.size; i++)
					proximities.get(i).setDetectionRadius(slider.getValue());
				labelProximityRadius.setText("Proximity Radius [" + slider.getValue() + "]");
			}
		});
		detailTable.add(proximityRadius);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		final Label labelMaxLinSpeed = new Label("Max.Linear Speed.[" + characters.get(0).getMaxLinearSpeed() + "]", container.skin);
		detailTable.add(labelMaxLinSpeed);
		detailTable.row();
		Slider maxLinSpeed = new Slider(0, 20, .5f, false, container.skin);
		maxLinSpeed.setValue(characters.get(0).getMaxLinearSpeed());
		maxLinSpeed.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				for (int i = 0; i < characters.size; i++)
					characters.get(i).setMaxLinearSpeed(slider.getValue());
				labelMaxLinSpeed.setText("Max.Linear Speed.[" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxLinSpeed);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw Proximity", container.skin);
		debug.setChecked(drawDebug);
		debug.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				drawDebug = checkBox.isChecked();
			}
		});
		detailTable.add(debug);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		float deltaTime = Gdx.graphics.getDeltaTime();

		world.step(deltaTime, 8, 3);

		// Update and draw the character
		spriteBatch.begin();
		for (int i = 0; i < characters.size; i++) {
			Box2dSteeringEntity character = characters.get(i);
			character.update(deltaTime);
			character.draw(spriteBatch);
		}
		spriteBatch.end();

		if (drawDebug) {
			Steerable<Vector2> steerable = characters.get(0);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(0, 1, 0, 1);
			int centerX = Box2dSteeringTest.metersToPixels(steerable.getPosition().x);
			int centerY = Box2dSteeringTest.metersToPixels(steerable.getPosition().y);
			int radius = Box2dSteeringTest.metersToPixels(char0Proximity.getDetectionRadius());
			shapeRenderer.circle(centerX, centerY, radius);
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
		world.dispose();
		spriteBatch.dispose();
	}

}
