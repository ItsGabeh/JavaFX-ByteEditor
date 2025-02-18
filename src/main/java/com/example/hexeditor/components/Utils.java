package com.example.hexeditor.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {

    private static List<String> log = new ArrayList<>();

    public static String encrypt(byte[] data, String password) {
        if (password.length() != 4) {
            throw new IllegalArgumentException("La contraseña debe tener exactamente 4 caracteres.");
        }

        log.add("Inicio de encriptación con contraseña: " + password);

        int length = data.length;
        byte[] key = expandKey(password, length);
        byte sum = 0;

        // 1. XOR Inicial con clave expandida
        for (int i = 0; i < length; i++) {
            data[i] ^= key[i];
        }
        log.add("Paso 1: XOR con clave expandida -> " + byteArrayToString(data));

        // 2. Rotación de bits
        for (int i = 0; i < length; i++) {
            data[i] = rotateLeft(data[i], key[i] % 8);
        }
        log.add("Paso 2: Rotación de bits -> " + byteArrayToString(data));

        // 3. Suma acumulativa
        for (int i = 0; i < length; i++) {
            sum += data[i];
            data[i] += sum;
        }
        log.add("Paso 3: Suma acumulativa -> " + byteArrayToString(data));

        // 4. XOR con suma total
        byte totalSum = 0;
        for (byte b : data) {
            totalSum += b;
        }
        for (int i = 0; i < length; i++) {
            data[i] ^= totalSum;
        }
        log.add("Paso 4: XOR con suma total (" + totalSum + ") -> " + byteArrayToString(data));

        // 5. Sustitución (S-Box simple)
        byte[] sbox = generateSBox();
        for (int i = 0; i < length; i++) {
            data[i] = sbox[Byte.toUnsignedInt(data[i])];
        }
        log.add("Paso 5: Sustitución (S-Box) -> " + byteArrayToString(data));

        // 6. Intercambio de pares de bytes
        for (int i = 0; i < length - 1; i += 2) {
            byte temp = data[i];
            data[i] = data[i + 1];
            data[i + 1] = temp;
        }
        log.add("Paso 6: Intercambio de pares de bytes -> " + byteArrayToString(data));

        // 7. Desplazamiento circular
        if (password.hashCode() % 2 == 0) {
            shiftRight(data);
            log.add("Paso 7: Desplazamiento circular a la derecha -> " + byteArrayToString(data));
        } else {
            shiftLeft(data);
            log.add("Paso 7: Desplazamiento circular a la izquierda -> " + byteArrayToString(data));
        }

        // 8. XOR con clave invertida
        for (int i = 0; i < length; i++) {
            data[i] ^= key[length - 1 - i];
        }
        log.add("Paso 8: XOR con clave invertida -> " + byteArrayToString(data));

        // 9. Multiplicación modular
        for (int i = 0; i < length; i++) {
            data[i] = (byte) ((data[i] * 37) % 256);
        }
        log.add("Paso 9: Multiplicación modular -> " + byteArrayToString(data));

        // 10. Intercambio de mitades
        swapHalves(data);
        log.add("Paso 10: Intercambio de mitades -> " + byteArrayToString(data));

        // 11. XOR adicional con número derivado de la clave
        byte mask = (byte) (password.hashCode() & 0xFF);
        for (int i = 0; i < length; i++) {
            data[i] ^= mask;
        }
        log.add("Paso 11: XOR con máscara (" + mask + ") -> " + byteArrayToString(data));

        // 12. Permutación de bytes
        permuteBytes(data, key);
        log.add("Paso 12: Permutación de bytes -> " + byteArrayToString(data));

        // 13. XOR Final con el hash del array
        byte hash = (byte) Arrays.hashCode(data);
        for (int i = 0; i < length; i++) {
            data[i] ^= hash;
        }
        log.add("Paso 13: XOR final con hash (" + hash + ") -> " + byteArrayToString(data));

        log.add("Encriptación completada.");
        return Arrays.toString(data);
    }

    // Convierte el array de bytes en una cadena legible
    private static String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02X ", b)); // Representación en hexadecimal
        }
        return sb.toString().trim();
    }

    // Expande la clave para adaptarla al tamaño del array
    private static byte[] expandKey(String password, int length) {
        byte[] key = new byte[length];
        byte[] pwdBytes = password.getBytes();
        for (int i = 0; i < length; i++) {
            key[i] = pwdBytes[i % pwdBytes.length];
        }
        return key;
    }

    // Rotación de bits a la izquierda
    private static byte rotateLeft(byte value, int positions) {
        return (byte) ((value << positions) | (value >>> (8 - positions)));
    }

    // Generar una S-Box de sustitución simple
    private static byte[] generateSBox() {
        byte[] sbox = new byte[256];
        for (int i = 0; i < 256; i++) {
            sbox[i] = (byte) ((i * 7) % 256);
        }
        return sbox;
    }

    // Desplazamiento circular a la izquierda
    private static void shiftLeft(byte[] data) {
        if (data.length == 0) return;
        byte first = data[0];
        System.arraycopy(data, 1, data, 0, data.length - 1);
        data[data.length - 1] = first;
    }

    // Desplazamiento circular a la derecha
    private static void shiftRight(byte[] data) {
        if (data.length == 0) return;
        byte last = data[data.length - 1];
        System.arraycopy(data, 0, data, 1, data.length - 1);
        data[0] = last;
    }

    // Intercambio de mitades del array
    private static void swapHalves(byte[] data) {
        int mid = data.length / 2;
        for (int i = 0; i < mid; i++) {
            byte temp = data[i];
            data[i] = data[mid + i];
            data[mid + i] = temp;
        }
    }

    // Permutación basada en la clave
    private static void permuteBytes(byte[] data, byte[] key) {
        for (int i = 0; i < data.length; i++) {
            int j = key[i % key.length] % data.length;
            byte temp = data[i];
            data[i] = data[j];
            data[j] = temp;
        }
    }

    public static byte[] decrypt(byte[] data, String password, List<String> logList) {
        if (password.length() != 4) {
            throw new IllegalArgumentException("La contraseña debe tener exactamente 4 caracteres.");
        }

        log = logList; // Usar la misma lista de logs
        log.add("Inicio de desencriptación con contraseña: " + password);

        int length = data.length;
        byte[] key = expandKey(password, length);

        // Paso 13: Revertir XOR final con hash
        byte hash = (byte) Arrays.hashCode(data);
        for (int i = 0; i < length; i++) {
            data[i] ^= hash;
        }
        log.add("Paso 13: Revertir XOR con hash (" + hash + ") -> " + byteArrayToString(data));

        // Paso 12: Revertir Permutación de bytes
        reversePermuteBytes(data, key);
        log.add("Paso 12: Revertir Permutación de bytes -> " + byteArrayToString(data));

        // Paso 11: Revertir XOR con máscara
        byte mask = (byte) (password.hashCode() & 0xFF);
        for (int i = 0; i < length; i++) {
            data[i] ^= mask;
        }
        log.add("Paso 11: Revertir XOR con máscara (" + mask + ") -> " + byteArrayToString(data));

        // Paso 10: Revertir Intercambio de mitades
        swapHalves(data);
        log.add("Paso 10: Revertir Intercambio de mitades -> " + byteArrayToString(data));

        // Paso 9: Revertir Multiplicación modular
        for (int i = 0; i < length; i++) {
            data[i] = reverseModularMultiplication(data[i], 37);
        }
        log.add("Paso 9: Revertir Multiplicación modular -> " + byteArrayToString(data));

        // Paso 8: Revertir XOR con clave invertida
        for (int i = 0; i < length; i++) {
            data[i] ^= key[length - 1 - i];
        }
        log.add("Paso 8: Revertir XOR con clave invertida -> " + byteArrayToString(data));

        // Paso 7: Revertir Desplazamiento circular
        if (password.hashCode() % 2 == 0) {
            shiftLeft(data);
            log.add("Paso 7: Revertir Desplazamiento circular a la izquierda -> " + byteArrayToString(data));
        } else {
            shiftRight(data);
            log.add("Paso 7: Revertir Desplazamiento circular a la derecha -> " + byteArrayToString(data));
        }

        // Paso 6: Revertir Intercambio de pares de bytes
        for (int i = 0; i < length - 1; i += 2) {
            byte temp = data[i];
            data[i] = data[i + 1];
            data[i + 1] = temp;
        }
        log.add("Paso 6: Revertir Intercambio de pares de bytes -> " + byteArrayToString(data));

        // Paso 5: Revertir Sustitución (S-Box)
        byte[] inverseSBox = generateInverseSBox();
        for (int i = 0; i < length; i++) {
            data[i] = inverseSBox[Byte.toUnsignedInt(data[i])];
        }
        log.add("Paso 5: Revertir Sustitución (S-Box) -> " + byteArrayToString(data));

        // Paso 4: Revertir XOR con suma total
        byte totalSum = 0;
        for (byte b : data) {
            totalSum += b;
        }
        for (int i = 0; i < length; i++) {
            data[i] ^= totalSum;
        }
        log.add("Paso 4: Revertir XOR con suma total (" + totalSum + ") -> " + byteArrayToString(data));

        // Paso 3: Revertir Suma acumulativa
        byte sum = 0;
        for (int i = length - 1; i >= 0; i--) {
            data[i] -= sum;
            sum += data[i];
        }
        log.add("Paso 3: Revertir Suma acumulativa -> " + byteArrayToString(data));

        // Paso 2: Revertir Rotación de bits
        for (int i = 0; i < length; i++) {
            data[i] = rotateRight(data[i], key[i] % 8);
        }
        log.add("Paso 2: Revertir Rotación de bits -> " + byteArrayToString(data));

        // Paso 1: Revertir XOR con clave expandida
        for (int i = 0; i < length; i++) {
            data[i] ^= key[i];
        }
        log.add("Paso 1: Revertir XOR con clave expandida -> " + byteArrayToString(data));

        log.add("Desencriptación completada.");
        return data;
    }

    private static void reversePermuteBytes(byte[] data, byte[] key) {
        for (int i = data.length - 1; i > 0; i--) {
            int swapIndex = (key[i] & 0xFF) % data.length;
            byte temp = data[i];
            data[i] = data[swapIndex];
            data[swapIndex] = temp;
        }
    }

    private static byte reverseModularMultiplication(byte value, int factor) {
        for (int i = 0; i < 256; i++) {
            if ((i * factor) % 256 == (value & 0xFF)) {
                return (byte) i;
            }
        }
        return 0;
    }

    private static byte rotateRight(byte b, int shifts) {
        return (byte) ((b & 0xFF) >>> shifts | (b & 0xFF) << (8 - shifts));
    }

    private static byte[] generateInverseSBox() {
        byte[] sBox = generateSBox();
        byte[] inverseSBox = new byte[256];
        for (int i = 0; i < 256; i++) {
            inverseSBox[sBox[i] & 0xFF] = (byte) i;
        }
        return inverseSBox;
    }

    public String hash(byte[] plainText) {
        // TODO hash
        return null;
    }

    public static List<String> getLog() {
        return log;
    }
}
