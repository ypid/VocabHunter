/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.controller;

import io.github.vocabhunter.analysis.marked.WordState;
import io.github.vocabhunter.gui.model.SessionModel;
import io.github.vocabhunter.gui.model.WordModel;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javafx.beans.binding.Bindings.notEqual;

public class WordStateHandler {
    private final Map<KeyCode, Runnable> keyHandlers = buildKeyHandlerMap();

    private final Button buttonUnseen;

    private final Button buttonKnown;

    private final Button buttonUnknown;

    private final SessionModel sessionModel;

    private final ObjectBinding<WordState> wordStateProperty;

    private final Runnable nextWordSelector;

    public WordStateHandler(final Button buttonUnseen, final Button buttonKnown, final Button buttonUnknown, final SessionModel sessionModel,
                            final ObjectBinding<WordState> wordStateProperty, final Runnable nextWordSelector) {
        this.buttonUnseen = buttonUnseen;
        this.buttonKnown = buttonKnown;
        this.buttonUnknown = buttonUnknown;
        this.sessionModel = sessionModel;
        this.wordStateProperty = wordStateProperty;
        this.nextWordSelector = nextWordSelector;
    }

    public void prepareEditButtons() {
        SimpleBooleanProperty editableProperty = sessionModel.editableProperty();
        BooleanBinding resettableProperty = editableProperty.and(notEqual(WordState.UNSEEN, wordStateProperty));

        buttonUnseen.visibleProperty().bind(resettableProperty);
        buttonKnown.visibleProperty().bind(editableProperty);
        buttonUnknown.visibleProperty().bind(editableProperty);

        buttonUnseen.setOnAction(e -> processResponse(WordState.UNSEEN, false));
        buttonKnown.setOnAction(e -> processResponse(WordState.KNOWN, true));
        buttonUnknown.setOnAction(e -> processResponse(WordState.UNKNOWN, true));
    }

    public void processKeyPress(final KeyEvent event) {
        KeyCode key = event.getCode();

        if (keyHandlers.containsKey(key)) {
            keyHandlers.get(key).run();
        }
    }

    private void processResponse(final WordState state, final boolean isSelectionChange) {
        WordModel word = sessionModel.getCurrentWord();

        if (state == WordState.UNKNOWN) {
            sessionModel.addSelectedWord(word);
        } else {
            sessionModel.removeDeselectedWord(word);
        }
        word.setState(state);
        if (isSelectionChange) {
            nextWordSelector.run();
        }
        sessionModel.setChangesSaved(false);
    }

    private Map<KeyCode, Runnable> buildKeyHandlerMap() {
        Map<KeyCode, Runnable> map = new HashMap<>();

        map.put(KeyCode.K, () -> processResponse(WordState.KNOWN, true));
        map.put(KeyCode.X, () -> processResponse(WordState.UNKNOWN, true));
        map.put(KeyCode.R, () -> processResponse(WordState.UNSEEN, false));

        return Collections.unmodifiableMap(map);
    }
}
