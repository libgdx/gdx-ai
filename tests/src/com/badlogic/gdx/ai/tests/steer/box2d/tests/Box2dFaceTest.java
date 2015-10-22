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
import com.badlogic.gdx.ai.steer.behaviors.Face;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringEntity;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dTargetInputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** A class to test and experiment with the {@link Face} behavior.
 * 
 * @autor davebaol */
public class Box2dFaceTest extends Box2dSteeringTest {
	Box2dSteeringEntity character;
	Box2dSteeringEntity target;

	private Batch spriteBatch;

	public Box2dFaceTest (SteeringBehaviorsTest container) {
		super(container, "Face");
	}

	@Override
	public void create () {
		super.create();

		spriteBatch = new SpriteBatch();

		// Create character
		character = createSteeringEntity(world, container.greenFish, true);
		character.setMaxAngularAcceleration(1);
		character.setMaxAngularSpeed(7);

		// Create target
		target = createSteeringEntity(world, container.target);
		markAsSensor(target);
		inputProcessor = new Box2dTargetInputProcessor(target);

		final Face<Vector2> faceSB = new Face<Vector2>(character, target) //
			.setTimeToTarget(0.01f) //
			.setAlignTolerance(0.0001f) //
			.setDecelerationRadius(MathUtils.degreesToRadians * 120);
		character.setSteeringBehavior(faceSB);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxAngularAccelerationController(detailTable, character, 0, 10, .1f);

		detailTable.row();
		addMaxAngularSpeedController(detailTable, character, 0, 20, .5f);

		detailTable.row();
		final Label labelDecelerationRadius = new Label("Deceleration Radius [" + faceSB.getDecelerationRadius() + "]",
			container.skin);
		detailTable.add(labelDecelerationRadius);
		detailTable.row();
		Slider decelerationRadius = new Slider(0, MathUtils.PI2, MathUtils.degreesToRadians, false, container.skin);
		decelerationRadius.setValue(faceSB.getDecelerationRadius());
		decelerationRadius.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				faceSB.setDecelerationRadius(slider.getValue());
				labelDecelerationRadius.setText("Deceleration Radius [" + slider.getValue() + "]");
			}
		});
		detailTable.add(decelerationRadius);

		detailTable.row();
		final Label labelAlignTolerance = new Label("Align tolerance [" + faceSB.getAlignTolerance() + "]", container.skin);
		detailTable.add(labelAlignTolerance);
		detailTable.row();
		Slider alignTolerance = new Slider(0, 1, 0.0001f, false, container.skin);
		alignTolerance.setValue(faceSB.getAlignTolerance());
		alignTolerance.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				faceSB.setAlignTolerance(slider.getValue());
				labelAlignTolerance.setText("Align tolerance [" + slider.getValue() + "]");
			}
		});
		detailTable.add(alignTolerance);

		detailTable.row();
		final Label labelTimeToTarget = new Label("Time to Target [" + faceSB.getTimeToTarget() + " sec.]", container.skin);
		detailTable.add(labelTimeToTarget);
		detailTable.row();
		Slider timeToTarget = new Slider(0, 3, 0.1f, false, container.skin);
		timeToTarget.setValue(faceSB.getTimeToTarget());
		timeToTarget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				faceSB.setTimeToTarget(slider.getValue());
				labelTimeToTarget.setText("Time to Target [" + slider.getValue() + " sec.]");
			}
		});
		detailTable.add(timeToTarget);

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
		// Draw character and target
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
