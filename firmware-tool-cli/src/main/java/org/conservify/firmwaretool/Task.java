package org.conservify.firmwaretool;

import org.apache.commons.cli.CommandLine;

public abstract class Task {
    abstract void run(CommandLine cmd);
}
