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

package com.badlogic.gdx.ai.tests.msg;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.tests.MessageTests;
import com.badlogic.gdx.ai.tests.utils.scene2d.CollapsableWindow;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** Base class for individual message tests.
 * 
 * @author davebaol */
public abstract class MessageTestBase {
	protected MessageTests container;
	public String testName;
	protected CollapsableWindow detailWindow;

	private float lastUpdateTime;
	private Stack testStack;
	protected Table testTable;

	public MessageTestBase (MessageTests container, String testName) {
		this.container = container;
		this.testName = testName;
	}

	public void create () {
		lastUpdateTime = 0;
		testStack = new Stack();
		container.stage.getRoot().addActorAt(0, testStack);
		testStack.setSize(container.stageWidth, container.stageHeight);
		testStack.add(testTable = new Table() {
			@Override
			public void act (float delta) {
				float time = GdxAI.getTimepiece().getTime();
				if (lastUpdateTime != time) {
					lastUpdateTime = time;
					super.act(GdxAI.getTimepiece().getDeltaTime());
				}
			}
		});
		testStack.layout();
	}

	public void dispose () {
		testStack.remove();
		testStack = null;
	}

	public abstract void update ();

	public abstract void draw ();

	public abstract String getDescription ();

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
