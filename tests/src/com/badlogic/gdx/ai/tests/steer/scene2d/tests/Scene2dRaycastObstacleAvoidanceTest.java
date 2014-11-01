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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.ParallelSideRayConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.steer.utils.rays.SingleRayConfiguration;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dRaycastCollisionDetector;
import com.badlogic.gdx.ai.tests.steer.scene2d.Scene2dSteeringTest;
import com.badlogic.gdx.ai.tests.steer.scene2d.SteeringActor;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** A class to test and experiment with the {@link RaycastObstacleAvoidance} behavior.
 * 
 * @autor davebaol */
public class Scene2dRaycastObstacleAvoidanceTest extends Scene2dSteeringTest {
	SteeringActor character;
	int rayConfigurationIndex;
	RayConfigurationBase<Vector2>[] rayConfigurations;
	RaycastObstacleAvoidance<Vector2> raycastObstacleAvoidanceSB;
	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	private World world;

	private Body[] walls;
	private int[] walls_hw;
	private int[] walls_hh;

	public Scene2dRaycastObstacleAvoidanceTest (SteeringBehaviorsTest container) {
		super(container, "Raycast Obstacle Avoidance");
	}

	@Override
	public void create (Table table) {
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		// Instantiate a new World with no gravity
		// and tell it to sleep when possible.
		world = new World(new Vector2(0, 0), true);

		createRandomWalls(8);

		final SteeringActor character = new SteeringActor(container.greenFish, false);
		character.setPosition(50, 50, Align.center);
		character.setMaxLinearSpeed(100);
		character.setMaxLinearAcceleration(350);

		@SuppressWarnings("unchecked")
		RayConfigurationBase<Vector2>[] localRayConfigurations = new RayConfigurationBase[] {
			new SingleRayConfiguration<Vector2>(character, 100),
			new ParallelSideRayConfiguration<Vector2>(character, 100, character.getBoundingRadius()),
			new CentralRayWithWhiskersConfiguration<Vector2>(character, 100, 40, 35 * MathUtils.degreesToRadians)};
		rayConfigurations = localRayConfigurations;
		rayConfigurationIndex = 0;
		RaycastCollisionDetector<Vector2> raycastCollisionDetector = new Box2dRaycastCollisionDetector(world);
		raycastObstacleAvoidanceSB = new RaycastObstacleAvoidance<Vector2>(character, rayConfigurations[rayConfigurationIndex],
			raycastCollisionDetector, 40);

		Wander<Vector2> wanderSB = new Wander<Vector2>(character) //
			// Don't use Face internally because independent facing is off
			.setFaceEnabled(false) //
			// We don't need a limiter supporting angular components because Face is not used
			// No need to call setAlignTolerance, setDecelerationRadius and setTimeToTarget for the same reason
			.setLimiter(new LinearAccelerationLimiter(30)) //
			.setWanderOffset(60) //
			.setWanderOrientation(10) //
			.setWanderRadius(40) //
			.setWanderRate(MathUtils.PI / 5);

		PrioritySteering<Vector2> prioritySteeringSB = new PrioritySteering<Vector2>(character, 0.0001f) //
			.add(raycastObstacleAvoidanceSB) //
			.add(wanderSB);

		character.setSteeringBehavior(prioritySteeringSB);

		table.addActor(character);

		inputProcessor = null;

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 1500, 1);

		detailTable.row();
		final Label labelDistFromBoundary = new Label("Distance from Boundary ["
			+ raycastObstacleAvoidanceSB.getDistanceFromBoundary() + "]", container.skin);
		detailTable.add(labelDistFromBoundary);
		detailTable.row();
		Slider distFromBoundary = new Slider(0, 150, 1, false, container.skin);
		distFromBoundary.setValue(raycastObstacleAvoidanceSB.getDistanceFromBoundary());
		distFromBoundary.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				raycastObstacleAvoidanceSB.setDistanceFromBoundary(slider.getValue());
				labelDistFromBoundary.setText("Distance from Boundary [" + slider.getValue() + "]");
			}
		});
		detailTable.add(distFromBoundary);

		detailTable.row();
		final Label labelRayConfig = new Label("Ray Configuration", container.skin);
		detailTable.add(labelRayConfig);
		detailTable.row();
		SelectBox<String> rayConfig = new SelectBox<String>(container.skin);
		rayConfig.setItems(new String[] {"Single Ray", "Parallel Side Rays", "Central Ray with Whiskers"});
		rayConfig.setSelectedIndex(0);
		rayConfig.addListener(new ChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				SelectBox<String> selectBox = (SelectBox<String>)actor;
				rayConfigurationIndex = selectBox.getSelectedIndex();
				raycastObstacleAvoidanceSB.setRayConfiguration(rayConfigurations[rayConfigurationIndex]);
			}
		});
		detailTable.add(rayConfig);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		CheckBox debug = new CheckBox("Draw Rays", container.skin);
		debug.setChecked(drawDebug);
		debug.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				drawDebug = checkBox.isChecked();
			}
		});
		detailTable.add(debug);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		addMaxLinearSpeedController(detailTable, character, 80, 160, 1);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		world.step(Gdx.graphics.getDeltaTime(), 8, 3);

		// Draw the walls
		for (int i = 0; i < walls.length; i++) {
			renderBox(shapeRenderer, walls[i], walls_hw[i], walls_hh[i]);
		}

		if (drawDebug) {
			Ray<Vector2>[] rays = rayConfigurations[rayConfigurationIndex].getRays();
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1, 0, 0, 1);
			transform.idt();
			shapeRenderer.setTransformMatrix(transform);
			for (int i = 0; i < rays.length; i++) {
				Ray<Vector2> ray = rays[i];
				shapeRenderer.line(ray.start, ray.end);
			}
			shapeRenderer.end();
		}
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
		world.dispose();
	}

	Matrix4 transform = new Matrix4();

	private void renderBox (ShapeRenderer shapeRenderer, Body body, float halfWidth, float halfHeight) {
		// get the bodies center and angle in world coordinates
		Vector2 pos = body.getWorldCenter();
		float angle = body.getAngle();

		// set the translation and rotation matrix
		transform.setToTranslation(pos.x, pos.y, 0);
		transform.rotate(0, 0, 1, (float)Math.toDegrees(angle));

		// render the box
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setTransformMatrix(transform);
		shapeRenderer.setColor(1, 1, 1, 1);
		shapeRenderer.rect(-halfWidth, -halfHeight, halfWidth * 2, halfHeight * 2);
		shapeRenderer.end();
	}

	private void createRandomWalls (int n) {
		PolygonShape groundPoly = new PolygonShape();

		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = groundPoly;
		fixtureDef.filter.groupIndex = 0;

		walls = new Body[n];
		walls_hw = new int[n];
		walls_hh = new int[n];
		for (int i = 0; i < n; i++) {
			groundBodyDef.position.set(MathUtils.random(50, (int)container.stageWidth - 50),
				MathUtils.random(50, (int)container.stageHeight - 50));
			walls[i] = world.createBody(groundBodyDef);
			walls_hw[i] = (int)MathUtils.randomTriangular(20, 150);
			walls_hh[i] = (int)MathUtils.randomTriangular(30, 80);
			groundPoly.setAsBox(walls_hw[i], walls_hh[i]);
			walls[i].createFixture(fixtureDef);

		}
		groundPoly.dispose();
	}

}
