package com.nhb.test.encrypt;

import com.nhb.common.encrypt.aes.AESEncryptor;

public class TestAESEncryption {

	public static void main(String[] args) {
		AESEncryptor encryptor = AESEncryptor.newInstance();
		encryptor.setPassword("bachden");

		String message = "Hello world!!!";
		
		encryptor.encryptToBase64(message.getBytes());
	}

}
