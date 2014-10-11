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

package com.badlogic.gdx.ai.tests.steer;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.utils.scene2d.CollapsableWindow;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** Base class for cross-engine steering behavior tests.
 * 
 * @author davebaol */
public abstract class SteeringTestBase {
	protected SteeringBehaviorsTest container;
	public String engineName;
	public String behaviorName;
	protected InputProcessor inputProcessor;
	protected CollapsableWindow detailWindow;

	public SteeringTestBase (SteeringBehaviorsTest container, String engineName, String name) {
		this(container, engineName, name, null);
	}

	public SteeringTestBase (SteeringBehaviorsTest container, String engineName, String behaviorName, InputProcessor inputProcessor) {
		this.container = container;
		this.engineName = engineName;
		this.behaviorName = behaviorName;
		this.inputProcessor = inputProcessor;

		// Reset help message
		container.helpMessage = null;
	}

	public abstract void create (Table table);

	public abstract void render ();

	public abstract void dispose ();

	public InputProcessor getInputProcessor () {
		return inputProcessor;
	}

	public void setInputProcessor (InputProcessor inputProcessor) {
		this.inputProcessor = inputProcessor;
	}

	public CollapsableWindow getDetailWindow () {
		return detailWindow;
	}

	protected CollapsableWindow createDetailWindow (Table table) {
		CollapsableWindow window = new CollapsableWindow(this.engineName + " " + this.behaviorName, container.skin);
		window.row();
		window.add(table);
		window.pack();
		window.setX(container.stage.getWidth() - window.getWidth() + 1);
		window.setY(container.stage.getHeight() - window.getHeight() + 1);
		window.layout();
		window.collapse();
		return window;
	}

	protected void addSeparator (Table table) {
		Label lbl = new Label("", container.skin);
		lbl.setColor(0.75f, 0.75f, 0.75f, 1);
		lbl.setStyle(new LabelStyle(lbl.getStyle()));
		lbl.getStyle().background = container.skin.newDrawable("white");
		table.add(lbl).colspan(2).height(1).width(220).pad(5, 1, 5, 1);
	}

	//
	// Limiter controllers
	//

	protected void addMaxLinearAccelerationController (Table table, final Limiter limiter, float minValue, float maxValue,
		float step) {
		final Label labelMaxLinAcc = new Label("Max.Linear Acc.[" + limiter.getMaxLinearAcceleration() + "]", container.skin);
		table.add(labelMaxLinAcc);
		table.row();
		Slider maxLinAcc = new Slider(minValue, maxValue, step, false, container.skin);
		maxLinAcc.setValue(limiter.getMaxLinearAcceleration());
		maxLinAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				limiter.setMaxLinearAcceleration(slider.getValue());
				labelMaxLinAcc.setText("Max.Linear Acc.[" + limiter.getMaxLinearAcceleration() + "]");
			}
		});
		table.add(maxLinAcc);
	}

	protected void addMaxLinearSpeedController (Table table, final Limiter limiter, float minValue, float maxValue, float step) {
		final Label labelMaxSpeed = new Label("Max.Lin.Speed [" + limiter.getMaxLinearSpeed() + "]", container.skin);
		table.add(labelMaxSpeed);
		table.row();
		Slider maxSpeed = new Slider(minValue, maxValue, step, false, container.skin);
		maxSpeed.setValue(limiter.getMaxLinearSpeed());
		maxSpeed.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				limiter.setMaxLinearSpeed(slider.getValue());
				labelMaxSpeed.setText("Max.Lin.Speed [" + limiter.getMaxLinearSpeed() + "]");
			}
		});
		table.add(maxSpeed);
	}

	protected void addMaxAngularAccelerationController (Table table, final Limiter limiter, float minValue, float maxValue,
		float step) {
		final Label labelMaxAngAcc = new Label("Max.Ang.Acc.[" + limiter.getMaxAngularAcceleration() + "]", container.skin);
		table.add(labelMaxAngAcc);
		table.row();
		Slider maxAngAcc = new Slider(minValue, maxValue, step, false, container.skin);
		maxAngAcc.setValue(limiter.getMaxAngularAcceleration());
		maxAngAcc.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				limiter.setMaxAngularAcceleration(slider.getValue());
				labelMaxAngAcc.setText("Max.Ang.Acc.[" + limiter.getMaxAngularAcceleration() + "]");
			}
		});
		table.add(maxAngAcc);
	}

	protected void addMaxAngularSpeedController (Table table, final Limiter limiter, float minValue, float maxValue, float step) {
		final Label labelMaxAngSpeed = new Label("Max.Ang.Speed [" + limiter.getMaxAngularSpeed() + "]", container.skin);
		table.add(labelMaxAngSpeed);
		table.row();
		Slider maxAngSpeed = new Slider(minValue, maxValue, step, false, container.skin);
		maxAngSpeed.setValue(limiter.getMaxAngularSpeed());
		maxAngSpeed.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				limiter.setMaxAngularSpeed(slider.getValue());
				labelMaxAngSpeed.setText("Max.Ang.Speed [" + limiter.getMaxAngularSpeed() + "]");
			}
		});
		table.add(maxAngSpeed);
	}
}
