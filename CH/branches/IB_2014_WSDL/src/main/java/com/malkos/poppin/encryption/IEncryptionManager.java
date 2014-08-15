package com.malkos.poppin.encryption;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;

import org.bouncycastle.openpgp.PGPException;

public interface IEncryptionManager {
	byte[] encrypt(InputStream source);
	byte[] decrypt(InputStream source) throws FileNotFoundException, NoSuchProviderException, IOException, PGPException;
}
