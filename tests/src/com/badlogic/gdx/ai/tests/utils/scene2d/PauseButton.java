
package com.badlogic.gdx.ai.tests.utils.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class PauseButton extends TextButton {

	private static final String PAUSE_AI = "Pause AI";
	private static final String RESUME_AI = "Resume AI";

	public PauseButton (Skin skin) {
		this(null, skin);
	}

	public PauseButton (Actor actor, Skin skin) {
		super(PAUSE_AI, skin.get(TextButtonStyle.class));
		init(actor);
	}

	public PauseButton (Skin skin, String styleName) {
		this(null, skin, styleName);
	}

	public PauseButton (Actor actor, Skin skin, String styleName) {
		super(PAUSE_AI, skin.get(styleName, TextButtonStyle.class));
		init(actor);
	}

	public PauseButton (TextButtonStyle style) {
		this(null, style);
	}

	public PauseButton (Actor actor, TextButtonStyle style) {
		super(PAUSE_AI, style);
		init(actor);
	}

	private final void init (final Actor visibleActor) {
		this.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				boolean pause = isChecked();
				setText(pause ? RESUME_AI : PAUSE_AI);
				if (visibleActor != null) visibleActor.setVisible(pause);
			}
		});
	}

}
