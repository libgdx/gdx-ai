/*******************************************************************************
 * Copyright 2017 See AUTHORS file.
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
package com.badlogic.gdx.ai.tests.btree.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Parallel.Orchestrator;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.tests.BehaviorTreeTests;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeTestBase;
import com.badlogic.gdx.ai.tests.steer.scene2d.SteeringActor;
import com.badlogic.gdx.ai.tests.utils.scene2d.FpsLabel;
import com.badlogic.gdx.ai.tests.utils.scene2d.IntValueLabel;
import com.badlogic.gdx.ai.tests.utils.scene2d.PauseButton;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A simple test to demonstrate the difference between the resume and join orchestrators in the Parallel task
 * @author Thomas Cashman
 */
public class ResumeVsJoinTest extends BehaviorTreeTestBase {

	private BehaviorTreeTests container;
	private Screen oldScreen;
	
	public ResumeVsJoinTest (BehaviorTreeTests container) {
		super("Predators: Resume vs. Join", "Notice how join predator will only spin around once after it reaches a target.");
		this.container = container;
	}

	@Override
	public Actor createActor (final Skin skin) {
		Table table = new Table();

		LabelStyle labelStyle = new LabelStyle(skin.get(LabelStyle.class));
		labelStyle.background = skin.newDrawable("white", .6f, .6f, .6f, 1);
		String branchChildren = "\n    spinAround\n    selectTarget\n    pursue";
		table.add(new Label("parallel policy:\"sequence\" orchestrator:\"resume\"" + branchChildren, labelStyle)).pad(5);
		table.add(new Label("vs", skin)).padLeft(10).padRight(10);
		table.add(new Label("parallel policy:\"sequence\" orchestrator:\"join\"" + branchChildren, labelStyle)).pad(5);

		table.row().padTop(15);

		TextButton startButton = new TextButton("Start", skin);
		startButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				oldScreen = container.getScreen();
				container.setScreen(new TestScreen(ResumeVsJoinTest.this, skin));
			}
		});
		table.add();
		table.add(startButton);
		table.add();
		return table;
	}

	private void backToPreviousScreen () {
		container.setScreen(oldScreen);
	}

	static class TestScreen extends ScreenAdapter {
		ResumeVsJoinTest test;

		Array<Sheep> sheeps;
		Predator resumePredator;
		Predator joinPredator;

		ShapeRenderer shapeRenderer;
		TextureRegion greenFishTextureRegion;
		TextureRegion badlogicTextureRegion;
		TextureRegion targetTextureRegion;
		Skin skin;
		Stage stage;
		Table testTable;
		TextButton pauseButton;
		TextButton gameOverButton;

		float lastUpdateTime;
		boolean gameOver;

		public TestScreen (final ResumeVsJoinTest test, Skin skin) {
			this.test = test;
			this.skin = skin;
			lastUpdateTime = 0;
			gameOver = false;

			greenFishTextureRegion = new TextureRegion(new Texture("data/green_fish.png"));
			badlogicTextureRegion = new TextureRegion(new Texture("data/badlogicsmall.jpg"));
			targetTextureRegion = new TextureRegion(new Texture("data/target.png"));

			shapeRenderer = new ShapeRenderer();

			stage = new Stage();

			Stack testStack = new Stack();
			stage.addActor(testStack);

			// Add translucent panel (it's only visible when AI is paused)
			final Image translucentPanel = new Image(skin, "translucent");
			translucentPanel.setSize(stage.getWidth(), stage.getHeight());
			translucentPanel.setVisible(false);
			stage.addActor(translucentPanel);

			// Create status bar
			Table statusBar = new Table(skin);
			statusBar.left().bottom();
			statusBar.row().height(26);
			statusBar.add(pauseButton = new PauseButton(translucentPanel, skin)).width(90).left();
			statusBar.add(new FpsLabel("FPS: ", skin)).padLeft(15);
			statusBar.add(new IntValueLabel("Resume (Fish): ", 0, skin) {
				@Override
				public int getValue () {
					return resumePredator.score;
				}
			}).padLeft(15);
			statusBar.add(new IntValueLabel("Join (Badlogics): ", 0, skin) {
				@Override
				public int getValue () {
					return joinPredator.score;
				}
			}).padLeft(15);
			stage.addActor(statusBar);

			// Add test table
			testStack.setSize(stage.getWidth(), stage.getHeight());
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

			this.sheeps = new Array<Sheep>();

			for (int i = 0; i < 30; i++) {
				Sheep sheep = new Sheep(targetTextureRegion);
				sheep.setMaxLinearAcceleration(50);
				sheep.setMaxLinearSpeed(80);
				sheep.setMaxAngularAcceleration(10); // greater than 0 because independent facing is enabled
				sheep.setMaxAngularSpeed(5);

				Wander<Vector2> wanderSB = new Wander<Vector2>(sheep) //
					.setFaceEnabled(true) // We want to use Face internally (independent facing is on)
					.setAlignTolerance(0.001f) // Used by Face
					.setDecelerationRadius(5) // Used by Face
					.setTimeToTarget(0.1f) // Used by Face
					.setWanderOffset(90) //
					.setWanderOrientation(10) //
					.setWanderRadius(40) //
					.setWanderRate(MathUtils.PI2 * 4);
				sheep.setSteeringBehavior(wanderSB);

				setRandomNonOverlappingPosition(sheep, sheeps, 5);
				setRandomOrientation(sheep);

				testTable.addActor(sheep);

				sheeps.add(sheep);
			}

			resumePredator = createPredator(false);
			joinPredator = createPredator(true);

			// Create GameOver panel
			gameOverButton = new TextButton("Game Over", skin);
			gameOverButton.setVisible(false);
			gameOverButton.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					test.backToPreviousScreen();
				}
			});
			testTable.add(gameOverButton);
		}

		private Predator createPredator (boolean join) {
			Predator predator = new Predator(join ? badlogicTextureRegion : greenFishTextureRegion, this);
			predator.setPosition(MathUtils.random(stage.getWidth()), MathUtils.random(stage.getHeight()), Align.center);
			predator.setMaxLinearSpeed(100);
			predator.setMaxLinearAcceleration(600);

			final Pursue<Vector2> pursueSB = new Pursue<Vector2>(predator, null, .5f);
			predator.setSteeringBehavior(pursueSB);
			testTable.addActor(predator);

			BranchTask<Predator> branch = join ? new Parallel<Predator>(Orchestrator.Join) : new Parallel<Predator>(Orchestrator.Resume);
			branch.addChild(new SpinAroundTask());
			branch.addChild(new SelectTargetTask());
			branch.addChild(new PursueTask());
			predator.btree = new BehaviorTree<Predator>(branch, predator);

			return predator;
		}

		@Override
		public void show () {
			Gdx.input.setInputProcessor(stage);
		}

		public void update (float delta) {
			if (!pauseButton.isChecked() && !gameOver) {
				// Update AI time
				GdxAI.getTimepiece().update(delta);

				// Update behavior trees
				resumePredator.btree.step();
				joinPredator.btree.step();
			}

			stage.act(delta);
		}

		@Override
		public void render (float delta) {
			update(delta);

			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			gameOver = sheeps.size == 0;
			if (gameOver) {
				if (!gameOverButton.isVisible()) {
					String winner = (resumePredator.score > joinPredator.score ? "Fish" : "Badlogics") + " wins!!!";
					if (resumePredator.score == joinPredator.score) winner = "There's no winner!!!";
					gameOverButton.setText("Game Over\n\n" + winner);
					gameOverButton.setVisible(true);
				}
			} else {
				Sheep target1 = resumePredator.target;
				Sheep target2 = joinPredator.target;
				if (target1 != null || target2 != null) {
					// Draw circles
					shapeRenderer.begin(ShapeType.Line);
					if (target1 != null) {
						shapeRenderer.setColor(Color.GREEN);
						shapeRenderer.circle(target1.getPosition().x, target1.getPosition().y, target1.getBoundingRadius() + 4);
					}
					if (target2 != null) {
						shapeRenderer.setColor(Color.RED);
						shapeRenderer.circle(target2.getPosition().x, target2.getPosition().y, target2.getBoundingRadius() + 6);
					}
					shapeRenderer.end();
				}
			}

			stage.draw();
		}

		@Override
		public void resize (int width, int height) {
			stage.getViewport().update(width, height, true);
		}

		@Override
		public void hide () {
			dispose();
		}

		@Override
		public void dispose () {
			stage.dispose();

			shapeRenderer.dispose();

			// Dispose textures
			greenFishTextureRegion.getTexture().dispose();
			badlogicTextureRegion.getTexture().dispose();
			targetTextureRegion.getTexture().dispose();
		}

		protected void setRandomNonOverlappingPosition (SteeringActor character, Array<? extends SteeringActor> others,
			float minDistanceFromBoundary) {
			int maxTries = Math.max(100, others.size * others.size);
			SET_NEW_POS:
			while (--maxTries >= 0) {
				character.setPosition(MathUtils.random(stage.getWidth()), MathUtils.random(stage.getHeight()), Align.center);
				character.getPosition().set(character.getX(Align.center), character.getY(Align.center));
				for (int i = 0; i < others.size; i++) {
					SteeringActor other = (SteeringActor)others.get(i);
					if (character.getPosition().dst(other.getPosition()) <= character.getBoundingRadius() + other.getBoundingRadius()
						+ minDistanceFromBoundary) continue SET_NEW_POS;
				}
				return;
			}
			throw new GdxRuntimeException("Probable infinite loop detected");
		}

		protected void setRandomOrientation (SteeringActor character) {
			float orientation = MathUtils.random(-MathUtils.PI, MathUtils.PI);
			character.setOrientation(orientation);
			if (!character.isIndependentFacing()) {
				// Set random initial non-zero linear velocity since independent facing is off
				character.angleToVector(character.getLinearVelocity(), orientation).scl(character.getMaxLinearSpeed() / 5);
			}
		}

		public static class Sheep extends SteeringActor {
			boolean eaten;

			public Sheep (TextureRegion region) {
				super(region, true);
				this.eaten = false;
			}
		}

		public static class Predator extends SteeringActor {
			Sheep target;
			TestScreen testScreen;
			BehaviorTree<Predator> btree;
			int score;
			float rotationOffset = 0f;

			public Predator (TextureRegion region, TestScreen testScreen) {
				super(region);
				this.testScreen = testScreen;
				this.score = 0;
			}
			
			@Override
			public float getRotation() {
				return (super.getRotation() + rotationOffset) % 360f;
			}
			
			public boolean spinAround() {
				if(rotationOffset < 360f) {
					rotationOffset += 4f;
					return false;
				} else {
					rotationOffset = 0f;
					return true;
				}
			}

			public void selectTarget () {
				target = null;
				((Pursue)getSteeringBehavior()).setTarget(null);
				Vector2 pos = getPosition();
				float minDist = Float.POSITIVE_INFINITY;
				for (Sheep sheep : testScreen.sheeps) {
					float dist = sheep.getPosition().dst2(pos);
					if (dist < minDist) {
						minDist = dist;
						target = sheep;
					}
				}
			}

			public boolean canEatTarget () {
				if (target.eaten) return false;
				float targetRadius = target.getBoundingRadius();
				return (!target.eaten && target.getPosition().dst2(getPosition()) < targetRadius * targetRadius);
			}

			public void eatTarget () {
				score++;
				target.eaten = true;
				testScreen.sheeps.removeValue(target, true);
				testScreen.testTable.removeActor(target);
			}
		}
		
		public static class SpinAroundTask extends LeafTask<Predator> {

			@Override
			public Status execute () {
				if(getObject().spinAround()) {
					return Status.SUCCEEDED;
				}
				return Status.RUNNING;
			}

			@Override
			protected Task<Predator> copyTo (Task<Predator> task) {
				return task;
			}

		}

		public static class SelectTargetTask extends LeafTask<Predator> {

			@Override
			public Status execute () {
				getObject().selectTarget();
				return Status.SUCCEEDED;
			}

			@Override
			protected Task<Predator> copyTo (Task<Predator> task) {
				return task;
			}

		}

		public static class PursueTask extends LeafTask<Predator> {

			@Override
			public Status execute () {
				Predator predator = getObject();
				boolean success = false;
				if (predator.target != null) {
					success = predator.canEatTarget();
					if (success)
						predator.eatTarget();
					else if (!predator.target.eaten) {
						((Pursue)predator.getSteeringBehavior()).setEnabled(true);
						((Pursue)predator.getSteeringBehavior()).setTarget(predator.target);
						return Status.RUNNING;
					}
				}
				((Pursue)predator.getSteeringBehavior()).setEnabled(false);
				return success ? Status.SUCCEEDED : Status.FAILED;
			}

			@Override
			protected Task<Predator> copyTo (Task<Predator> task) {
				return task;
			}
		}

	}
}
