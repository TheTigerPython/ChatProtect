package de.thetigerpython.chatprotect.checks;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckResult {

    private boolean failed;
    private String info;
    private String message;

    public CheckResult() {
        this.failed = false;
        this.info = "-";
    }

    public CheckResult(boolean failed, String info) {
        this.failed = failed;
        this.info = info;
    }

    public CheckResult fail(String info, String message) {
        this.failed = true;
        this.info = info;
        this.message = message;

        return this;
    }

}