
package com.badlogic.gdx.ai.tests.utils.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** A text button to pause/resume tests. The cover actor (if any) is made visible when the button is checked, invisible otherwise.
 * 
 * @author davebaol */
public class PauseButton extends TextButton {

	private static final String PAUSE_AI = "Pause AI";
	private static final String RESUME_AI = "Resume AI";

	public PauseButton (Skin skin) {
		this(null, skin);
	}

	public PauseButton (Actor cover, Skin skin) {
		super(PAUSE_AI, skin.get(TextButtonStyle.class));
		initialize(cover);
	}

	public PauseButton (Skin skin, String styleName) {
		this(null, skin, styleName);
	}

	public PauseButton (Actor cover, Skin skin, String styleName) {
		super(PAUSE_AI, skin.get(styleName, TextButtonStyle.class));
		initialize(cover);
	}

	public PauseButton (TextButtonStyle style) {
		this(null, style);
	}

	public PauseButton (Actor cover, TextButtonStyle style) {
		super(PAUSE_AI, style);
		initialize(cover);
	}

	private void initialize (final Actor cover) {
		this.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				boolean pause = isChecked();
				setText(pause ? RESUME_AI : PAUSE_AI);
				if (cover != null) cover.setVisible(pause);
			}
		});
	}

}
