package utils;

import java.nio.ByteBuffer;

public class ArrayUtils {

    public static byte[] arrayTrim(byte[] array) {
        int size = 0;
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] != 0) {
                size = i + 1;
                break;
            }
        }
        byte[] result = new byte[size];
        System.arraycopy(array, 0, result, 0, size);
        return result;
    }

    public static byte[] arrayTrim(ByteBuffer buffer) {
        int size = buffer.position();
        byte[] result = new byte[size];
        System.arraycopy(buffer.array(), 0, result, 0, size);
        return result;
    }

    public static boolean isEmpty(byte[] array){
        return arrayTrim(array).length == 0;
    }
}
