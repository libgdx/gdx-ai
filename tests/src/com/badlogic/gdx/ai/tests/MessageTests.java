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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.tests.msg.MessageTestBase;
import com.badlogic.gdx.ai.tests.msg.tests.MessageTimerTest;
import com.badlogic.gdx.ai.tests.msg.tests.TelegramProviderTest;
import com.badlogic.gdx.ai.tests.utils.GdxAiTestUtils;
import com.badlogic.gdx.ai.tests.utils.scene2d.CollapsableWindow;
import com.badlogic.gdx.ai.tests.utils.scene2d.FpsLabel;
import com.badlogic.gdx.ai.tests.utils.scene2d.PauseButton;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** Main class for message tests.
 * 
 * @author davebaol */
public class MessageTests extends ApplicationAdapter {

	public static void main (String[] argv) {
		GdxAiTestUtils.launch(new MessageTests());
	}

	private static final boolean DEBUG_STAGE = false;

	public CollapsableWindow testSelectionWindow;
	Label testDescriptionLabel;
	TextButton pauseButton;

	// @off - disable libgdx formatter
	MessageTestBase[] tests = {
			new MessageTimerTest(this),
			new TelegramProviderTest(this)
	};
	// @on - enable libgdx formatter

	MessageTestBase currentTest;

	public Stage stage;
	public float stageWidth;
	public float stageHeight;
	public Skin skin;

	@Override
	public void create () {
		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		stage = new Stage();
		stage.setDebugAll(DEBUG_STAGE);
		stageWidth = stage.getWidth();
		stageHeight = stage.getHeight();

		Gdx.input.setInputProcessor(stage);

		// Add translucent panel (it's only visible when AI is paused)
		final Image translucentPanel = new Image(skin, "translucent");
		translucentPanel.setSize(stageWidth, stageHeight);
		translucentPanel.setVisible(false);
		stage.addActor(translucentPanel);

		// Create test selection window
		List<String> testList = createTestList();
		testSelectionWindow = addTestSelectionWindow("Tests", testList, 0, -1);

		// Create status bar
		Table statusBar = new Table(skin);
		statusBar.left().bottom();
		statusBar.row().height(26);
		statusBar.add(pauseButton = new PauseButton(translucentPanel, skin)).width(90).left();
		statusBar.add(new FpsLabel("FPS: ", skin)).padLeft(15);
		statusBar.add(testDescriptionLabel = new Label("", skin)).padLeft(15);
		stage.addActor(statusBar);

		// Set selected behavior
		changeTest(0);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Render current steering behavior test
		if (currentTest != null) {
			if (!pauseButton.isChecked()) {
				// Update AI time
				GdxAI.getTimepiece().update(Gdx.graphics.getDeltaTime());

				// Update test
				currentTest.update();
			}
			// Draw test
			currentTest.draw();
		}

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

	private List<String> createTestList () {
		// Create behavior names
		int numTests = tests.length;
		String[] testNames = new String[numTests];
		for (int i = 0; i < numTests; i++) {
			testNames[i] = tests[i].testName;
		}

		final List<String> testList = new List<String>(skin);
		testList.setItems(testNames);
		testList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!testSelectionWindow.isCollapsed() && getTapCount() == 2) {
					changeTest(testList.getSelectedIndex());
					testSelectionWindow.collapse();
				}
			}
		});
		return testList;
	}

	protected CollapsableWindow addTestSelectionWindow (String title, List<String> testList, float x, float y) {
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

	private void changeTest (int testIndex) {
		// Remove the old test and its window
		if (currentTest != null) {
			if (currentTest.getDetailWindow() != null) currentTest.getDetailWindow().remove();
			currentTest.dispose();
		}

		// Add the new test and its window
		currentTest = tests[testIndex];
		currentTest.create();
		testDescriptionLabel.setText(currentTest.getDescription());
		if (currentTest.getDetailWindow() != null) stage.addActor(currentTest.getDetailWindow());
	}

}
