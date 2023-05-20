package org.pdks.security.entity;

import java.util.Random;

import com.Ostermiller.util.RandPass;

public final class DefaultPasswordGenerator {

	private static final String charUPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String charLOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String charNUMERIC = "0123456789";

	private static final String[] charCategories = new String[] { charLOWER + charUPPER + charNUMERIC };

	public static String generate(int length) {
		StringBuilder password = null;

		int sayac = 0;
		boolean devam = false;
		while (devam == false) {
			password = new StringBuilder(length);
			Random random = new Random(System.nanoTime());
			boolean buyuk = false, kucuk = false, numeric = false;
			for (int i = 0; i < length; i++) {
				String charCategory = charCategories[random.nextInt(charCategories.length)];
				int position = random.nextInt(charCategory.length());
				String str = String.valueOf(charCategory.charAt(position));
				if (charUPPER.contains(str))
					buyuk = true;
				else if (charLOWER.contains(str))
					kucuk = true;
				else if (charNUMERIC.contains(str)) {
					if (i == 0)
						break;
					numeric = true;
				}
				password.append(str);
			}
			++sayac;
			devam = buyuk && kucuk && numeric;
			if (sayac == 5 && devam == false) {
				RandPass sifreYaratici = new RandPass();
				String encodePassword = sifreYaratici.getPass(length);
				password = new StringBuilder(encodePassword);
				break;
			}
		}

		return new String(password);
	}

}
