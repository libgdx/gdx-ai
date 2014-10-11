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

package com.badlogic.gdx.ai.tests.steer.box2d;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.tests.SteeringBehaviorsTest;
import com.badlogic.gdx.ai.tests.steer.SteeringTestBase;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Base class for box2d steering behavior tests.
 * 
 * @author davebaol */
public abstract class Box2dSteeringTest extends SteeringTestBase {

	public Box2dSteeringTest (SteeringBehaviorsTest container, String name) {
		this(container, name, null);
	}

	public Box2dSteeringTest (SteeringBehaviorsTest container, String name, InputProcessor inputProcessor) {
		super(container, "Box2d", name, inputProcessor);
	}

	/** Instantiate a new World with no gravity and tell it to sleep when possible. */
	public World createWorld () {
		return createWorld(0);
	}

	/** Instantiate a new World with the given gravity and tell it to sleep when possible. */
	public World createWorld (float y) {
		return new World(new Vector2(0, y), true);
	}

	protected void addMaxLinearAccelerationController (Table table, Limiter limiter) {
		addMaxLinearAccelerationController(table, limiter, 0, 500, 10);
	}

	protected void addMaxLinearSpeedController (Table table, Limiter limiter) {
		addMaxLinearSpeedController(table, limiter, 0, 500, 10);
	}

	protected void addMaxAngularAccelerationController (Table table, Limiter limiter) {
		addMaxAngularAccelerationController(table, limiter, 0, 50, 1);
	}

	protected void addMaxAngularSpeedController (Table table, Limiter limiter) {
		addMaxAngularSpeedController(table, limiter, 0, 20, 1);
	}

	public static float pixelsToMeters (int pixels) {
		return (float)pixels * 0.02f;
	}

	public static int metersToPixels (float meters) {
		return (int)(meters * 50.0f);
	}

	public Box2dSteeringEntity createSteeringEntity (World world, TextureRegion region) {
		return createSteeringEntity(world, region, false);
	}

	public Box2dSteeringEntity createSteeringEntity (World world, TextureRegion region, boolean independentFacing) {
		return createSteeringEntity(world, region, independentFacing, (int)(container.stageWidth / 2), (int)(container.stageHeight / 2));
	}

	public Box2dSteeringEntity createSteeringEntity (World world, TextureRegion region, int posX, int posY) {
		return createSteeringEntity(world, region, false, posX, posY);
	}

	public Box2dSteeringEntity createSteeringEntity (World world, TextureRegion region, boolean independentFacing, int posX, int posY) {

		CircleShape circleChape = new CircleShape();
		circleChape.setPosition(new Vector2());
		int radiusInPixels = (int)((region.getRegionWidth() + region.getRegionHeight()) / 4f);
		circleChape.setRadius(Box2dSteeringTest.pixelsToMeters(radiusInPixels));

		BodyDef characterBodyDef = new BodyDef();
		characterBodyDef.position.set(Box2dSteeringTest.pixelsToMeters(posX), Box2dSteeringTest.pixelsToMeters(posY));
		characterBodyDef.type = BodyType.DynamicBody;
		Body characterBody = world.createBody(characterBodyDef);

		FixtureDef charFixtureDef = new FixtureDef();
		charFixtureDef.density = 1;
		charFixtureDef.shape = circleChape;
		charFixtureDef.filter.groupIndex = 0;
		characterBody.createFixture(charFixtureDef);

		circleChape.dispose();

		return new Box2dSteeringEntity(region, characterBody, independentFacing, Box2dSteeringTest.pixelsToMeters(radiusInPixels));
	}

	public void markAsSensor (Box2dSteeringEntity character) {
		Array<Fixture> fixtures = character.getBody().getFixtureList();
		for (int i = 0; i < fixtures.size; i++) {
			fixtures.get(i).setSensor(true);
		}
	}

// protected void addAlignOrientationToLinearVelocityController (Table table, final SteeringActor character) {
// CheckBox alignOrient = new CheckBox("Align orient.to velocity", container.skin);
// alignOrient.setChecked(character.isIndependentFacing());
// alignOrient.addListener(new ClickListener() {
// @Override
// public void clicked (InputEvent event, float x, float y) {
// CheckBox checkBox = (CheckBox)event.getListenerActor();
// character.setIndependentFacing(checkBox.isChecked());
// }
// });
// table.add(alignOrient);
// }

	protected void setRandomNonOverlappingPosition (Box2dSteeringEntity character, Array<Box2dSteeringEntity> others,
		float minDistanceFromBoundary) {
		int maxTries = Math.max(100, others.size * others.size); 
		SET_NEW_POS:
		while (--maxTries >= 0) {
			int x = MathUtils.random((int)container.stageWidth);
			int y = MathUtils.random((int)container.stageHeight);
			float angle = MathUtils.random(-MathUtils.PI, MathUtils.PI);
			character.body.setTransform(pixelsToMeters(x), pixelsToMeters(y), angle);
			for (int i = 0; i < others.size; i++) {
				Box2dSteeringEntity other = (Box2dSteeringEntity)others.get(i);
				if (character.getPosition().dst(other.getPosition()) <= character.getBoundingRadius() + other.getBoundingRadius()
					+ minDistanceFromBoundary) continue SET_NEW_POS;
			}
			return;
		}
		throw new GdxRuntimeException("Probable infinite loop detected");
	}

	protected Matrix4 transform = new Matrix4();

	protected void renderBox (ShapeRenderer shapeRenderer, Body body, float halfWidth, float halfHeight) {
		// get the bodies center and angle in world coordinates
		Vector2 pos = body.getWorldCenter();
		float angle = body.getAngle();

		// set the translation and rotation matrix
		transform.setToTranslation(Box2dSteeringTest.metersToPixels(pos.x), Box2dSteeringTest.metersToPixels(pos.y), 0);
		transform.rotate(0, 0, 1, angle * MathUtils.radiansToDegrees);

		// render the box
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setTransformMatrix(transform);
		shapeRenderer.setColor(1, 1, 1, 1);
		shapeRenderer.rect(-halfWidth, -halfHeight, halfWidth * 2, halfHeight * 2);
		shapeRenderer.end();
	}
}
