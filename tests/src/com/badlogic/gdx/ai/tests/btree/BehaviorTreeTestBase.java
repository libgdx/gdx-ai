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

package com.badlogic.gdx.ai.tests.btree;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.tests.BehaviorTreeTests;
import com.badlogic.gdx.ai.tests.utils.scene2d.CollapsableWindow;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** Base class for individual behavior tree tests.
 * 
 * @author davebaol */
public abstract class BehaviorTreeTestBase {
	protected BehaviorTreeTests container;
	public String testName;
	protected InputProcessor inputProcessor;
	protected CollapsableWindow detailWindow;

	public BehaviorTreeTestBase (BehaviorTreeTests container, String name) {
		this(container, name, null);
	}

	public BehaviorTreeTestBase (BehaviorTreeTests container, String testName, InputProcessor inputProcessor) {
		this.container = container;
		this.testName = testName;
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
		CollapsableWindow window = new CollapsableWindow(this.testName, container.skin);
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
}
