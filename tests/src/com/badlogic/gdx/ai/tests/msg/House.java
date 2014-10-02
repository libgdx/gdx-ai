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
public class House implements Telegraph {

	static int NUM = 0;

	Array<Citizen> citizens;
	final int num;

	public House () {
		num = NUM++;
		citizens = new Array<Citizen>();
		Gdx.app.log(House.class.getSimpleName() + " " + num, "New house in town");
		// Mr & Mrs
		citizens.add(new Citizen(this));
		citizens.add(new Citizen(this));
		MessageDispatcher.getInstance().addListeners(this, TelegramProviderTest.MSG_TIME_TO_ACT);
	}

	@Override
	public boolean handleMessage (Telegram msg) {
		if (citizens.size < 3) {
			// new child
			Gdx.app.log(House.class.getSimpleName() + " " + num, "We're having a baby!");
			citizens.add(new Citizen(this));
		}
		return false;
	}
}
