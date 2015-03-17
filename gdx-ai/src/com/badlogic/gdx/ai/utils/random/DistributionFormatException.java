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

package com.badlogic.gdx.ai.utils.random;

/** Thrown to indicate that the application has attempted to convert a string to one of the distribution types, but that the string
 * does not have the appropriate format.
 * 
 * @author davebaol */
public class DistributionFormatException extends RuntimeException {

	/** Constructs an <code>IllegalArgumentException</code> with no detail message. */
	public DistributionFormatException () {
		super();
	}

	/** Constructs an <code>IllegalArgumentException</code> with the specified detail message.
	 *
	 * @param s the detail message. */
	public DistributionFormatException (String s) {
		super(s);
	}

	/** Constructs a new exception with the specified detail message and cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically incorporated in this exception's
	 * detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
	 * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A <tt>null</tt>
	 *           value is permitted, and indicates that the cause is nonexistent or unknown.) */
	public DistributionFormatException (String message, Throwable cause) {
		super(message, cause);
	}

	/** Constructs a new exception with the specified cause and a detail message of <tt>(cause==null ? null : cause.toString())</tt>
	 * (which typically contains the class and detail message of <tt>cause</tt>). This constructor is useful for exceptions that
	 * are little more than wrappers for other throwables.
	 *
	 * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A <tt>null</tt>
	 *           value is permitted, and indicates that the cause is nonexistent or unknown.) */
	public DistributionFormatException (Throwable cause) {
		super(cause);
	}

}
