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
            result[i] = result[i] <= 117 ? (byte) (firstByte + result[i]) : result[i];
        }

        // Step 2:
        // Get last byte from password and add it to all bytes
        char lastByte = (char) (password.charAt(password.length() - 1) - '0');
        for (int i = 0; i < plainText.length; i++) {
            result[i] = result[i] <= 117 ? (byte) (lastByte + result[i]) : result[i];
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
//
//        // Step 6
//        // left shift bytes by 3
        byte bits = 8;
        for (int i = 0; i < plainText.length; i++) {
            result[i] = (byte) leftRotate(result[i], 4);
        }

        // Step 7
        // divide password in 2
        byte left = (byte) (firstByte + (password.charAt(1) - '0'));
        byte r = (byte) (lastByte + (password.charAt(2) - '0'));
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
            result[i] = (byte) (result[i] % 2 == 0 ? result[i] ^ left : result[i] ^ r);
        }

//        // step 4
//        // revert right shift by 3
        byte bits = 8;
        for (int i = 0; i < cipherText.length; i++) {
            result[i] = (byte) rigthRotate(result[i], 4);
            // result[i] = (byte) Integer.rotateLeft(result[i], bits);
        }
//
//        // step 5
//        // Get the left middle of password and multiply it by 2
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
            result[i] = result[i] <= 117 ? (byte) (lastByte + result[i]) : result[i];
        }

        // step 9
        // get  first byte of password and subtract it
        byte firstByte = (byte) (password.charAt(0) - '0');
        for (int i = 0; i < result.length; i++) {
                result[i] = result[i] <= 117 ? (byte) (firstByte + result[i]) : result[i];
        }

        return result;
    }




    public static void clearLog() {
        log.clear();
    }
}