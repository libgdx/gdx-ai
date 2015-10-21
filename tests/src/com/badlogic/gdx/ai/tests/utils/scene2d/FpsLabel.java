package com.badlogic.gdx.ai.tests.utils.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.StringBuilder;

public class FpsLabel extends Label {
	
	int fps;
	int appendAt;

	public FpsLabel (CharSequence text, Skin skin) {
		this(text, skin.get(LabelStyle.class));
	}

	public FpsLabel (CharSequence text, Skin skin, String styleName) {
		this(text, skin.get(styleName, LabelStyle.class));
	}

	/** Creates a label, using a {@link LabelStyle} that has a BitmapFont with the specified name from the skin and the specified
	 * color. */
	public FpsLabel (CharSequence text, Skin skin, String fontName, Color color) {
		this(text, new LabelStyle(skin.getFont(fontName), color));
	}

	/** Creates a label, using a {@link LabelStyle} that has a BitmapFont with the specified name and the specified color from the
	 * skin. */
	public FpsLabel (CharSequence text, Skin skin, String fontName, String colorName) {
		this(text, new LabelStyle(skin.getFont(fontName), skin.getColor(colorName)));
	}

	public FpsLabel (CharSequence text, LabelStyle style) {
		super(text, style);
		this.fps= 0 ;
		this.appendAt = text.length();
	}
	
	@Override
	public void act(float delta) {
		// Update FPS label
		if (fps != Gdx.graphics.getFramesPerSecond()) {
			fps = Gdx.graphics.getFramesPerSecond();
			StringBuilder sb = getText();
			sb.setLength(appendAt);
			sb.append(fps);
			invalidateHierarchy();
		}
		super.act(delta);

	}

}
