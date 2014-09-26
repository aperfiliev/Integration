package com.malkos.poppin.encryption.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PGPEncryptionUtil {
	
	private static Logger logger = LoggerFactory.getLogger(PGPEncryptionUtil.class);
	
	@SuppressWarnings("deprecation")
	private static PGPPrivateKey findSecretKey(
            PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
            throws PGPException, NoSuchProviderException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
        
        if (pgpSecKey == null) {
        	logger.warn("The secret key stream does not contain any secret key.");
            return null;
        }
        return pgpSecKey.extractPrivateKey(pass, "BC");
    }
	public static byte[] encrypt(byte[] clearData, PGPPublicKey encKey,
            String fileName,boolean withIntegrityCheck, boolean armor)
            throws IOException, PGPException, NoSuchProviderException {
		if (fileName == null) {
            fileName = PGPLiteralData.CONSOLE;
        }
		//comress
        byte[] compressedData = compress(clearData, fileName, CompressionAlgorithmTags.ZIP);
		
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		OutputStream out = bOut;
		//wrap with armor
        if (armor)
        {
            out = new ArmoredOutputStream(out);
        }
        //get public encryotion key
        BcPGPDataEncryptorBuilder builder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.CAST5);
        builder.setSecureRandom(new SecureRandom());
        builder.setWithIntegrityPacket(withIntegrityCheck);
        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(builder);
        encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(encKey));


        OutputStream encryotedOut = encryptedDataGenerator.open(out, compressedData.length);
        
        encryotedOut.write(compressedData);
        encryotedOut.close();

        if (armor)
        {
            out.close();
        }

        return bOut.toByteArray();
    }
	@SuppressWarnings("resource")
	public static byte[] decrypt(byte[] encrypted, InputStream keyIn, char[] password)
            throws IOException, PGPException, NoSuchProviderException {
		
        InputStream in = new ByteArrayInputStream(encrypted);

        in = PGPUtil.getDecoderStream(in);

        PGPObjectFactory objectFactory = new PGPObjectFactory(in);
        PGPEncryptedDataList enc = null;
        
        Object o = objectFactory.nextObject();

        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) objectFactory.nextObject();
        }
        //
        // find the secret key
        //
        Iterator<?> it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData publicKeyEncrDataObject = null;
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(keyIn));
        logger.info("Getting the secret key from secret key Stream.");
        while (sKey == null && it.hasNext()) {
            publicKeyEncrDataObject = (PGPPublicKeyEncryptedData) it.next();

            sKey = findSecretKey(pgpSec, publicKeyEncrDataObject.getKeyID(), password);
        }

        if (sKey == null) {
            throw new IllegalArgumentException(
                    "secret key for message not found.");
        }
        logger.info("Decrypting the stream with secret key.");
        InputStream clear = null;
        //try{
        clear = publicKeyEncrDataObject.getDataStream(sKey, "BC");
        //}
        //catch(PGPException | NoSuchProviderException ex){
        //	logger.warn("Failed to decrypt the stream with BC provider." + ex.getMessage());
        //}
        logger.info("Decrypting is finished.");
        PGPObjectFactory pgpFact = new PGPObjectFactory(clear);

        PGPCompressedData compressedData = (PGPCompressedData) pgpFact.nextObject();

        pgpFact = new PGPObjectFactory(compressedData.getDataStream());

        PGPLiteralData literalData = (PGPLiteralData) pgpFact.nextObject();

        InputStream unc = literalData.getInputStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch;
        logger.info("Starting read decrypted data.");
        while ((ch = unc.read()) >= 0) {
            out.write(ch);

        }

        byte[] returnBytes = out.toByteArray();
        out.close();
        return returnBytes;
    }
	public static PGPPublicKey readPublicKey(InputStream in)
            throws IOException, PGPException {
        in = PGPUtil.getDecoderStream(in);

        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);
        
        Iterator<?> rIt = pgpPub.getKeyRings();
        
        while (rIt.hasNext()) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing)rIt.next();
            Iterator<?> kIt = kRing.getPublicKeys();

            while (kIt.hasNext()) {
                PGPPublicKey k = (PGPPublicKey) kIt.next();
                if (k.isEncryptionKey()) {
                    return k;
                }
            }
        }

        throw new IllegalArgumentException(
                "Can't find encryption key in key ring.");
    }
	private static byte[] compress(byte[] clearData, String fileName, int algorithm) throws IOException
    {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        PGPCompressedDataGenerator compressedData = new PGPCompressedDataGenerator(algorithm);
        OutputStream cos = compressedData.open(bytesOut); // open it with the final destination

        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();

        // we want to generate compressed data. This might be a user option later,
        // in which case we would pass in bOut.
        OutputStream  packetsOut = lData.open(cos, // the compressed output stream
                                        PGPLiteralData.BINARY,
                                        fileName,  // "filename" to store
                                        clearData.length, // length of clear data
                                        new Date()  // current time
                                      );

        packetsOut.write(clearData);
        packetsOut.close();

        compressedData.close();

        return bytesOut.toByteArray();
    }

}
