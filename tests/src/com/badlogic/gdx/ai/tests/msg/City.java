package com.badlogic.gdx.ai.tests.msg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.tests.TelegramProviderTest;

import java.util.ArrayList;
import java.util.List;

/** @author avianey */
public class City implements Telegraph {

    List<House> houses;

    public City() {
        Gdx.app.log(City.class.getSimpleName(), "A new city is born...");
        houses = new ArrayList<>();
        MessageDispatcher.getInstance().addListeners(this, TelegramProviderTest.MSG_TIME_TO_ACT);
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        // build a new house
        if (houses.size() <= 10) {
            houses.add(new House());
        }
        return false;
    }
}
