package com.malkos.poppin.bootstrap;

import java.util.concurrent.atomic.AtomicBoolean;

public class SchedulledTasksSynchronizer {
	private static volatile AtomicBoolean _isAnotherTaskRunning = new AtomicBoolean(false);
	private static volatile AtomicBoolean _environmentIsReady = new AtomicBoolean(false);
	private static volatile AtomicBoolean _isRemoteFileManagerConnectionBusy = new AtomicBoolean(false);
	
	public static boolean getIsAnotherTaskRunning() {
		return _isAnotherTaskRunning.get();
	}

	public static void setIsAnotherTaskRunning(boolean isAnotherTaskRunning) {
		_isAnotherTaskRunning.set(isAnotherTaskRunning);
	}

	public static boolean getEnvironmentIsReady() {
		return _environmentIsReady.get();
	}

	public static void setEnvironmentIsReady(boolean environmentIsReady) {
		_environmentIsReady.set(environmentIsReady);
	}
	public static boolean getIsRemoteFileManagerConnectionBusy() {
		return _isRemoteFileManagerConnectionBusy.get();
	}

	public static void setIsRemoteFileManagerConnectionBusy(boolean isRemoteFileManagerConnectionBusy) {
		_isRemoteFileManagerConnectionBusy.set(isRemoteFileManagerConnectionBusy);
	}

}
