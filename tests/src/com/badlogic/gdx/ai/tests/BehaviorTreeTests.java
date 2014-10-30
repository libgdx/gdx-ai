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

package com.badlogic.gdx.ai.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeTestBase;
import com.badlogic.gdx.ai.tests.btree.tests.IncludeSubtreeTest;
import com.badlogic.gdx.ai.tests.btree.tests.ParseAndRunTest;
import com.badlogic.gdx.ai.tests.btree.tests.ParseCloneAndRunTest;
import com.badlogic.gdx.ai.tests.utils.GdxAiTest;
import com.badlogic.gdx.ai.tests.utils.scene2d.CollapsableWindow;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.StringBuilder;

/** Test class for behavior trees.
 * 
 * @author davebaol */
public class BehaviorTreeTests extends GdxAiTest {

	public static void main (String[] argv) {
		launch(new BehaviorTreeTests());
	}

	private static final boolean DEBUG_STAGE = false;

	public CollapsableWindow behaviorSelectionWindow;
	Label fpsLabel;
	StringBuilder fpsStringBuilder;
	public String helpMessage;

	// @off - disable libgdx formatter
	BehaviorTreeTestBase [] tests = {
		new ParseAndRunTest(this),
		new ParseCloneAndRunTest(this),
		new IncludeSubtreeTest(this, false),
		new IncludeSubtreeTest(this, true)
	};
	// @on - enable libgdx formatter

	Table testsTable;
	BehaviorTreeTestBase currentTest;

	public Stage stage;
	public float stageWidth;
	public float stageHeight;
	public Skin skin;

	@Override
	public void create () {
		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);

		fpsStringBuilder = new StringBuilder();

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		stage = new Stage();
		stage.setDebugAll(DEBUG_STAGE);
		stageWidth = stage.getWidth();
		stageHeight = stage.getHeight();

		Gdx.input.setInputProcessor(new InputMultiplexer(stage));

		Stack stack = new Stack();
		stage.addActor(stack);
		stack.setSize(stageWidth, stageHeight);
		testsTable = new Table();
		stack.add(testsTable);

		// Create behavior selection window
		List<String> testList = createTestList();
		behaviorSelectionWindow = addBehaviorSelectionWindow("Behavior Tree Tests", testList, 0, -1);

		// Set selected test
		changeTest(0);

		fpsLabel = new Label("FPS: 999", skin);
		stage.addActor(fpsLabel);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		fpsStringBuilder.setLength(0);
		getStatus(fpsStringBuilder);
		fpsLabel.setText(fpsStringBuilder);

		if (currentTest != null) currentTest.render();

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
		stageWidth = width;
		stageHeight = height;
	}

	@Override
	public void dispose () {
		if (currentTest != null) currentTest.dispose();

		stage.dispose();
		skin.dispose();
	}

	protected void getStatus (final StringBuilder stringBuilder) {
		stringBuilder.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
		if (helpMessage != null) stringBuilder.append("     ").append(helpMessage);
	}

	private List<String> createTestList () {
		// Create behavior names
		int numBehaviors = tests.length;
		String[] behaviorNames = new String[numBehaviors];
		for (int i = 0; i < numBehaviors; i++) {
			behaviorNames[i] = tests[i].testName;
		}

		final List<String> behaviorsList = new List<String>(skin);
		behaviorsList.setItems(behaviorNames);
		behaviorsList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!behaviorSelectionWindow.isCollapsed() && getTapCount() == 2) {
					changeTest(behaviorsList.getSelectedIndex());
					behaviorSelectionWindow.collapse();
				}
			}
		});
		return behaviorsList;
	}

	protected CollapsableWindow addBehaviorSelectionWindow (String title, List<String> testList, float x, float y) {

		CollapsableWindow window = new CollapsableWindow(title, skin);
		window.row();

		ScrollPane pane = new ScrollPane(testList, skin);
		pane.setFadeScrollBars(false);
		pane.setScrollX(0);
		pane.setScrollY(0);

		window.add(pane);
		window.pack();
		window.pack();
		if (window.getHeight() > stage.getHeight()) {
			window.setHeight(stage.getHeight());
		}
		window.setX(x < 0 ? stage.getWidth() - (window.getWidth() - (x + 1)) : x);
		window.setY(y < 0 ? stage.getHeight() - (window.getHeight() - (y + 1)) : y);
		window.layout();
		window.collapse();
		stage.addActor(window);

		return window;
	}

	void changeTest (int behaviorIndex) {
		// Remove the old behavior and its window
		testsTable.clear();
		if (currentTest != null) {
			if (currentTest.getDetailWindow() != null) currentTest.getDetailWindow().remove();
			currentTest.dispose();
		}

		// Add the new behavior and its window
		currentTest = tests[behaviorIndex];
		Gdx.app.log("BehaviorTreeTests", "***********************************************");
		Gdx.app.log("BehaviorTreeTests", "Starting test " + currentTest.getClass().getSimpleName());
		Gdx.app.log("BehaviorTreeTests", "***********************************************");
		currentTest.create(testsTable);
		InputMultiplexer im = (InputMultiplexer)Gdx.input.getInputProcessor();
		if (im.size() > 1) im.removeProcessor(1);
		if (currentTest.getInputProcessor() != null) im.addProcessor(currentTest.getInputProcessor());
		if (currentTest.getDetailWindow() != null) stage.addActor(currentTest.getDetailWindow());
	}

}
