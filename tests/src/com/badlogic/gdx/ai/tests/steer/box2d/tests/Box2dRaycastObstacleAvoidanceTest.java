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

package com.badlogic.gdx.ai.tests.steer.box2d.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Ray;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.RaycastCollisionDetector;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.ai.steer.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.rays.ParallelSideRayConfiguration;
import com.badlogic.gdx.ai.steer.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.steer.rays.SingleRayConfiguration;
import com.badlogic.gdx.ai.tests.SteeringBehaviorTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dRaycastCollisionDetector;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringEntity;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringTest;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** A class to test and experiment with the {@link RaycastObstacleAvoidance} behavior.
 * 
 * @autor davebaol */
public class Box2dRaycastObstacleAvoidanceTest extends Box2dSteeringTest {
	Box2dSteeringEntity character;
	int rayConfigurationIndex;
	RayConfigurationBase<Vector2>[] rayConfigurations;
	RaycastObstacleAvoidance<Vector2> raycastObstacleAvoidanceSB;
	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	private World world;
	private Body wall1;
	private Body wall2;
	private Body wall3;



	public Box2dRaycastObstacleAvoidanceTest (SteeringBehaviorTest container) {
		super(container, "Raycast Obstacle Avoidance");
	}

	@Override
	public void create (Table table) {
		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		// Instantiate a new World with no gravity
		world = createWorld();

		// next we create a static ground platform. This platform
		// is not movable and will not react to any influences from
		// outside. It will however influence other bodies. First we
		// create a PolygonShape that holds the form of the platform.
		// it will be 100 meters wide and 2 meters high, centered
		// around the origin
		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(Box2dSteeringTest.pixelsToMeters(150), Box2dSteeringTest.pixelsToMeters(20));

		// next we create the body for the ground platform. It's
		// simply a static body.
		BodyDef groundBodyDef = new BodyDef();

        groundBodyDef.position.set(Box2dSteeringTest.pixelsToMeters(200), Box2dSteeringTest.pixelsToMeters(350));
//        System.out.println("POSITION:" + groundBodyDef.position);
  //      System.exit(0);
		groundBodyDef.type = BodyType.StaticBody;
		wall1 = world.createBody(groundBodyDef);

        groundBodyDef.position.set(Box2dSteeringTest.pixelsToMeters(500), Box2dSteeringTest.pixelsToMeters(100));
		wall2 = world.createBody(groundBodyDef);

		groundBodyDef.position.set(Box2dSteeringTest.pixelsToMeters(350), Box2dSteeringTest.pixelsToMeters(200));
		wall3 = world.createBody(groundBodyDef);

		// finally we add a fixture to the body using the polygon
		// defined above. Note that we have to dispose PolygonShapes
		// and CircleShapes once they are no longer used. This is the
		// only time you have to care explicitly for memory management.
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = groundPoly;
		fixtureDef.filter.groupIndex = 0;
		wall1.createFixture(fixtureDef);
		groundPoly.setAsBox(Box2dSteeringTest.pixelsToMeters(20), Box2dSteeringTest.pixelsToMeters(80));
		wall2.createFixture(fixtureDef);
		groundPoly.setAsBox(Box2dSteeringTest.pixelsToMeters(50), Box2dSteeringTest.pixelsToMeters(30));
		wall3.createFixture(fixtureDef);
		groundPoly.dispose();
		
		CircleShape circleChape = new CircleShape();
		circleChape.setPosition(new Vector2());
        int radiusInPixels = 16;
		circleChape.setRadius(Box2dSteeringTest.pixelsToMeters(radiusInPixels));

		BodyDef characterBodyDef = new BodyDef();
		characterBodyDef.position.set(Box2dSteeringTest.pixelsToMeters(50), Box2dSteeringTest.pixelsToMeters(50));
//		characterBodyDef.angle = 90;
		characterBodyDef.type = BodyType.DynamicBody;
		Body characterBody = world.createBody(characterBodyDef);
		
		FixtureDef charFixtureDef = new FixtureDef();
		charFixtureDef.density = 1;
		charFixtureDef.shape = circleChape;
		charFixtureDef.filter.groupIndex = 0;
		characterBody.createFixture(charFixtureDef);

		circleChape.dispose();

		character = new Box2dSteeringEntity(container.greenFish, characterBody, radiusInPixels);
		//character.setMaxLinearSpeed(500);
		//character.setMaxLinearAcceleration(5000);
		//character.setMaxAngularAcceleration(40000);
		//character.setMaxAngularSpeed(15000);
        character.setMaxLinearSpeed(1);
        character.setMaxLinearAcceleration(5);
        character.setMaxAngularAcceleration(5);
        character.setMaxAngularSpeed(10);

		rayConfigurations = new RayConfigurationBase[] {new SingleRayConfiguration(character, 100),
			new ParallelSideRayConfiguration<Vector2>(character, 100, character.getBoundingRadius()),
			new CentralRayWithWhiskersConfiguration<Vector2>(character, 100, 40, 35 * MathUtils.degreesToRadians)};
		rayConfigurationIndex = 0;
		RaycastCollisionDetector<Vector2> raycastCollisionDetector = new Box2dRaycastCollisionDetector(world);
		raycastObstacleAvoidanceSB = new RaycastObstacleAvoidance<Vector2>(character, rayConfigurations[rayConfigurationIndex],
			raycastCollisionDetector, 40);

		Wander<Vector2> wanderSB = new Wander<Vector2>(character) //
			// Don't use Face internally because independent facing is off
			.setFaceEnabled(false) //
			// We don't need a limiter supporting angular components because Face is not used
			// No need to call setAlignTolerance, setDecelerationRadius and setTimeToTarget for the same reason
//			.setLimiter(new LinearAccelerationLimiter(1000)) //
			.setWanderOffset(60) //
			.setWanderOrientation(10) //
			.setWanderRadius(40) //
			.setWanderRate(MathUtils.PI / 5);

		PrioritySteering<Vector2> prioritySteeringSB = new PrioritySteering<Vector2>(character, 0.0001f) //
			.add(raycastObstacleAvoidanceSB) //
			.add(wanderSB);

		LookWhereYouAreGoing<Vector2> lookWhereYouAReGoingSB = new LookWhereYouAreGoing<Vector2>(character) {

			@Override
			protected SteeringAcceleration<Vector2> calculateSteering (SteeringAcceleration<Vector2> steering) {
				super.calculateSteering(steering);
				return steering;
			}

		}
			.setTimeToTarget(0.1f) //
			.setAlignTolerance(0.001f) //
			.setDecelerationRadius(MathUtils.PI/20);

		BlendedSteering<Vector2> xxx = new BlendedSteering<Vector2>(character) //
			.add(prioritySteeringSB, 1) //
			.add(lookWhereYouAReGoingSB, 1); 

		character.setSteeringBehavior(xxx);

		inputProcessor = null;

		Table detailTable = new Table(container.skin);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character, 0, 1500, 1);

		detailTable.row();
		final Label labelDistFromBoundary = new Label("Distance from Boundary ["
			+ raycastObstacleAvoidanceSB.getDistanceFromBoundary() + "]", container.skin);
		detailTable.add(labelDistFromBoundary);
		detailTable.row();
		Slider distFromBoundary = new Slider(0, 350, 1, false, container.skin);
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
		addMaxSpeedController(detailTable, character, 0, 500, 10);

		detailWindow = createDetailWindow(detailTable);
	}

	private Vector2 tmp = new Vector2();

	private Batch b;
	
	@Override
	public void render () {
		world.step(Gdx.graphics.getDeltaTime(), 8, 3);

		// next we render the ground body
		renderBox(wall1, 150, 20);
		renderBox(wall2, 20, 80);
		renderBox(wall3, 50, 30);

		if (drawDebug) {
			Ray<Vector2>[] rays = rayConfigurations[rayConfigurationIndex].getRays();
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1, 0, 0, 1);
			transform.idt();
			shapeRenderer.setTransformMatrix(transform);
			for (int i = 0; i < rays.length; i++) {
				Ray<Vector2> ray = rays[i];
				shapeRenderer.line(ray.origin, tmp.set(ray.origin).add(ray.direction));
			}
			shapeRenderer.end();
		}

		character.update();
		if (b == null)
			b = new SpriteBatch();
		b.begin();
		character.draw(b);
		b.end();

		renderBox(character.getBody(), character.getBoundingRadius(), character.getBoundingRadius());
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
		world.dispose();
	}

	Matrix4 transform = new Matrix4();

	private void renderBox (Body body, float halfWidth, float halfHeight) {
		// get the bodies center and angle in world coordinates
		Vector2 pos = body.getWorldCenter();
		float angle = body.getAngle();

		// set the translation and rotation matrix
		transform.setToTranslation(Box2dSteeringTest.metersToPixels(pos.x), Box2dSteeringTest.metersToPixels(pos.y), 0);
		transform.rotate(0, 0, 1, (float)Math.toDegrees(angle));

		// render the box
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setTransformMatrix(transform);
		shapeRenderer.setColor(1, 1, 1, 1);
		shapeRenderer.rect(-halfWidth, -halfHeight, halfWidth * 2, halfHeight * 2);
		shapeRenderer.end();
	}

}
