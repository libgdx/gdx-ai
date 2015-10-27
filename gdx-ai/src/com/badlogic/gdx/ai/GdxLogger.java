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

package com.badlogic.gdx.ai;

import com.badlogic.gdx.Gdx;

/** @author davebaol */
public class GdxLogger implements Logger {

	public GdxLogger () {
	}

	@Override
	public void debug (String tag, String message) {
		Gdx.app.debug(tag, message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		Gdx.app.debug(tag, message, exception);
	}

	@Override
	public void info (String tag, String message) {
		Gdx.app.log(tag, message);
	}

	@Override
	public void info (String tag, String message, Throwable exception) {
		Gdx.app.log(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		Gdx.app.error(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		Gdx.app.error(tag, message, exception);
	}

}
