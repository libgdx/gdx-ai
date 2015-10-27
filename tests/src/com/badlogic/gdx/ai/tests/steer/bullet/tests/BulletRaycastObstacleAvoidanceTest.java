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

package com.badlogic.gdx.ai.tests.steer.bullet.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.ParallelSideRayConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.steer.utils.rays.SingleRayConfiguration;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.bullet.BulletSteeringTest;
import com.badlogic.gdx.ai.tests.steer.bullet.BulletSteeringUtils;
import com.badlogic.gdx.ai.tests.steer.bullet.SteeringBulletEntity;
import com.badlogic.gdx.ai.tests.utils.bullet.BulletEntity;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** A class to test and experiment with the {@link RaycastObstacleAvoidance} behavior.
 * @author Daniel Holderbaum
 * @author davebaol */
public class BulletRaycastObstacleAvoidanceTest extends BulletSteeringTest {

	SteeringBulletEntity character;
	int rayConfigurationIndex;
	RayConfigurationBase<Vector3>[] rayConfigurations;
	RaycastObstacleAvoidance<Vector3> raycastObstacleAvoidanceSB;

	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	public BulletRaycastObstacleAvoidanceTest (SteeringBehaviorsTest container) {
		super(container, "Raycast Obstacle Avoidance");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void create () {
		super.create();
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		world.add("ground", 0f, 0f, 0f).setColor(MathUtils.random(0.25f, 0.75f), MathUtils.random(0.25f, 0.75f),
			MathUtils.random(0.25f, 0.75f), 1f);

		createWalls();

		BulletEntity characterBase = world.add("capsule", new Matrix4());

		character = new SteeringBulletEntity(characterBase);
		character.setMaxLinearAcceleration(100);
		character.setMaxLinearSpeed(10);

		float rayLength = 6;
		rayConfigurations = new RayConfigurationBase[] {new SingleRayConfiguration<Vector3>(character, rayLength),
			new ParallelSideRayConfiguration<Vector3>(character, rayLength, character.getBoundingRadius()),
			new CentralRayWithWhiskersConfiguration<Vector3>(character, rayLength, rayLength / 2, 35 * MathUtils.degreesToRadians)};
		rayConfigurationIndex = 0;
		RaycastCollisionDetector<Vector3> raycastCollisionDetector = new BulletRaycastCollisionDetector(world.collisionWorld,
			character.body);
		raycastObstacleAvoidanceSB = new RaycastObstacleAvoidance<Vector3>(character, rayConfigurations[rayConfigurationIndex],
			raycastCollisionDetector, 7);

		Wander<Vector3> wanderSB = new Wander<Vector3>(character) //
			// Don't use Face internally because independent facing is off
			.setFaceEnabled(false) //
			// We don't need a limiter supporting angular components because Face is disabled
			// No need to call setAlignTolerance, setDecelerationRadius and setTimeToTarget for the same reason
			.setLimiter(new LinearAccelerationLimiter(10)) //
			.setWanderOffset(10) //
			.setWanderOrientation(1) //
			.setWanderRadius(8) //
			.setWanderRate(MathUtils.PI2 * 3.5f);

		PrioritySteering<Vector3> prioritySteeringSB = new PrioritySteering<Vector3>(character, 0.00001f) //
			.add(raycastObstacleAvoidanceSB) //
			.add(wanderSB);

		character.setSteeringBehavior(prioritySteeringSB);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 200, 1);

		detailTable.row();
		final Label labelDistFromBoundary = new Label("Distance from Boundary ["
			+ raycastObstacleAvoidanceSB.getDistanceFromBoundary() + "]", container.skin);
		detailTable.add(labelDistFromBoundary);
		detailTable.row();
		Slider distFromBoundary = new Slider(0, 10, 0.1f, false, container.skin);
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
		addMaxLinearSpeedController(detailTable, character);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void update () {
		character.update(GdxAI.getTimepiece().getDeltaTime());

		super.update();
	}

	@Override
	public void draw () {
		super.draw();

		if (drawDebug) {
			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
			Ray<Vector3>[] rays = rayConfigurations[rayConfigurationIndex].getRays();
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1, 1, 0, 1);
			shapeRenderer.setProjectionMatrix(camera.combined);
			for (int i = 0; i < rays.length; i++) {
				Ray<Vector3> ray = rays[i];
				shapeRenderer.line(ray.start, ray.end);
			}
			shapeRenderer.end();
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		shapeRenderer.dispose();
	}

	private void createWalls () {
		float side = 20; // Wall length
		int sides = MathUtils.random(4, 12);
		float angle = MathUtils.PI2 / sides;
		float radius = side / (2 * MathUtils.sin(MathUtils.PI / sides));
		float apothem = radius * MathUtils.cos(MathUtils.PI / sides);
		Vector3 v = new Vector3();
		for (int i = 0; i < sides; i++) {
			float a = angle * i;
			BulletSteeringUtils.angleToVector(v, a).scl(apothem);
			BulletEntity wall = world.add("staticwall", v.x, 0, v.z);
			wall.setColor(MathUtils.random(0.25f, 0.75f), MathUtils.random(0.25f, 0.75f), MathUtils.random(0.25f, 0.75f), 1f);
			wall.transform.rotateRad(Vector3.Y, a + MathUtils.PI / 2);
			wall.body.setWorldTransform(wall.transform);
		}
	}
}
