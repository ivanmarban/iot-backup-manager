package com.github.ivanmarban;

import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "iot-backup-manager", description = "...", mixinStandardHelpOptions = true)
public class App implements Runnable {

	@Option(names = { "-v", "--verbose" }, description = "...")
	boolean verbose;

	public static void main(String[] args) throws Exception {
		PicocliRunner.run(App.class, args);
	}

	public void run() {
		// business logic here
	}

}
