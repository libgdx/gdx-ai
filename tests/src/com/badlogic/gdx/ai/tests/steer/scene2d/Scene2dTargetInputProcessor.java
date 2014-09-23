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

package com.badlogic.gdx.ai.tests.steer.scene2d;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

/** An {@link InputProcessor} that allows you to manually move a {@link SteeringActor}.
 * 
 * @autor davebaol */
public class Scene2dTargetInputProcessor extends InputAdapter {
	protected SteeringActor target;

	public Scene2dTargetInputProcessor (SteeringActor target) {
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
		target.getStage().screenToStageCoordinates(pos.set(screenX, screenY));
		target.getParent().stageToLocalCoordinates(pos);
		target.setPosition(pos.x, pos.y, Align.center);
	}
}
