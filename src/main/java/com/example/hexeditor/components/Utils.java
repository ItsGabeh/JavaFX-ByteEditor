package com.example.hexeditor.components;

import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {

    private static List<String> log = new ArrayList<>();

    public static byte[] encrypt(byte[] plainText, String password) {
        byte[] result = plainText.clone();

        // Step 1:
        // Get first byte of password and add it to all bytes
        char firstByte = (char) (password.charAt(0) - '0');
        for (int i = 0; i < plainText.length; i++) {
            result[i] = (byte) ((result[i] + firstByte) % 127); // Suma con módulo 256
        }
        // Step 2:
        // Get last byte from password and add it to all bytes
        char lastByte = (char) (password.charAt(password.length() - 1) - '0');
        for (int i = 0; i < plainText.length; i++) {
            result[i] = (byte) ((result[i] + lastByte) % 127); // Suma con módulo 256
        }

        // Step 3
        // sum 1 to odd indexes
        for (int i = 1; i < plainText.length; i += 2) {
            result[i]++;
        }

        // Step 4
        // subtract 1 to even indexes
        for (int i = 0; i < plainText.length; i += 2) {
            result[i]--;
        }

        // Step 5
        // Get left middle index of password and divide it by 2
        // xor with each index
        byte leftMiddle = password.charAt(1) > '0' ? (byte) ((password.charAt(1) - '0') / 2) : 2;
        for (int i = 0; i < plainText.length; i++) {
            result[i] = (byte) (result[i] ^ leftMiddle);
        }

        // Step 6
        // left shift bytes by 3
        byte bits = 8;
        for (int i = 0; i < plainText.length; i++) {
            result[i] = (byte) leftRotate(result[i], 4);
        }

        // Step 7
        // divide password in 2
        byte left = (byte) (password.charAt(0) - '0'+ (password.charAt(1) - '0'));
        byte r = (byte) (password.charAt(3) - '0' + (password.charAt(2) - '0'));
        for (int i = 0; i < plainText.length; i++) {
            result[i] = (byte) (result[i] % 2 == 0 ? result[i] ^ left : result[i] ^ r);
        }


        // Step 8
        // get right middle character of password
        // AND its value with a mask
        byte bitmask = 15;
        byte rightMiddle = (byte) ((password.charAt(2) - '0') ^ bitmask);
        for (int i = 0; i < plainText.length; i++) {
            result[i] = (byte)(result[i] ^ rightMiddle);
        }

        // Step 9
        // reverse array
        int right = plainText.length - 1;
        for (int i = 0; i < right; i++, right--) {
            result[i] = (byte) (result[i] ^ result[right]);
            result[right] = (byte) (result[right] ^ result[i]);
            result[i] = (byte) (result[i] ^ result[right]);
        }

        return result;
    }

    static int leftRotate(int x, int n) {
        int mask = (1 << 7) - 1;
        return mask & ((x << n) | (x >>> (7 - n)));
    }

    private static int rigthRotate(int x, int n) {
        int mask = (1 << 7) - 1;
        return mask & ((x << (7 - n)) | (x >>> n));
    }

    public static byte[] decrypt(byte[] cipherText, String password) {
        // Trabajamos sobre una copia de cipherText para no modificar el original.
        byte[] result = cipherText.clone();

        // Step 1 revert reversing
        // reverse array
        int right = cipherText.length - 1;
        for (int i = 0; i < right; i++, right--) {
            result[i] = (byte) (result[i] ^ result[right]);
            result[right] = (byte) (result[right] ^ result[i]);
            result[i] = (byte) (result[i] ^ result[right]);
        }

        // Step 2
        // get right middle character of password
        // AND its value with a mask
        byte bitmask = 15;
        byte rightMiddle = (byte) ((password.charAt(2) - '0') ^ bitmask);
        for (int i = 0; i < cipherText.length; i++) {
            result[i] = (byte)(result[i] ^ rightMiddle);
        }


        // Step 3
        // divide password in 2
        byte left = (byte) (password.charAt(0) - '0' + (password.charAt(1) - '0'));
        byte r = (byte) (password.charAt(3) - '0' + (password.charAt(2) - '0'));
        for (int i = 0; i < result.length; i++) {
            if ((result[i] ^ left) % 2 == 0) {
                result[i] = (byte) (result[i] ^ left);
            } else {
                result[i] = (byte) (result[i] ^ r);
            }
        }

        // step 4
        // revert right shift by 3
        byte bits = 8;
        for (int i = 0; i < cipherText.length; i++) {
            result[i] = (byte) rigthRotate(result[i], 4);
            // result[i] = (byte) Integer.rotateLeft(result[i], bits);
        }

        // step 5
        // Get the left middle of password and multiply it by 2
        byte leftMiddle = password.charAt(1) > '0' ? (byte) ((password.charAt(1) - '0') / 2) : 2;
        for (int i = 0; i < cipherText.length; i++) {
            result[i] = (byte) (result[i] ^ leftMiddle);
        }

        // step 6
        // add 1 to even indexes
        for (int i = 0; i < cipherText.length; i += 2) {
            result[i]++;
        }

        // step 7
        // subtract 1 to odd indexes
        for(int i = 1; i < result.length; i += 2) {
            result[i]--;
        }

        // step 8
        // get last byte of password and subtract it
        byte lastByte = (byte) (password.charAt(password.length() - 1) - '0');
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) ((result[i] - lastByte + 127) % 127);
        }

        // step 9
        // get  first byte of password and subtract it
        byte firstByte = (byte) (password.charAt(0) - '0');
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) ((result[i] - firstByte + 127) % 127); // Resta con módulo 256
        }

        return result;
    }

    public static String hash(byte[] data) {
        // Step 1 acc inspired in FNV-1a
        long acc = 0x811C9DC5L;

        // Step 2 and 3 iterate bytes, xor and mult by a const
        for (byte b : data) {
            acc ^= (b & 0xFF);      // XOR no sign
            acc *= 0x01000193L;
        }

        // Step 4 sum array length
        acc += data.length;

        // NO SIGNS, get abs of acc
        acc = Math.abs(acc);

        // Step 5 acc to base 36 string
        String hashStr = Long.toString(acc, 36);

        // Step 6 fill the string with 10 chars
        if (hashStr.length() < 10) {
            hashStr = String.format("%10s", hashStr).replace(' ', '0'); // fill with zeros
        } else if (hashStr.length() > 10) {
            hashStr = hashStr.substring(0, 10); // Trunk
        }

        return hashStr;
    }


    public static void clearLog() {
        log.clear();
    }
}