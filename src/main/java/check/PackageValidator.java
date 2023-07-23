package check;

import entity.*;
import entity.Package;
import service.Convertable;
import service.SIBConverter;
import service.WITSConverter;

import java.util.IllegalFormatConversionException;

public class PackageValidator {
    private static SIBConverter sibConverter;
    private static WITSConverter witsConverter;

    public static Class<? extends Package> identify(byte[] data) {
        if (data[0] == -56 || data[0] == 4)
            return SIBParameter.class;
        else if (data[0] == 38 && data[1] == 38 && data[4] == 48 && data[5] == 49)
            return WITSPackageTimeBased.class;
        else if (data[0] == 38 && data[1] == 38 && data[4] == 48 && data[5] == 55)
            return WITSPackageDirectional.class;
        else if (data[0] == 38 && data[1] == 38 && data[4] == 48 && data[5] == 56)
            return WITSPackageMwdEvaluation.class;
        else throw new IllegalFormatConversionException(' ', Object.class);
    }

    public static Convertable<? extends Package> getPackageConverter(Class<? extends Package> clazz) {
        if (clazz.getSimpleName().equals(SIBParameter.class.getSimpleName())) {
            if (sibConverter == null) {
                sibConverter = new SIBConverter();
                return sibConverter;
            } else return sibConverter;
        } else if (clazz.getSuperclass().getSimpleName().equals(WITSPackage.class.getSimpleName())){
            if (witsConverter == null) {
                witsConverter = new WITSConverter();
                return witsConverter;
            } else return witsConverter;
        }
        else throw new IllegalArgumentException("Illegal specified argument: " + clazz.getSimpleName());
    }
}