package com.malkos.poppin.encryption;

public class PGPEncryptionException extends Exception {
	private Exception pgpEncryptionException;
	public PGPEncryptionException(Exception e){
		super();
		pgpEncryptionException = e;
	}
	public Exception getPgpEncryptionException() {
		return pgpEncryptionException;
	}
}
