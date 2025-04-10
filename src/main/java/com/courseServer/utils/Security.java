package com.courseServer.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Security {
	public static String hashPassword(String unHashed) {
		String hashed = BCrypt.withDefaults().hashToString(12, unHashed.toCharArray());
		return hashed;
	}
	
	public static boolean checkPassword(String loggedPassword, String realPassword) {
		return BCrypt.verifyer().verify(loggedPassword.getBytes(), realPassword.getBytes()).verified;
	}
}