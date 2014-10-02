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

package com.badlogic.gdx.ai.tests.msg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.tests.TelegramProviderTest;
import com.badlogic.gdx.utils.Array;

/** @author avianey */
public class City implements Telegraph {

	Array<House> houses;

	public City () {
		Gdx.app.log(City.class.getSimpleName(), "A new city is born...");
		houses = new Array<House>();
		MessageDispatcher.getInstance().addListeners(this, TelegramProviderTest.MSG_TIME_TO_ACT);
	}

	@Override
	public boolean handleMessage (Telegram msg) {
		// build a new house
		if (houses.size <= 10) {
			houses.add(new House());
		}
		return false;
	}
}
