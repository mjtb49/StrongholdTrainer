package io.github.mjtb49.strongholdtrainer.ml.model;

import java.io.IOException;

public class STMetaSyntaxException extends IOException {

    public STMetaSyntaxException(int ln, String s) {
        super("L" + ln + ": " + s);
    }
}
