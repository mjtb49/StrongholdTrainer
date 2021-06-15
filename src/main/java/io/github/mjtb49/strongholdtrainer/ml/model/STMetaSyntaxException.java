package io.github.mjtb49.strongholdtrainer.ml.model;

import java.io.IOException;

public class STMetaSyntaxException extends IOException {

    public STMetaSyntaxException(int ln, String s) {
        super("\u001b[31m STMETA ERROR at line " + ln + ": " + s + "\u001b[0m");
    }
}
