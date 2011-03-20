package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Простейший перехватчик исключений для записи в лог
 */
public class UncaughtExceptionLogger implements UncaughtExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionLogger.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Uncaught exception in thread {}", t, e);
	}
}
