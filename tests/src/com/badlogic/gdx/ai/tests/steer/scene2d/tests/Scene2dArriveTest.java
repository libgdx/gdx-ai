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

import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.Scene2dSteeringTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.SteeringActor;
import com.badlogic.gdx.ai.tests.steer.scene2d.Scene2dTargetInputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** A class to test and experiment with the {@link Arrive} behavior.
 * 
 * @autor davebaol */
public class Scene2dArriveTest extends Scene2dSteeringTest {
	SteeringActor character;
	SteeringActor target;

	public Scene2dArriveTest (SteeringBehaviorsTest container) {
		super(container, "Arrive");
	}

	@Override
	public void create (Table table) {
		character = new SteeringActor(container.badlogicSmall, false);
		target = new SteeringActor(container.target);
		inputProcessor = new Scene2dTargetInputProcessor(target);

		// Set character's limiter
		character.setMaxLinearSpeed(100);
		character.setMaxLinearAcceleration(300);

		final Arrive<Vector2> arriveSB = new Arrive<Vector2>(character, target) //
			.setTimeToTarget(0.1f) //
			.setArrivalTolerance(0.001f) //
			.setDecelerationRadius(80);
		character.setSteeringBehavior(arriveSB);

		table.addActor(character);
		table.addActor(target);

		character.setPosition(container.stageWidth / 2, container.stageHeight / 2, Align.center);
		target.setPosition(MathUtils.random(container.stageWidth), MathUtils.random(container.stageHeight), Align.center);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 2000, 20);

		detailTable.row();
		addMaxLinearSpeedController(detailTable, character, 0, 300, 10);

		detailTable.row();
		final Label labelDecelerationRadius = new Label("Deceleration Radius [" + arriveSB.getDecelerationRadius() + "]",
			container.skin);
		detailTable.add(labelDecelerationRadius);
		detailTable.row();
		Slider decelerationRadius = new Slider(0, 150, 1, false, container.skin);
		decelerationRadius.setValue(arriveSB.getDecelerationRadius());
		decelerationRadius.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				arriveSB.setDecelerationRadius(slider.getValue());
				labelDecelerationRadius.setText("Deceleration Radius [" + slider.getValue() + "]");
			}
		});
		detailTable.add(decelerationRadius);

		detailTable.row();
		final Label labelArrivalTolerance = new Label("Arrival tolerance [" + arriveSB.getArrivalTolerance() + "]", container.skin);
		detailTable.add(labelArrivalTolerance);
		detailTable.row();
		Slider arrivalTolerance = new Slider(0, 1, 0.0001f, false, container.skin);
		arrivalTolerance.setValue(arriveSB.getArrivalTolerance());
		arrivalTolerance.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				arriveSB.setArrivalTolerance(slider.getValue());
				labelArrivalTolerance.setText("Arrival tolerance [" + slider.getValue() + "]");
			}
		});
		detailTable.add(arrivalTolerance);

		detailTable.row();
		final Label labelTimeToTarget = new Label("Time to Target [" + arriveSB.getTimeToTarget() + " sec.]", container.skin);
		detailTable.add(labelTimeToTarget);
		detailTable.row();
		Slider timeToTarget = new Slider(0, 3, 0.1f, false, container.skin);
		timeToTarget.setValue(arriveSB.getTimeToTarget());
		timeToTarget.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				arriveSB.setTimeToTarget(slider.getValue());
				labelTimeToTarget.setText("Time to Target [" + slider.getValue() + " sec.]");
			}
		});
		detailTable.add(timeToTarget);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
	}

	@Override
	public void dispose () {
	}

}
