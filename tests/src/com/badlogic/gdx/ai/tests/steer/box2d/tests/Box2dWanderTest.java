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
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringEntity;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringTest;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** A class to test and experiment with the {@link Wander} behavior.
 * 
 * @autor davebaol */
public class Box2dWanderTest extends Box2dSteeringTest {
	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	Box2dSteeringEntity character;
	Wander<Vector2> wanderSB;

	private Batch spriteBatch;

	public Box2dWanderTest (SteeringBehaviorsTest container) {
		super(container, "Wander");
	}

	@Override
	public void create () {
		super.create();

		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		spriteBatch = new SpriteBatch();

		// Create character
		character = createSteeringEntity(world, container.greenFish, true);
		character.setMaxLinearAcceleration(10);
		character.setMaxLinearSpeed(3);
		character.setMaxAngularAcceleration(.5f); // greater than 0 because independent facing is enabled
		character.setMaxAngularSpeed(5);

		this.wanderSB = new Wander<Vector2>(character) //
			.setFaceEnabled(true) // We want to use Face internally (independent facing is on)
			.setAlignTolerance(0.001f) // Used by Face
			.setDecelerationRadius(1) // Used by Face
			.setTimeToTarget(0.1f) // Used by Face
			.setWanderOffset(3) //
			.setWanderOrientation(3) //
			.setWanderRadius(1) //
			.setWanderRate(MathUtils.PI2 * 4);
		character.setSteeringBehavior(wanderSB);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 100, 1);

		detailTable.row();
		addMaxAngularAccelerationController(detailTable, character, 0, 10, .01f);

		detailTable.row();
		addMaxAngularSpeedController(detailTable, character, 0, 20, .5f);

		detailTable.row();
		final Label labelWanderOffset = new Label("Wander Offset [" + wanderSB.getWanderOffset() + "]", container.skin);
		detailTable.add(labelWanderOffset);
		detailTable.row();
		Slider wanderOffset = new Slider(0, 10, .2f, false, container.skin);
		wanderOffset.setValue(wanderSB.getWanderOffset());
		wanderOffset.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				wanderSB.setWanderOffset(slider.getValue());
				labelWanderOffset.setText("Wander Offset [" + slider.getValue() + "]");
			}
		});
		detailTable.add(wanderOffset);

		detailTable.row();
		final Label labelWanderRadius = new Label("Wander Radius [" + wanderSB.getWanderRadius() + "]", container.skin);
		detailTable.add(labelWanderRadius);
		detailTable.row();
		Slider wanderRadius = new Slider(0, 5, .2f, false, container.skin);
		wanderRadius.setValue(wanderSB.getWanderRadius());
		wanderRadius.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				wanderSB.setWanderRadius(slider.getValue());
				labelWanderRadius.setText("Wander Radius [" + slider.getValue() + "]");
			}
		});
		detailTable.add(wanderRadius);

		detailTable.row();
		final Label labelWanderRate = new Label("Wander Rate [" + wanderSB.getWanderRate() + "]", container.skin);
		detailTable.add(labelWanderRate);
		detailTable.row();
		Slider wanderRate = new Slider(0, MathUtils.PI2 * 10, MathUtils.degreesToRadians, false, container.skin);
		wanderRate.setValue(wanderSB.getWanderRate());
		wanderRate.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				wanderSB.setWanderRate(slider.getValue());
				labelWanderRate.setText("Wander Rate [" + slider.getValue() + "]");
			}
		});
		detailTable.add(wanderRate);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw circle", container.skin);
		debug.setChecked(drawDebug);
		debug.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				drawDebug = checkBox.isChecked();
			}
		});
		detailTable.add(debug);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		addMaxLinearSpeedController(detailTable, character);

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
		// Draw the character
		spriteBatch.begin();
		character.draw(spriteBatch);
		spriteBatch.end();

		if (drawDebug) {
			// Draw circle
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(0, 1, 0, 1);
			int wanderCenterX = Box2dSteeringTest.metersToPixels(wanderSB.getWanderCenter().x);
			int wanderCenterY = Box2dSteeringTest.metersToPixels(wanderSB.getWanderCenter().y);
			int wanderRadius = Box2dSteeringTest.metersToPixels(wanderSB.getWanderRadius());
			shapeRenderer.circle(wanderCenterX, wanderCenterY, wanderRadius);
			shapeRenderer.end();

			// Draw target
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(1, 0, 0, 1);
			int targetCenterX = Box2dSteeringTest.metersToPixels(wanderSB.getInternalTargetPosition().x);
			int targetCenterY = Box2dSteeringTest.metersToPixels(wanderSB.getInternalTargetPosition().y);
			shapeRenderer.circle(targetCenterX, targetCenterY, 4);
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		shapeRenderer.dispose();
		spriteBatch.dispose();
	}

}
