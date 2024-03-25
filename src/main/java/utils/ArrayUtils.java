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

    /**
     * Converts the source buffer to a byte array, excluding null bytes at the end of the buffer.
     * The source buffer must have position=0 and a limit equal to the length of the resulting byte array
     * @param buffer current buffer.
     * @return Truncated byte array.
     */
    public static byte[] toArrayAndTrim(ByteBuffer buffer) {
        int size = buffer.limit();
        byte[] result = new byte[size];
        buffer.get(result);
        return result;
    }

    public static boolean isEmpty(byte[] array){
        return arrayTrim(array).length == 0;
    }
}
