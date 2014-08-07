package com.malkos.poppin.bootstrap;

import java.util.concurrent.atomic.AtomicBoolean;

public class Synchronizer {
	private static volatile AtomicBoolean _isAnotherTaskRunning = new AtomicBoolean(false);	
	private static volatile AtomicBoolean _isRemoteFileManagerConnectionBusy = new AtomicBoolean(false);
	
	public static boolean getIsAnotherTaskRunning() {
		return _isAnotherTaskRunning.get();
	}

	public static void setIsAnotherTaskRunning(boolean isAnotherTaskRunning) {
		_isAnotherTaskRunning.set(isAnotherTaskRunning);
	}	
	public static boolean getIsRemoteFileManagerConnectionBusy() {
		return _isRemoteFileManagerConnectionBusy.get();
	}

	public static void setIsRemoteFileManagerConnectionBusy(boolean isRemoteFileManagerConnectionBusy) {
		_isRemoteFileManagerConnectionBusy.set(isRemoteFileManagerConnectionBusy);
	}
}
