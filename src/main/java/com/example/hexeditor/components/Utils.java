package com.example.hexeditor.components;

import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {

    private static List<String> log = new ArrayList<>();

    public static byte[] encrypt(byte[] plainText, String password) {
        byte[] data = plainText.clone();
        byte k1 = (byte)(password.charAt(0) - '0');
        byte k2 = (byte)(password.charAt(1) - '0');
        byte k3 = (byte)(password.charAt(2) - '0');
        byte k4 = (byte)(password.charAt(3) - '0');
        int rotateAmount = k3 % 8;

        // Paso 1: Sumar k1
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] + k1);
        }
        // Paso 2: XOR con k2
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] ^ k2);
        }
        // Paso 3: Rotar a la izquierda
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) leftRotate(data[i], rotateAmount);
        }
        // Paso 4: Sumar el índice
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] + i);
        }
        // Paso 5: Restar k4
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] - k4);
        }
        // Paso 6: Ajuste según paridad del índice
        for (int i = 0; i < data.length; i++) {
            if (i % 2 == 0)
                data[i] = (byte)(data[i] + 1);
            else
                data[i] = (byte)(data[i] - 1);
        }
        // Paso 7: XOR con el índice
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] ^ i);
        }
        // Paso 8: Revertir el array
        for (int i = 0, j = data.length - 1; i < j; i++, j--) {
            byte temp = data[i];
            data[i] = data[j];
            data[j] = temp;
        }
        // Paso 9: Rotar a la derecha
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) rightRotate(data[i], rotateAmount);
        }
        return data;
    }

//    static int leftRotate(int x, int n) {
//        int mask = (1 << 8) - 1;
//        return mask & ((x << n) | (x >>> (8 - n)));
//    }
//
//    private static int rightRotate(int x, int n) {
//        int mask = (1 << 8) - 1;
//        return mask & ((x << (8 - n)) | (x >>> n));
//    }

    static int leftRotate(int x, int n) {
        int y = x & 0xFF; // Convertir a 8 bits sin signo
        return ((y << n) | (y >>> (8 - n))) & 0xFF;
    }

    private static int rightRotate(int x, int n) {
        int y = x & 0xFF; // Convertir a 8 bits sin signo
        return ((y >>> n) | (y << (8 - n))) & 0xFF;
    }

    public static byte[] decrypt(byte[] cipherText, String password) {
        byte[] data = cipherText.clone();
        byte k1 = (byte)(password.charAt(0) - '0');
        byte k2 = (byte)(password.charAt(1) - '0');
        byte k3 = (byte)(password.charAt(2) - '0');
        byte k4 = (byte)(password.charAt(3) - '0');
        int rotateAmount = k3 % 8;

        // Inverso del paso 9: Rotar a la izquierda
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) leftRotate(data[i], rotateAmount);
        }
        // Inverso del paso 8: Revertir el array
        for (int i = 0, j = data.length - 1; i < j; i++, j--) {
            byte temp = data[i];
            data[i] = data[j];
            data[j] = temp;
        }
        // Inverso del paso 7: XOR con el índice
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] ^ i);
        }
        // Inverso del paso 6: Ajuste según paridad (para pares restar 1, impares sumar 1)
        for (int i = 0; i < data.length; i++) {
            if (i % 2 == 0)
                data[i] = (byte)(data[i] - 1);
            else
                data[i] = (byte)(data[i] + 1);
        }
        // Inverso del paso 5: Sumar k4
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] + k4);
        }
        // Inverso del paso 4: Restar el índice
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] - i);
        }
        // Inverso del paso 3: Rotar a la derecha
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) rightRotate(data[i], rotateAmount);
        }
        // Inverso del paso 2: XOR con k2
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] ^ k2);
        }
        // Inverso del paso 1: Restar k1
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] - k1);
        }
        return data;
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