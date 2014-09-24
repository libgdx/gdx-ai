package com.badlogic.gdx.ai.tests.msg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.TelegramProvider;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.tests.TelegramProviderTest;

/** @author avianey */
public class Citizen implements Telegraph, TelegramProvider {

    static int NUM = 0;

    final int num;
    final House house;

    public Citizen(House house) {
        this.num = NUM++;
        this.house = house;
        Gdx.app.log(Citizen.class.getSimpleName() + " " + num, "Hi there, I'm new in town and I live in house number " + house.num);
        MessageDispatcher.getInstance().addListener(this, TelegramProviderTest.MSG_EXISTING_CITIZEN);
        MessageDispatcher.getInstance().addProvider(this, TelegramProviderTest.MSG_EXISTING_CITIZEN);
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        Citizen citizen = (Citizen) msg.extraInfo;
        // greet only if not in the same house
        if (this.house.num != citizen.house.num) {
            Gdx.app.log(Citizen.class.getSimpleName() + " " + num, "Hi " + Citizen.class.getSimpleName() + " " + citizen.num + ", I'm your new neighbour");
        }
        return false;
    }

    @Override
    public Object provideMessageInfo(int msg, Telegraph receiver) {
        // when a new citizen come to town we tell him that we exists
        return this;
    }

}
