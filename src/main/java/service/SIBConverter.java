package service;

import entity.SIBParameter;
import entity.SIBParameterType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ArrayUtils;

public class SIBConverter implements Convertable<SIBParameter> {
    private static final Logger log = LogManager.getLogger(SIBConverter.class.getSimpleName());

    private String getName(int bytePerformance) {
        for (SIBParameterType type : SIBParameterType.values()) {
            if (type.getBytePerformance() == bytePerformance)
                return type.getName();
        }
        return "Unknown parameter";
    }

    private String toBinary(int n, int length) {
        StringBuilder binary = new StringBuilder();
        for (long i = (1L << length - 1); i > 0; i = i / 2) {
            binary.append((n & i) != 0 ? "1" : "0");
        }
        return binary.toString();
    }

    @Override
    public SIBParameter convert(byte[] bytes, Class<? extends SIBParameter> clazz) {
        if (bytes.length < 2)
            return null;
        SIBParameter parameter = null;
        try {
            parameter = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("SIBObject creation error", e);
            e.printStackTrace();
        }
        String bytes1;
        String bytes2;
        double data;
        String name = getName(bytes[1]);
        if (bytes[3] < 0) {
            bytes1 = toBinary((bytes[2] ^ 0xFF) & 0xFF, Byte.SIZE);
            bytes2 = toBinary((bytes[3] ^ 0xFF) & 0xFF, Byte.SIZE);
            data = -(Integer.parseInt(bytes2 + bytes1, 2) + 1);
        } else {
            bytes1 = toBinary(bytes[2] & 0xFF, Byte.SIZE);
            bytes2 = toBinary(bytes[3] & 0xFF, Byte.SIZE);
            data = Integer.parseInt(bytes2 + bytes1, 2);
        }
        if (name.equals(SIBParameterType.GAMMA.getName()) || name.equals(SIBParameterType.INCR.getName())
                || name.equals(SIBParameterType.INCD.getName()))
            data /= 10.0;

        parameter.setParameterName(name);
        parameter.setParameterData(data);
        parameter.setQuality(bytes[4]);

        return parameter;
    }

    private SIBParameter metaPackage(byte[] bytes) {
        byte[] data = ArrayUtils.arrayTrim(bytes);
        if (data.length == 1 && data[0] == -56)
            return new SIBParameter("Start Parameter", -999.25, 100);
        if (data.length == 1 && data[0] == 4) {
            return new SIBParameter("End Parameter", -999.25, 100);
        }
        return null;
    }
}
