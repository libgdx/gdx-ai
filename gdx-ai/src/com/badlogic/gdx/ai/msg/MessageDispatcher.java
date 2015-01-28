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

package com.badlogic.gdx.ai.msg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/** A {@code MessageDispatcher} is in charge of the creation, dispatch, and management of telegrams.
 * 
 * @author davebaol */
public class MessageDispatcher {

	private static final String LOG_TAG = MessageDispatcher.class.getSimpleName();

	private static final Pool<Telegram> pool = new Pool<Telegram>(16) {
		protected Telegram newObject () {
			return new Telegram();
		}
	};

	private PriorityQueue<Telegram> queue;

	private IntMap<Array<Telegraph>> msgListeners;

	private IntMap<Array<TelegramProvider>> msgProviders;

	private float currentTime;

	private boolean debugEnabled;

	/** Creates a {@code MessageDispatcher} */
	public MessageDispatcher () {
		this.queue = new PriorityQueue<Telegram>();
		this.msgListeners = new IntMap<Array<Telegraph>>();
		this.msgProviders = new IntMap<Array<TelegramProvider>>();
	}

	/** Returns the current time. */
	public float getCurrentTime () {
		return currentTime;
	}

	/** Returns true if debug mode is on; false otherwise. */
	public boolean isDebugEnabled () {
		return debugEnabled;
	}

	/** Sets debug mode on/off. */
	public void setDebugEnabled (boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	/** Registers a listener for the specified message code. Messages without an explicit receiver are broadcasted to all its
	 * registered listeners.
	 * @param listener the listener to add
	 * @param msg the message code */
	public void addListener (Telegraph listener, int msg) {
		Array<Telegraph> listeners = msgListeners.get(msg);
		if (listeners == null) {
			// Associate an empty unordered array with the message code
			listeners = new Array<Telegraph>(false, 16);
			msgListeners.put(msg, listeners);
		}
		listeners.add(listener);

		// Dispatch messages from registered providers
		Array<TelegramProvider> providers = msgProviders.get(msg);
		if (providers != null) {
			for (int i = 0; i < providers.size; i++) {
				TelegramProvider provider = providers.get(i);
				Object info = provider.provideMessageInfo(msg, listener);
				if (info != null) if (ClassReflection.isInstance(Telegraph.class, provider))
					dispatchMessage(0, (Telegraph)provider, listener, msg, info);
				else
					dispatchMessage(0, null, listener, msg, info);
			}
		}
	}

	/** Registers a listener for a selection of message types. Messages without an explicit receiver are broadcasted to all its
	 * registered listeners.
	 * 
	 * @param listener the listener to add
	 * @param msgs the message codes */
	public void addListeners (Telegraph listener, int... msgs) {
		for (int msg : msgs)
			addListener(listener, msg);
	}

	/** Registers a provider for the specified message code.
	 * @param msg the message code
	 * @param provider the provider to add */
	public void addProvider (TelegramProvider provider, int msg) {
		Array<TelegramProvider> providers = msgProviders.get(msg);
		if (providers == null) {
			// Associate an empty unordered array with the message code
			providers = new Array<TelegramProvider>(false, 16);
			msgProviders.put(msg, providers);
		}
		providers.add(provider);
	}

	/** Registers a provider for a selection of message types.
	 * @param provider the provider to add
	 * @param msgs the message codes */
	public void addProviders (TelegramProvider provider, int... msgs) {
		for (int msg : msgs)
			addProvider(provider, msg);
	}

	/** Unregister the specified listener for the specified message code.
	 * @param listener the listener to remove
	 * @param msg the message code */
	public void removeListener (Telegraph listener, int msg) {
		Array<Telegraph> listeners = msgListeners.get(msg);
		if (listeners != null) {
			listeners.removeValue(listener, true);
		}
	}

	/** Unregister the specified listener for the selection of message codes.
	 * 
	 * @param listener the listener to remove
	 * @param msgs the message codes */
	public void removeListener (Telegraph listener, int... msgs) {
		for (int msg : msgs)
			removeListener(listener, msg);
	}

	/** Unregisters all the listeners for the specified message code.
	 * @param msg the message code */
	public void clearListeners (int msg) {
		msgListeners.remove(msg);
	}

	/** Unregisters all the listeners for the given message codes.
	 * 
	 * @param msgs the message codes */
	public void clearListeners (int... msgs) {
		for (int msg : msgs)
			clearListeners(msg);
	}

	/** Removes all the registered listeners for all the message codes. */
	public void clearListeners () {
		msgListeners.clear();
	}

	/** Unregisters all the providers for the specified message code.
	 * @param msg the message code */
	public void clearProviders (int msg) {
		msgProviders.remove(msg);
	}

	/** Unregisters all the providers for the given message codes.
	 * 
	 * @param msgs the message codes */
	public void clearProviders (int... msgs) {
		for (int msg : msgs)
			clearProviders(msg);
	}

	/** Removes all the registered providers for all the message codes. */
	public void clearProviders () {
		msgProviders.clear();
	}

	/** Removes all the telegrams from the queue and releases them to the internal pool. */
	public void clearQueue () {
		for (int i = 0; i < queue.size(); i++) {
			pool.free(queue.get(i));
		}
		queue.clear();
		currentTime = 0;
	}

	/** Removes all the telegrams from the queue and the registered listeners for all the messages. */
	public void clear () {
		clearQueue();
		clearListeners();
		clearProviders();
	}

	/** Sends an immediate message to all registered listeners, with no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(0, sender,
	 * null, msg, null)}
	 * 
	 * @param sender the sender of the telegram
	 * @param msg the message code */
	public void dispatchMessage (Telegraph sender, int msg) {
		dispatchMessage(0f, sender, null, msg, null);
	}

	/** Sends an immediate message to all registered listeners, with extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(0, sender,
	 * null, msg, extraInfo)}
	 * 
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (Telegraph sender, int msg, Object extraInfo) {
		dispatchMessage(0f, sender, null, msg, extraInfo);
	}

	/** Sends an immediate message to the specified receiver with no extra info. The receiver doesn't need to be a register listener
	 * for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(0, sender,
	 * receiver, msg, null)}
	 * 
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code */
	public void dispatchMessage (Telegraph sender, Telegraph receiver, int msg) {
		dispatchMessage(0f, sender, receiver, msg, null);
	}

	/** Sends an immediate message to the specified receiver with extra info. The receiver doesn't need to be a register listener
	 * for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(0, sender,
	 * receiver, msg, extraInfo)}
	 * 
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (Telegraph sender, Telegraph receiver, int msg, Object extraInfo) {
		dispatchMessage(0f, sender, receiver, msg, extraInfo);
	}

	/** Sends a message to all registered listeners, with the specified delay but no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(delay,
	 * sender, null, msg, null)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param msg the message code */
	public void dispatchMessage (float delay, Telegraph sender, int msg) {
		dispatchMessage(delay, sender, null, msg, null);
	}

	/** Sends a message to all registered listeners, with the specified delay and extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(delay,
	 * sender, null, msg, extraInfo)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (float delay, Telegraph sender, int msg, Object extraInfo) {
		dispatchMessage(delay, sender, null, msg, extraInfo);
	}

	/** Sends a message to the specified receiver, with the specified delay but no extra info. The receiver doesn't need to be a
	 * register listener for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage(delay,
	 * sender, receiver, msg, null)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code */
	public void dispatchMessage (float delay, Telegraph sender, Telegraph receiver, int msg) {
		dispatchMessage(delay, sender, receiver, msg, null);
	}

	/** Given a message, a receiver, a sender and any time delay, this method routes the message to the correct agents (if no delay)
	 * or stores in the message queue to be dispatched at the correct time.
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (float delay, Telegraph sender, Telegraph receiver, int msg, Object extraInfo) {

		// Get a telegram from the pool
		Telegram telegram = pool.obtain();
		telegram.sender = sender;
		telegram.receiver = receiver;
		telegram.message = msg;
		telegram.extraInfo = extraInfo;

		// If there is no delay, route telegram immediately
		if (delay <= 0.0f) {
			if (debugEnabled)
				Gdx.app.log(LOG_TAG, "Instant telegram dispatched at time: " + currentTime + " by " + sender + " for " + receiver
					+ ". Msg is " + msg);

			// Send the telegram to the recipient
			discharge(telegram);
		} else {
			// Set the timestamp for the delayed telegram
			telegram.setTimestamp(this.currentTime + delay);

			// Put the telegram in the queue
			boolean added = queue.add(telegram);

			// Return it to the pool if has been rejected
			if (!added) pool.free(telegram);

			if (debugEnabled) {
				if (added)
					Gdx.app.log(LOG_TAG, "Delayed telegram from " + sender + " for " + receiver + " recorded at time "
						+ this.currentTime + ". Msg is " + msg);
				else
					Gdx.app.log(LOG_TAG, "Delayed telegram from " + sender + " for " + receiver + " rejected by the queue. Msg is "
						+ msg);
			}
		}
	}

	/** Dispatches any telegrams with a timestamp that has expired. Any dispatched telegrams are removed from the queue.
	 * <p>
	 * This method must be called each time through the main game loop.
	 * @param deltaTime the time span between the current frame and the last frame in seconds */
	public void update (float deltaTime) {
		currentTime += deltaTime;

		// Peek at the queue to see if any telegrams need dispatching.
		// Remove all telegrams from the front of the queue that have gone
		// past their time stamp.
		Telegram telegram;
		while ((telegram = queue.peek()) != null) {

			// Exit loop if the telegram is in the future
			if (telegram.getTimestamp() > currentTime) break;

			if (debugEnabled) {
				Gdx.app.log(LOG_TAG, "Queued telegram ready for dispatch: Sent to " + telegram.receiver + ". Msg is "
					+ telegram.message);
			}

			// Send the telegram to the recipient
			discharge(telegram);

			// Remove it from the queue
			queue.poll();
		}

	}

	/** Scans the queue and passes pending messages to the given callback in any particular order.
	 * <p>
	 * Typically this method is used to save (serialize) pending messages and restore (deserialize and schedule) them back on game
	 * loading.
	 * @param callback The callback used to report pending messages individually. **/
	public void scanQueue (PendingMessageCallback callback) {
		int queueSize = queue.size();
		for (int i = 0; i < queueSize; i++) {
			Telegram telegram = queue.get(i);
			callback.report(telegram.getTimestamp() - currentTime, telegram.sender, telegram.receiver, telegram.message,
				telegram.extraInfo);
		}
	}

	/** This method is used by {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage} for immediate
	 * telegrams and {@link #update(float) update} for delayed telegrams. It first calls the message handling method of the
	 * receiving agents with the specified telegram then returns the telegram to the pool.
	 * @param telegram the telegram to discharge */
	private void discharge (Telegram telegram) {
		if (telegram.receiver != null) {
			// Dispatch the telegram to the receiver specified by the telegram itself
			if (!telegram.receiver.handleMessage(telegram)) {
				// Telegram could not be handled
				if (debugEnabled) Gdx.app.log(LOG_TAG, "Message " + telegram.message + " not handled");
			}
		} else {
			// Dispatch the telegram to all the registered receivers
			int handledCount = 0;
			Array<Telegraph> listeners = msgListeners.get(telegram.message);
			if (listeners != null) {
				for (int i = 0; i < listeners.size; i++) {
					if (listeners.get(i).handleMessage(telegram)) {
						handledCount++;
					}
				}
			}
			// Telegram could not be handled
			if (debugEnabled && handledCount == 0) Gdx.app.log(LOG_TAG, "Message " + telegram.message + " not handled");
		}

		// Release the telegram to the pool
		pool.free(telegram);
	}

	/** A {@code PendingMessageCallback} is used by the {@link MessageDispatcher#scanQueue(PendingMessageCallback) scanQueue} method
	 * of the {@link MessageDispatcher} to report its pending messages individually.
	 * 
	 * @author davebaol */
	public interface PendingMessageCallback {

		/** Reports a pending message.
		 * @param delay The remaining delay in seconds
		 * @param sender The message sender
		 * @param receiver The message receiver
		 * @param message The message code
		 * @param extraInfo Any additional information that may accompany the message */
		public void report (float delay, Telegraph sender, Telegraph receiver, int message, Object extraInfo);
	}

}
