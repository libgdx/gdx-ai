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

package com.badlogic.gdx.ai.tests.msg.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.TelegramProvider;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.tests.MessageTests;
import com.badlogic.gdx.ai.tests.msg.MessageTestBase;
import com.badlogic.gdx.utils.Array;

/** A simple test to demonstrate telegram providers.<br/>
 * It builds an ideal city where every new citizen says hello to all the citizens that don't live in his house.
 * @author avianey */
public class TelegramProviderTest extends MessageTestBase {

	public static final int MSG_TIME_TO_ACT = 0;
	public static final int MSG_EXISTING_CITIZEN = 1;

	City city;
	float elapsedTime;

	public TelegramProviderTest (MessageTests container) {
		super(container, "Telegram Providers");
	}

	@Override
	public String getDescription () {
		return "Creates a town where newcomers introduce themselves to neighbors";
	}

	@Override
	public void create () {
		super.create();
		MessageManager.getInstance().clear();
		elapsedTime = 0;
		// build a new city
		city = new City();

	}

	@Override
	public void update () {
		elapsedTime += GdxAI.getTimepiece().getDeltaTime();
		if (elapsedTime > 1.5f) {
			MessageManager.getInstance().dispatchMessage(null, MSG_TIME_TO_ACT);
			elapsedTime = 0;
		}
	}

	@Override
	public void draw () {
	}

	@Override
	public void dispose () {
		city = null;
		MessageManager.getInstance().clear();
		super.dispose();
	}

	static class City implements Telegraph {

		Array<House> houses;

		public City () {
			Gdx.app.log(City.class.getSimpleName(), "A new city is born...");
			houses = new Array<House>();
			MessageManager.getInstance().addListeners(this, TelegramProviderTest.MSG_TIME_TO_ACT);
		}

		@Override
		public boolean handleMessage (Telegram msg) {
			// build a new house
			if (houses.size <= 10) {
				houses.add(new House(this));
			}
			return false;
		}
	}

	static class House implements Telegraph {

		Array<Citizen> citizens;
		final int id;

		public House (City city) {
			this.id = city.houses.size + 1;
			citizens = new Array<Citizen>();
			Gdx.app.log(toString(), "New house in town");
			// Mr & Mrs
			citizens.add(new Citizen(this));
			citizens.add(new Citizen(this));
			MessageManager.getInstance().addListeners(this, TelegramProviderTest.MSG_TIME_TO_ACT);
		}

		@Override
		public boolean handleMessage (Telegram msg) {
			if (citizens.size < 3) {
				// new child
				Gdx.app.log(toString(), "We're having a baby!");
				citizens.add(new Citizen(this));
			}
			return false;
		}

		@Override
		public String toString () {
			return getClass().getSimpleName() + " " + id;
		}
	}

	static class Citizen implements Telegraph, TelegramProvider {

		final int id;
		final House house;

		public Citizen (House house) {
			this.id = house.citizens.size + 1;
			this.house = house;
			Gdx.app.log(toString(), "Hi there, I'm new in town and I live in house number " + house.id);
			MessageManager.getInstance().addListener(this, TelegramProviderTest.MSG_EXISTING_CITIZEN);
			MessageManager.getInstance().addProvider(this, TelegramProviderTest.MSG_EXISTING_CITIZEN);
		}

		@Override
		public boolean handleMessage (Telegram msg) {
			Citizen citizen = (Citizen)msg.extraInfo;
			// greet only if not in the same house
			if (this.house.id != citizen.house.id) {
				Gdx.app.log(toString(), "Hi " + citizen + ", I'm your new neighbour");
			}
			return false;
		}

		@Override
		public Object provideMessageInfo (int msg, Telegraph receiver) {
			// when a new citizen come to town we tell him that we exists
			return this;
		}

		@Override
		public String toString () {
			return getClass().getSimpleName() + " " + house.id + "." + id;
		}
	}
}
