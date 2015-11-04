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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
	Label testHelpLabel;
	TextButton pauseButton;

	// @off - disable libgdx formatter
	// Keep it sorted!
	MessageTestBase[] behaviors = {
			new MessageTimerTest(this),
			new TelegramProviderTest(this)
	};
	// @on - enable libgdx formatter

	MessageTestBase currentBehavior;

	public Stage stage;
	public float stageWidth;
	public float stageHeight;
	public Skin skin;
	String behaviorNames[][];

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
		statusBar.add(pauseButton = new TextButton("Pause AI", skin)).width(90).left();
		pauseButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				boolean pause = pauseButton.isChecked();
				pauseButton.setText(pause ? "Resume AI" : "Pause AI");
				translucentPanel.setVisible(pause);
			}
		});
		statusBar.add(new FpsLabel("FPS: ", skin)).padLeft(15);
		statusBar.add(testHelpLabel = new Label("", skin)).padLeft(15);
		stage.addActor(statusBar);

		// Set selected behavior
		changeTest(0);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Render current steering behavior test
		if (currentBehavior != null) {
			if (!pauseButton.isChecked()) {
				// Update AI time
				GdxAI.getTimepiece().update(Gdx.graphics.getDeltaTime());

				// Update test
				currentBehavior.update();
			}
			// Draw test
			currentBehavior.draw();
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
		if (currentBehavior != null) currentBehavior.dispose();

		stage.dispose();
		skin.dispose();
	}

	private List<String> createTestList () {
		// Create behavior names
		int numBehaviors = behaviors.length;
		String[] behaviorNames = new String[numBehaviors];
		for (int i = 0; i < numBehaviors; i++) {
			behaviorNames[i] = behaviors[i].testName;
		}

		final List<String> testList = new List<String>(skin);
		testList.setItems(behaviorNames);
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

	void changeTest (int behaviorIndex) {
		// Remove the old behavior and its window
		if (currentBehavior != null) {
			if (currentBehavior.getDetailWindow() != null) currentBehavior.getDetailWindow().remove();
			currentBehavior.dispose();
		}

		// Add the new behavior and its window
		currentBehavior = behaviors[behaviorIndex];
		currentBehavior.create();
		testHelpLabel.setText(currentBehavior.getHelpMessage());
		if (currentBehavior.getDetailWindow() != null) stage.addActor(currentBehavior.getDetailWindow());
	}

}
