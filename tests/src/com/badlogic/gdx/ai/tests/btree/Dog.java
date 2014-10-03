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

package com.badlogic.gdx.ai.tests.btree;

import com.badlogic.gdx.Gdx;

/** @author implicit-invocation */
public class Dog {

	public void bark () {
		Gdx.app.log("Dog", "Bow wow!!!");
	}

	public void randomlyWalk () {
		Gdx.app.log("Dog", "Dog walks randomly around!");
	}

	public boolean standBesideATree () {
		if (Math.random() < 0.5) {
			Gdx.app.log("Dog", "No tree found :(");
			return false;
		}
		return true;
	}

	public void markATree () {
		Gdx.app.log("Dog", "Dog lifts a leg and pee!");
	}

	private boolean urgent = false;

	public boolean isUrgent () {
		return urgent;
	}

	public void setUrgent (boolean urgent) {
		this.urgent = urgent;
	}

}
