package com.utils.crea_exe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

final class CreateExecutables {

	private static final Logger LOGGER = createLogger();

	private CreateExecutables() {
	}

	private static Logger createLogger() {

		final Logger logger = Logger.getLogger(CreateExecutables.class.getName());
		logger.setUseParentHandlers(false);

		final Handler handlerMessages = new StreamHandler(System.out, new SimpleFormatter() {

			@Override
			public synchronized String format(
					final LogRecord record) {

				final String str;
				final String message = record.getMessage();
				if (message != null) {
					str = message + System.lineSeparator();
				} else {
					str = "";
				}
				return str;
			}
		}) {

			@Override
			public synchronized void publish(
					final LogRecord record) {

				super.publish(record);
				flush();
			}
		};
		logger.addHandler(handlerMessages);

		final Handler handlerExceptions = new StreamHandler(System.err, new SimpleFormatter() {

			@Override
			public synchronized String format(
					final LogRecord record) {

				final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				try (PrintStream printStream = new PrintStream(byteArrayOutputStream)) {

					final Throwable throwable = record.getThrown();
					if (throwable != null) {
						try {
							throwable.printStackTrace();
							throwable.printStackTrace(printStream);
						} catch (final Exception ignored) {
						}
					}
					return byteArrayOutputStream.toString();
				}
			}
		}) {

			@Override
			public synchronized void publish(
					final LogRecord record) {

				super.publish(record);
				flush();
			}
		};
		logger.addHandler(handlerExceptions);

		return logger;
	}

	public static void main(
			final String[] args) {

		final String osName = System.getProperty("os.name");
		final boolean windows = osName != null &&
				osName.toLowerCase(Locale.US).contains("windows");
		if (windows) {

			runCommand("gradlew.bat", "fatJar", "--console=plain");
			runCommand("gradlew.bat", "sourcesJar", "--console=plain");

		} else {
			runCommand("chmod", "+x", "gradlew");
			runCommand("./gradlew", "fatJar", "--console=plain");
			runCommand("./gradlew", "sourcesJar", "--console=plain");
		}

		final boolean wait = checkWait(args);
		if (wait) {

			LOGGER.info("press any key to continue");
			try {
				final int ch = System.in.read();
				LOGGER.info(String.valueOf((char) ch));

			} catch (final Exception exc) {
				LOGGER.log(Level.SEVERE, null, exc);
			}
		}
	}

	private static void runCommand(
			final String... command) {

		try {
			final Process process = new ProcessBuilder(command)
					.inheritIO()
					.start();
			process.waitFor();

		} catch (final Exception exc) {
			LOGGER.log(Level.SEVERE, null, exc);
		}
	}

	private static boolean checkWait(
			final String[] args) {

		boolean wait = true;
		if (args.length > 0) {

			final String firstArgument = args[0];
			if ("-no_wait".equals(firstArgument)) {
				wait = false;
			}
		}
		return wait;
	}
}
