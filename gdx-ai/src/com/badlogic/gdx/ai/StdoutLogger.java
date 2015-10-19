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

/** A logger that writes to the standard output.
 * 
 * @author davebaol */
public class StdoutLogger implements Logger {

	public StdoutLogger () {
	}

	@Override
	public void debug (String tag, String message) {
		println("DEBUG", tag, message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		println("DEBUG", tag, message, exception);
	}

	@Override
	public void info (String tag, String message) {
		println("INFO", tag, message);
	}

	@Override
	public void info (String tag, String message, Throwable exception) {
		println("INFO", tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		println("ERROR", tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		println("ERROR", tag, message, exception);
	}

	private void println (String level, String tag, String message) {
		System.out.println(level + " " + tag + ": " + message);
	}

	private void println (String level, String tag, String message, Throwable exception) {
		println(level, tag, message);
		exception.printStackTrace();
	}

}
