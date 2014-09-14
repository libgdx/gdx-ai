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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.tests.steer.scene2d.SteeringActor;
import com.badlogic.gdx.math.Vector2;

/** An {@link InputProcessor} that allows you to manually move a {@link SteeringActor}.
 * 
 * @autor davebaol */
public class Box2dTargetInputProcessor extends InputAdapter {
	protected Box2dSteeringEntity target;

	public Box2dTargetInputProcessor (Box2dSteeringEntity target) {
		this.target = target;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		setTargetPosition(screenX, screenY);
		return true;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		setTargetPosition(screenX, screenY);
		return true;
	}
	
	protected void setTargetPosition(int screenX, int screenY) {
		Vector2 pos = target.getPosition();
		screenY = Gdx.graphics.getHeight() - screenY;
		pos.x = Box2dSteeringTest.pixelsToMeters(screenX);
		pos.y = Box2dSteeringTest.pixelsToMeters(screenY);
		target.getBody().setTransform(pos, target.body.getAngle());
	}
}
