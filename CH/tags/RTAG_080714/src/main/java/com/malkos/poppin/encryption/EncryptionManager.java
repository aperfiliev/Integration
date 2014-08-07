package com.malkos.poppin.encryption;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.Security;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.malkos.poppin.bootstrap.GlobalProperties;
import com.malkos.poppin.bootstrap.GlobalPropertiesProvider;
import com.malkos.poppin.encryption.utils.PGPEncryptionUtil;

public class EncryptionManager implements IEncryptionManager{

	public String publicKeyPathHub;/* = "resources/encryptionKeys/commercehub.asc";*/
	private String secretKeyPath; /*= "resources/encryptionKeys/poppin_sandbox_sec.asc";*/
	private String secretPassword; /*= "poppin18";*/
	
	private static Logger logger = LoggerFactory.getLogger(EncryptionManager.class);
	
	private PGPPublicKey publicKey;
	
	static{
		logger.info("Adding BC provider.");
		Security.addProvider(new BouncyCastleProvider());
		logger.info(Security.getProvider("BC").toString());
	}
	public EncryptionManager(){
		GlobalProperties properties = GlobalPropertiesProvider.getGlobalProperties();
		
		this.publicKeyPathHub = properties.getStaplesPublicKeyPath();
		this.secretKeyPath = properties.getPoppinPrivateKeyPath();
		this.secretPassword = properties.getSecretPassword();
		FileInputStream publicKeyStream = null;
		try {
			publicKeyStream = new FileInputStream(new File(publicKeyPathHub));
			publicKey = PGPEncryptionUtil.readPublicKey(publicKeyStream);
		} catch (IOException | PGPException e) {
			e.printStackTrace();
		} finally{
			try {
				publicKeyStream.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public byte[] encrypt(InputStream source) {		
		byte[] encrypted = null;
		try {			
			byte[] bytesToEncrypt = IOUtils.toByteArray(source);
			encrypted = PGPEncryptionUtil.encrypt(bytesToEncrypt, publicKey, null, true, true);

		} catch (PGPException | IOException | NoSuchProviderException e) {
			e.printStackTrace();
		}		
		return encrypted;
	}
	public byte[] decrypt(InputStream source) throws NoSuchProviderException, IOException, PGPException {
		FileInputStream privateKeyStream = null;
		byte[] decrypted = null;
		privateKeyStream = new FileInputStream(new File(secretKeyPath));
		decrypted = PGPEncryptionUtil.decrypt(IOUtils.toByteArray(source), privateKeyStream, secretPassword.toCharArray());
		return decrypted;
	}
}
