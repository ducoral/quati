package io.quati.core;

public class Validation {

    final CommandInfo command;

    public Validation(CommandInfo command) {
        this.command = command;
    }

    public void validate(String[] args) {

        command.validate();
    }
}
