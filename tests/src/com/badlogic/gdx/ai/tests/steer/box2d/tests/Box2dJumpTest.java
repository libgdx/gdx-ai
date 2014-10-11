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
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.behaviors.Jump;
import com.badlogic.gdx.ai.steer.behaviors.Jump.GravityComponentHandler;
import com.badlogic.gdx.ai.steer.behaviors.Jump.JumpCallback;
import com.badlogic.gdx.ai.steer.behaviors.Jump.JumpDescriptor;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.limiters.LinearLimiter;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringEntity;
import com.badlogic.gdx.ai.tests.steer.box2d.Box2dSteeringTest;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** A class to test and experiment with the {@link Jump} behavior.
 * @author davebaol */
public class Box2dJumpTest extends Box2dSteeringTest {
	
	static final int PLATFORM_HALF_HEIGHT = 15;
	static final int PLATFORM_HALF_WIDTH = 200;
	static final int PLATFORM_Y = 100;

	static final GravityComponentHandler<Vector2> GRAVITY_COMPONENT_HANDLER = new GravityComponentHandler<Vector2>() {

		@Override
		public float getComponent (Vector2 vector) {
			return vector.y;
		}

		@Override
		public void setComponent (Vector2 vector, float value) {
			vector.y = value;
		}
	};

	boolean drawDebug;
	ShapeRenderer shapeRenderer;

	private World world;
	private Batch spriteBatch;
	private Body leftPlatform;
	private Body rightPlatform;

	Box2dSteeringEntity character;

	JumpDescriptor<Vector2> jumpDescriptor;
	Jump<Vector2> jumpSB;
	
	Seek<Vector2> seekSB;

	int airbornePlanarVelocityToUse = 0;
	float runUpLength = 3.5f;

	public Box2dJumpTest (SteeringBehaviorsTest container) {
		super(container, "Jump");
	}

	@Override
	public void create (Table table) {
		Jump.DEBUG_ENABLED = true; 

		drawDebug = true;

		shapeRenderer = new ShapeRenderer();

		spriteBatch = new SpriteBatch();

		// Instantiate a new World with gravity
		world = createWorld(-9.81f);
		
		setContactListener();

		// next we create 2 static ground platforms. This platform
		// are not movable and will not react to any influences from
		// outside. They will however influence other bodies.
		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(Box2dSteeringTest.pixelsToMeters(PLATFORM_HALF_WIDTH), Box2dSteeringTest.pixelsToMeters(PLATFORM_HALF_HEIGHT));

		// next we create the body for the ground platform. It's
		// simply a static body.
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyType.StaticBody;

		groundBodyDef.position.set(Box2dSteeringTest.pixelsToMeters(PLATFORM_HALF_WIDTH), Box2dSteeringTest.pixelsToMeters(PLATFORM_Y));
		leftPlatform = world.createBody(groundBodyDef);

		groundBodyDef.position.set(Box2dSteeringTest.pixelsToMeters((int)container.stageWidth - PLATFORM_HALF_WIDTH), Box2dSteeringTest.pixelsToMeters(PLATFORM_Y));
		rightPlatform = world.createBody(groundBodyDef);

		// finally we add a fixture to the body using the polygon
		// defined above. Note that we have to dispose PolygonShapes
		// and CircleShapes once they are no longer used. This is the
		// only time you have to care explicitly for memory management.
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = groundPoly;
		fixtureDef.filter.groupIndex = 0;
		leftPlatform.createFixture(fixtureDef);
		rightPlatform.createFixture(fixtureDef);
		groundPoly.dispose();

		// Create character
		character = createSteeringEntity(world, container.badlogicSmall, true);
		character.setMaxLinearSpeed(4);
		character.setMaxLinearAcceleration(200);
		// Remove all stuff that causes jump failure
		// Notice that you might remove this on takeoff and restore on landing
//		character.body.setSleepingThresholds(0, 0);
//		character.body.setDamping(0, 0);
//		character.body.setFriction(0);
// character.body.setMassProps(1, new Vector3(0,0,0));
// character.body.setAnisotropicFriction(new Vector3(0,0,0)); // ???
		
		Vector2 takeoffPoint = new Vector2(leftPlatform.getPosition()).add(Box2dSteeringTest.pixelsToMeters(PLATFORM_HALF_WIDTH-20), Box2dSteeringTest.pixelsToMeters(PLATFORM_HALF_HEIGHT)+character.getBoundingRadius());
		Vector2 landingPoint = new Vector2(rightPlatform.getPosition()).add(Box2dSteeringTest.pixelsToMeters(20-PLATFORM_HALF_WIDTH), Box2dSteeringTest.pixelsToMeters(PLATFORM_HALF_HEIGHT)+character.getBoundingRadius());
		System.out.println("takeoffPoint: " + takeoffPoint);
		System.out.println("landingPoint: " + landingPoint);
		jumpDescriptor = new JumpDescriptor<Vector2>(takeoffPoint, landingPoint);

		JumpCallback jumpCallback = new JumpCallback() {
			JumpDescriptor<Vector2> newJumpDescriptor = new JumpDescriptor<Vector2>(new Vector2(), new Vector2());

			@Override
			public void reportAchievability (boolean achievable) {
				System.out.println("Jump Achievability = " + achievable);
			}

			@Override
			public void takeoff (float maxVerticalVelocity, float time) {
				System.out.println("Take off!!!");
				System.out.println("Character Velocity = " + character.getLinearVelocity() + "; Speed = "
					+ character.getLinearVelocity().len());
				float h = maxVerticalVelocity * maxVerticalVelocity / (-2f * jumpSB.getGravity().y);
				System.out.println("jump height = " + h);
				switch (airbornePlanarVelocityToUse) {
				case 0: // Use character velocity on takeoff
					character.getBody().setLinearVelocity(character.getBody().getLinearVelocity().add(0, maxVerticalVelocity));
					break;
				case 1: // Use predicted velocity. We are cheating!!!
					Vector2 targetLinearVelocity = jumpSB.getTarget().getLinearVelocity();
					character.getBody().setLinearVelocity(newJumpDescriptor.takeoffPosition.set(targetLinearVelocity.x,
						maxVerticalVelocity));
					break;
				case 2: // Calculate and use exact velocity. We are shamelessly cheating!!!
					Vector2 newLinearVelocity = character.getBody().getLinearVelocity();
					newJumpDescriptor.set(character.getPosition(), jumpSB.getJumpDescriptor().landingPosition);
					System.out.println("character.pos = " + character.getPosition());
					time = jumpSB.calculateAirborneTimeAndVelocity(newLinearVelocity, newJumpDescriptor, jumpSB.getLimiter()
						.getMaxLinearSpeed());
					character.getBody().setLinearVelocity(newLinearVelocity.add(0, maxVerticalVelocity));
					break;
				}
				character.setSteeringBehavior(null);
			}

		};
		jumpSB = new Jump<Vector2>(character, jumpDescriptor, world.getGravity(), GRAVITY_COMPONENT_HANDLER, jumpCallback) //
			.setMaxVerticalVelocity(5) //
			.setTakeoffPositionTolerance(.3f) //
			.setTakeoffVelocityTolerance(.7f) //
			.setTimeToTarget(.01f);

		// Setup the limiter for the run up
		jumpSB.setLimiter(new LinearLimiter(Float.POSITIVE_INFINITY, character.getMaxLinearSpeed() * 3));

		// Create Seek behavior with a fake target that is far away on the right
		seekSB = new Seek<Vector2>(character, new SteerableAdapter<Vector2>() {
			Vector2 pos = new Vector2(jumpDescriptor.landingPosition).add(1000, 0);

			@Override
			public Vector2 getPosition () {
				return pos;
			}
		});
		
		restart();

		Table detailTable = new Table(container.skin);

		detailTable.row();
		final Label labelRunUpLenght = new Label("Run Up Length [" + runUpLength + "]", container.skin);
		detailTable.add(labelRunUpLenght);
		detailTable.row();
		Slider sliderRunUpLenght = new Slider(0.1f, 4f, 0.1f, false, container.skin);
		sliderRunUpLenght.setValue(runUpLength);
		sliderRunUpLenght.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				runUpLength = slider.getValue();
				labelRunUpLenght.setText("Run Up Length [" + slider.getValue() + "]");
			}
		});
		detailTable.add(sliderRunUpLenght);

		detailTable.row();
		final Label labelTakeoffPosTol = new Label("Takeoff Pos.Tolerance [" + jumpSB.getTakeoffPositionTolerance() + "]",
			container.skin);
		detailTable.add(labelTakeoffPosTol);
		detailTable.row();
		Slider takeoffPosTol = new Slider(0.01f, 1f, 0.01f, false, container.skin);
		takeoffPosTol.setValue(jumpSB.getTakeoffPositionTolerance());
		takeoffPosTol.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				jumpSB.setTakeoffPositionTolerance(slider.getValue());
				labelTakeoffPosTol.setText("Takeoff Pos.Tolerance [" + slider.getValue() + "]");
			}
		});
		detailTable.add(takeoffPosTol);

		detailTable.row();
		final Label labelTakeoffVelTol = new Label("Takeoff Vel.Tolerance [" + jumpSB.getTakeoffVelocityTolerance() + "]",
			container.skin);
		detailTable.add(labelTakeoffVelTol);
		detailTable.row();
		Slider takeoffVelTol = new Slider(0.01f, 1f, 0.01f, false, container.skin);
		takeoffVelTol.setValue(jumpSB.getTakeoffVelocityTolerance());
		takeoffVelTol.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				jumpSB.setTakeoffVelocityTolerance(slider.getValue());
				labelTakeoffVelTol.setText("Takeoff Vel.Tolerance [" + slider.getValue() + "]");
			}
		});
		detailTable.add(takeoffVelTol);

		detailTable.row();
		final Label labelMaxVertVel = new Label("Max.Vertical Vel. [" + jumpSB.getMaxVerticalVelocity() + "]", container.skin);
		detailTable.add(labelMaxVertVel);
		detailTable.row();
		Slider maxVertVel = new Slider(1f, 15f, 0.5f, false, container.skin);
		maxVertVel.setValue(jumpSB.getMaxVerticalVelocity());
		maxVertVel.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Slider slider = (Slider)actor;
				jumpSB.setMaxVerticalVelocity(slider.getValue());
				labelMaxVertVel.setText("Max.Vertical Vel. [" + slider.getValue() + "]");
			}
		});
		detailTable.add(maxVertVel);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		final Label labelJumpVel = new Label("Airborne Planar Velocity To Use", container.skin);
		detailTable.add(labelJumpVel);
		detailTable.row();
		SelectBox<String> jumpVel = new SelectBox<String>(container.skin);
		jumpVel.setItems(new String[] {"Character Velocity on Takeoff", "Predicted Velocity (Cheat!!!)",
			"Calculate Exact Velocity (Cheat!!!)"});
		jumpVel.setSelectedIndex(0);
		jumpVel.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				@SuppressWarnings("unchecked")
				SelectBox<String> selectBox = (SelectBox<String>)actor;
				airbornePlanarVelocityToUse = selectBox.getSelectedIndex();
			}
		});
		detailTable.add(jumpVel);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		addMaxLinearAccelerationController(detailTable, character);

		detailTable.row();
		addMaxLinearSpeedController(detailTable, character);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		Button buttonRestart = new TextButton("Restart", container.skin);
		buttonRestart.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				restart();
			}
		});
		detailTable.add(buttonRestart);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		// Should the character switch to Jump behavior?
		if (character.getSteeringBehavior() == seekSB) {
			if (character.getPosition().x >= jumpDescriptor.takeoffPosition.x - runUpLength
				&& character.getPosition().x <= jumpDescriptor.landingPosition.x) {
				System.out.println("Switched to Jump behavior. Taking a run up...");
//				System.out.println("run up length = " + distFromTakeoffPoint);
//				character.body.setDamping(0, 0);
//				System.out.println("friction: " + character.body.getFriction());
//				character.body.setFriction(0);
				System.out.println("owner.linearVelocity = " + character.getLinearVelocity() + "; owner.linearSpeed = "
					+ character.getLinearVelocity().len());
				character.setSteeringBehavior(jumpSB);
			}
		}

		world.step(deltaTime, 8, 3);

		// Draw platforms
		renderBox(shapeRenderer, leftPlatform, PLATFORM_HALF_WIDTH, PLATFORM_HALF_HEIGHT);
		renderBox(shapeRenderer, rightPlatform, PLATFORM_HALF_WIDTH, PLATFORM_HALF_HEIGHT);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.identity();
		shapeRenderer.setColor(0, .6f, 0, 1);
		// Draw run up begin
		float runupX = Box2dSteeringTest.metersToPixels(jumpDescriptor.takeoffPosition.x-runUpLength);
		float runupY = Box2dSteeringTest.metersToPixels(jumpDescriptor.takeoffPosition.y);
		shapeRenderer.line(runupX, runupY+3, runupX, runupY-3);
		// Draw take-off pad
		drawPad(shapeRenderer, jumpDescriptor.takeoffPosition, jumpSB.getTakeoffPositionTolerance());
		// Draw landing pad
		float t = jumpSB.calculateAirborneTimeAndVelocity(tmp, jumpDescriptor, character.getMaxLinearSpeed());
		if (t < 0 ) {
			shapeRenderer.setColor(1, 0, 0, 1);
			t = 0;
		}
		drawPad(shapeRenderer, jumpDescriptor.landingPosition, jumpSB.getTakeoffPositionTolerance()+t*jumpSB.getTakeoffVelocityTolerance());
		shapeRenderer.end();

		// Update and draw the character
		character.update(deltaTime);
		spriteBatch.begin();
		character.draw(spriteBatch);
		spriteBatch.end();
	}

	private Vector2 tmp = new Vector2();

	private void drawPad(ShapeRenderer shapeRenderer, Vector2 centerPoint, float tolerance) {
		int centerX = Box2dSteeringTest.metersToPixels(centerPoint.x);
		int centerY = Box2dSteeringTest.metersToPixels(centerPoint.y);
		tolerance = Box2dSteeringTest.metersToPixels(tolerance);
		shapeRenderer.line(centerX-tolerance, centerY, centerX+tolerance, centerY);
		shapeRenderer.line(centerX, centerY+3, centerX, centerY-3);
	}

	@Override
	public void dispose () {
		shapeRenderer.dispose();
		world.dispose();
		spriteBatch.dispose();
	}

	private void restart () {
		Body body = character.getBody();
		body.setTransform(0.1f, pixelsToMeters(PLATFORM_Y + PLATFORM_HALF_HEIGHT) + character.getBoundingRadius(), body.getAngle());
		character.setSteeringBehavior(seekSB);
		jumpSB.setJumpDescriptor(jumpDescriptor); // prepare for a new jump
	}

	private void setContactListener () {
		world.setContactListener(new ContactListener() {

			@Override
			public void beginContact (Contact contact) {
				// Check to see if the flying character has landed
				if (character.getSteeringBehavior() == null) {
					Box2dSteeringEntity entityA = (Box2dSteeringEntity)contact.getFixtureA().getBody().getUserData();
					Box2dSteeringEntity entityB = (Box2dSteeringEntity)contact.getFixtureB().getBody().getUserData();
					if (entityA == character || entityB == character) {
						character.setSteeringBehavior(seekSB);
						jumpSB.setJumpDescriptor(jumpDescriptor); // prepare for a new jump
					}
				}
			}

			@Override
			public void endContact (Contact contact) {
			}

			@Override
			public void preSolve (Contact contact, Manifold oldManifold) {
			}

			@Override
			public void postSolve (Contact contact, ContactImpulse impulse) {
			}

		});
	}
}
