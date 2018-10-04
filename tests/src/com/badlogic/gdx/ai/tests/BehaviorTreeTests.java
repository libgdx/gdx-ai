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

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeTestBase;
import com.badlogic.gdx.ai.tests.btree.tests.IncludeSubtreeTest;
import com.badlogic.gdx.ai.tests.btree.tests.ParallelVsSequenceTest;
import com.badlogic.gdx.ai.tests.btree.tests.ParseAndCloneTreeTest;
import com.badlogic.gdx.ai.tests.btree.tests.ParseTreeTest;
import com.badlogic.gdx.ai.tests.btree.tests.ProgrammaticallyCreatedTreeTest;
import com.badlogic.gdx.ai.tests.btree.tests.ResumeVsJoinTest;
import com.badlogic.gdx.ai.tests.btree.tests.SemaphoreGuardTest;
import com.badlogic.gdx.ai.tests.utils.GdxAiTestUtils;
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
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** Test class for behavior trees.
 * 
 * @author davebaol */
public class BehaviorTreeTests extends Game {

	public static void main (String[] argv) {
		GdxAiTestUtils.launch(new BehaviorTreeTests());
	}

	@Override
	public void create () {
		setScreen(new MainScreen());
	}

	public class MainScreen extends ScreenAdapter {
		private static final boolean DEBUG_STAGE = false;

		TextButton pauseButton;
		private Label testDescriptionLabel;

		// @off - disable libgdx formatter
		private BehaviorTreeTestBase[] tests = {
			new ParseTreeTest(),
			new ParseAndCloneTreeTest(false),
			new ParseAndCloneTreeTest(true),
			new IncludeSubtreeTest(false),
			new IncludeSubtreeTest(true),
			new ParallelVsSequenceTest(BehaviorTreeTests.this),
			new ResumeVsJoinTest(BehaviorTreeTests.this),
			new ProgrammaticallyCreatedTreeTest(false),
			new ProgrammaticallyCreatedTreeTest(true),
			new SemaphoreGuardTest()
		};
		// @on - enable libgdx formatter

		private BehaviorTreeTestBase currentTest;

		private SplitPane splitPane;

		private Stage stage;
		private Skin skin;

		public MainScreen() {
			Gdx.gl.glClearColor(.3f, .3f, .3f, 1);

			skin = new Skin(Gdx.files.internal("data/uiskin.json"));

			stage = new Stage(new ScreenViewport());
			stage.setDebugAll(DEBUG_STAGE);

			// Create split pane
			List<String> testList = createTestList();
			ScrollPane leftScrollPane = new ScrollPane(testList, skin);
			splitPane = new SplitPane(leftScrollPane, null, false, skin, "default-horizontal");
			splitPane.setSplitAmount(Math.min((testList.getPrefWidth() + 10) / stage.getWidth(), splitPane.getSplitAmount()));

			// Create layout
			Table t = new Table(skin);
			t.setFillParent(true);
			t.add(splitPane).colspan(3).grow();
			t.row();
			t.add(pauseButton = new PauseButton(skin)).width(90).left();
			t.add(new FpsLabel("FPS: ", skin)).left();
			t.add(testDescriptionLabel = new Label("", skin)).left();
			stage.addActor(t);

			// Set selected test
			changeTest(0);
		}

		@Override
		public void show () {
			Gdx.input.setInputProcessor(stage);
		}

		@Override
		public void render (float delta) {
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			stage.act(delta);
			stage.draw();
		}

		@Override
		public void resize (int width, int height) {
			stage.getViewport().update(width, height, true);
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
				testNames[i] = tests[i].getName();
			}

			final List<String> testList = new List<String>(skin);
			testList.setItems(testNames);
			testList.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					changeTest(testList.getSelectedIndex());
				}
			});
			return testList;
		}

		private void changeTest (int testIndex) {
			// Dispose the previous test (if any)
			if (currentTest != null) currentTest.dispose();

			// Add the new test
			currentTest = tests[testIndex];
			Gdx.app.log("BehaviorTreeTests", "***********************************************");
			Gdx.app.log("BehaviorTreeTests", "Starting test " + currentTest.getName());
			Gdx.app.log("BehaviorTreeTests", "***********************************************");
			String description = currentTest.getDescription();
			if (description != null) {
				Gdx.app.log("BehaviorTreeTests", description);
				Gdx.app.log("BehaviorTreeTests", "***********************************************");
				testDescriptionLabel.setText(description);
			} else {
				testDescriptionLabel.setText("Run the tree, look at the log and see what happens");
			}
			Stack testStack = new Stack() {
				@Override
				public void act (float delta) {
					boolean paused = pauseButton.isChecked();
					getChildren().peek().setVisible(paused);
					if (!paused) {
						// Update AI time
						GdxAI.getTimepiece().update(delta);
						
						// Call super
						super.act(delta);
					}
				}
			};
			testStack.add(currentTest.createActor(skin));
			testStack.add(new Image(skin, "translucent"));
			splitPane.setSecondWidget(testStack);
		}
		
	}

}
