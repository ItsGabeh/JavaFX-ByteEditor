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
        int n = data.length;

        log.add("Iniciando cifrado...");

        // Derivación de constantes (se asume que la contraseña tiene al menos 4 dígitos)
        byte k1 = (byte)(password.charAt(0) - '0');
        byte k2 = (byte)(password.charAt(1) - '0');
        byte k3 = (byte)(password.charAt(2) - '0');
        byte k4 = (byte)(password.charAt(3) - '0');
        int rotateAmount = k3 % 8;  // cantidad de bits a rotar

        log.add(String.format("Contraseña procesada -> k1=%d, k2=%d, k3=%d, k4=%d", k1, k2, k3, k4));
        log.add(String.format("Calculando rotacion: %d mod 8 = %d", k3, rotateAmount));

        // Bucle combinado para pasos 1 a 7
        for (int i = 0; i < n; i++) {
            // Se trata el byte como valor sin signo (0 a 255)
            int val = data[i] & 0xFF;

            // Paso 1: Sumar k1 (operación modular 8 bits)
            val = (val + k1) & 0xFF;

            // Paso 2: XOR con k2
            val = val ^ k2;

            // Paso 3: Rotación a la izquierda
            val = leftRotate(val, rotateAmount);

            // Paso 4: Sumar el índice
            val = (val + i) & 0xFF;

            // Paso 5: Restar k4
            val = (val - k4) & 0xFF;

            // Paso 6: Ajuste según paridad del índice: pares +1, impares -1
            if (i % 2 == 0) {
                val = (val + 1) & 0xFF;
            } else {
                val = (val - 1) & 0xFF;
            }

            // Paso 7: XOR con el índice
            val = val ^ i;

            data[i] = (byte) val;
        }
        log.add("Transformando bytes a valores sin signo -> byte[i] & 0xFF = valor");
        log.add(String.format("Sumando k1 -> (valor + %d ) & 0xFF", k1));
        log.add(String.format("XOR con K2 -> valor ^ %d", k2));
        log.add(String.format("Rotacion a la izquierda por %d", rotateAmount));
        log.add("Sumando indice -> valor + i");
        log.add(String.format("Restando k4 -> (valor - %d) ^ OxFF", k4));
        log.add("Ajuste de paridad -> pares + 1, impares - 1");
        log.add("XOR con indice");

        // Paso 8: Revertir el array
        for (int i = 0, j = n - 1; i < j; i++, j--) {
            byte temp = data[i];
            data[i] = data[j];
            data[j] = temp;
        }
        log.add("Revirtiendo array...");

        // Paso 9: Rotación a la derecha
        for (int i = 0; i < n; i++) {
            int val = data[i] & 0xFF;
            val = rightRotate(val, rotateAmount);
            data[i] = (byte) val;
        }
        log.add(String.format("Rotando a la derecha por %d", rotateAmount));
        log.add("Terminado");
        return data;
    }

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
        int n = data.length;

        byte k1 = (byte)(password.charAt(0) - '0');
        byte k2 = (byte)(password.charAt(1) - '0');
        byte k3 = (byte)(password.charAt(2) - '0');
        byte k4 = (byte)(password.charAt(3) - '0');
        int rotateAmount = k3 % 8;

        log.add(String.format("Contraseña procesada -> k1=%d, k2=%d, k3=%d, k4=%d", k1, k2, k3, k4));
        log.add(String.format("Calculando rotacion: %d mod 8 = %d", k3, rotateAmount));

        // Paso inverso 9: Rotar a la izquierda (inverso de rotar a la derecha)
        for (int i = 0; i < n; i++) {
            int val = data[i] & 0xFF;
            val = leftRotate(val, rotateAmount);
            data[i] = (byte) val;
        }
        log.add(String.format("Rotando a la izquierda por %d", rotateAmount));

        // Paso inverso 8: Revertir el array (reversión es su propio inverso)
        for (int i = 0, j = n - 1; i < j; i++, j--) {
            byte temp = data[i];
            data[i] = data[j];
            data[j] = temp;
        }
        log.add("Revirtiendo array...");

        // Bucle combinado para invertir los pasos 7 a 1 (en orden inverso)
        for (int i = 0; i < n; i++) {
            int val = data[i] & 0xFF;

            // Inverso del paso 7: XOR con el índice
            val = val ^ i;

            // Inverso del paso 6: Ajuste de paridad (aquí, para índices pares se resta 1 y para impares se suma 1)
            if (i % 2 == 0) {
                val = (val - 1) & 0xFF;
            } else {
                val = (val + 1) & 0xFF;
            }

            // Inverso del paso 5: Sumar k4
            val = (val + k4) & 0xFF;

            // Inverso del paso 4: Restar el índice
            val = (val - i) & 0xFF;

            // Inverso del paso 3: Rotar a la derecha (inverso de la rotación a la izquierda)
            val = rightRotate(val, rotateAmount);

            // Inverso del paso 2: XOR con k2
            val = val ^ k2;

            // Inverso del paso 1: Restar k1
            val = (val - k1) & 0xFF;

            data[i] = (byte) val;
        }
        log.add("Transformando bytes a valores sin signo -> byte[i] & 0xFF = valor");
        log.add("XOR con indice");
        log.add("Ajuste de paridad -> pares - 1, impares + 1");
        log.add(String.format("Sumando k4 -> (valor + %d) ^ OxFF", k4));
        log.add("Restando indice -> valor - i");
        log.add(String.format("Rotacion a la derecha por %d", rotateAmount));
        log.add(String.format("XOR con K2 -> valor ^ %d", k2));
        log.add(String.format("Restandp k1 -> (valor - %d ) & 0xFF", k1));
        log.add("Terminado");

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

    public static String getLog() {
        StringBuilder builder = new StringBuilder();
        for (String s : log) {
            builder.append(s).append("\n");
        }
        return builder.toString();
    }

    public static void clearLog() {
        log.clear();
    }
}