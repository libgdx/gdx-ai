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

package com.badlogic.gdx.ai.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.tests.msg.City;
import com.badlogic.gdx.ai.tests.utils.GdxAiTest;

/** A simple test to demonstrate telegram providers.<br/>
 * It builds an ideal city where every new citizen says hello to all the citizens that don't live in his house.
 * @author avianey */
public class TelegramProviderTest extends GdxAiTest {

	public static final int MSG_TIME_TO_ACT = 0;
	public static final int MSG_EXISTING_CITIZEN = 1;

	public static void main (String[] args) {
		launch(new TelegramProviderTest());
	}

	City city;
	float elapsedTime;

	@Override
	public void create () {
		elapsedTime = 0;
		// build a new city
		city = new City();

	}

	@Override
	public void render () {
		elapsedTime += Gdx.graphics.getRawDeltaTime();
		if (elapsedTime > 0.8f) {
			MessageDispatcher.getInstance().dispatchMessage(null, MSG_TIME_TO_ACT);
			elapsedTime = 0;
		}
	}
}
