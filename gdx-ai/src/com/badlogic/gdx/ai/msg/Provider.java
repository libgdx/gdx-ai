package com.badlogic.gdx.ai.msg;

/** @author avianey */
public interface Provider {
    /**
     * Provides {@link Telegram#extraInfo} to dispatch immediately when a {@link Telegraph} is registered for the given message type.
     * @param msg the message type to provide
     * @return extra info to dispatch in a Telegram or null if nothing to dispatch
     * @see com.badlogic.gdx.ai.msg.MessageDispatcher#addListener(Telegraph, int)
     * @see com.badlogic.gdx.ai.msg.MessageDispatcher#addListeners(Telegraph, int...)
     */
    Object provides(int msg);
}
