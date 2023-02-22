package com.personal.flatten_dir;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

final class AppStartFlattenDir {

	private AppStartFlattenDir() {
	}

	public static void main(
			final String[] args) throws Exception {

		final Instant start = Instant.now();

		if (args.length == 0) {

			final String helpMessage = createHelpMessage();
			System.err.println("ERROR - insufficient arguments" + System.lineSeparator() + helpMessage);
			System.exit(-1);
		}

		if ("--help".equals(args[0])) {

			final String helpMessage = createHelpMessage();
			System.out.println(helpMessage);
			System.exit(0);
		}

		final String dirPathString = args[0];
		final Path dirPath = Paths.get(dirPathString).toAbsolutePath().normalize();

		System.out.println("directory path:");
		System.out.println(dirPath);

		if (!Files.isDirectory(dirPath)) {

			System.err.println("ERROR - directory does not exist");
			System.exit(-2);
		}

		Files.walkFileTree(dirPath, new SimpleFileVisitor<>() {

			@Override
			public FileVisitResult visitFile(
					final Path filePath,
					final BasicFileAttributes attrs) throws IOException {

				final Path dstFilePath = dirPath.resolve(filePath.getFileName());
				Files.copy(filePath, dstFilePath,
						StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);

				return super.visitFile(filePath, attrs);
			}
		});

		final Duration executionTime = Duration.between(start, Instant.now());
		System.out.println("done; execution time: " + durationToString(executionTime));
	}

	private static String createHelpMessage() {
		return "usage: flatten_dir DIR_PATH";
	}

	private static String durationToString(
			final Duration duration) {

		final StringBuilder stringBuilder = new StringBuilder();
		final long allSeconds = duration.get(ChronoUnit.SECONDS);
		final long hours = allSeconds / 3600;
		if (hours > 0) {
			stringBuilder.append(hours).append("h ");
		}

		final long minutes = (allSeconds - hours * 3600) / 60;
		if (minutes > 0) {
			stringBuilder.append(minutes).append("m ");
		}

		final long nanoseconds = duration.get(ChronoUnit.NANOS);
		final double seconds = allSeconds - hours * 3600 - minutes * 60 +
				nanoseconds / 1_000_000_000.0;
		stringBuilder.append(doubleToString(seconds)).append('s');

		return stringBuilder.toString();
	}

	private static String doubleToString(
			final double d) {

		final String str;
		if (Double.isNaN(d)) {
			str = "";

		} else {
			final String format;
			format = "0.000";
			final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
			final DecimalFormat decimalFormat = new DecimalFormat(format, decimalFormatSymbols);
			str = decimalFormat.format(d);
		}
		return str;
	}
}
