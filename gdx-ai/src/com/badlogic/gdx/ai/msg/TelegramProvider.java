package com.badlogic.gdx.ai.msg;

/** @author avianey */
public interface TelegramProvider {
    /**
     * Provides {@link Telegram#extraInfo} to dispatch immediately when a {@link Telegraph} is registered for the given message type.
     * @param msg the message type to provide
     * @param msg the newly registered Telegraph. Providers can provide different info depending on the targeted Telegraph.
     * @return extra info to dispatch in a Telegram or null if nothing to dispatch
     * @see com.badlogic.gdx.ai.msg.MessageDispatcher#addListener(Telegraph, int)
     * @see com.badlogic.gdx.ai.msg.MessageDispatcher#addListeners(Telegraph, int...)
     */
    Object provideMessageInfo(int msg, Telegraph receiver);
}
