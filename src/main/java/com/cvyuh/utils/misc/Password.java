package com.cvyuh.utils.misc;

import java.util.Random;

public class Password {
    private static final String CAPITAL_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String SPECIAL_CHARACTER_SET = "!@#$";
    private static final String NUMBERS = "1234567890";
    private static final String COMBINED_CHAR_SET =  CAPITAL_CASE_LETTERS
            + LOWER_CASE_LETTERS + SPECIAL_CHARACTER_SET + NUMBERS;

    /**
     *
     * @param length Length of the Password
     * @return Password String
     */
    public static String generate(int length) {
        Random random = new Random();
        char[] password = new char[length];

        password[0] = LOWER_CASE_LETTERS.charAt(random.nextInt(LOWER_CASE_LETTERS.length()));
        password[1] = CAPITAL_CASE_LETTERS.charAt(random.nextInt(CAPITAL_CASE_LETTERS.length()));
        password[2] = SPECIAL_CHARACTER_SET.charAt(random.nextInt(SPECIAL_CHARACTER_SET.length()));
        password[3] = NUMBERS.charAt(random.nextInt(NUMBERS.length()));

        for(int i = 4; i< length ; i++) {
            password[i] = COMBINED_CHAR_SET.charAt(random.nextInt(COMBINED_CHAR_SET.length()));
        }
        return String.valueOf(password);
    }
}
