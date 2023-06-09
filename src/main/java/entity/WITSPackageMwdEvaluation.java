package entity;

import annotations.WITSPackageCode;
import annotations.WITSRecordCode;

import java.time.LocalDate;
import java.time.LocalTime;

@WITSPackageCode(code = "08")
public class WITSPackageMwdEvaluation extends WITSPackage {
    @WITSRecordCode(code = "05")
    private LocalDate witsDate;
    @WITSRecordCode(code = "06")
    private LocalTime witsTime;
    @WITSRecordCode(code = "08")
    private double depth;
    @WITSRecordCode(code = "10")
    private double bitDepth;
    @WITSRecordCode(code = "21")
    private double gammaMeasuredDepth;
    @WITSRecordCode(code = "23")
    private double gammaRayRaw;
    @WITSRecordCode(code = "24")
    private double gammaRayCorrected;

    public WITSPackageMwdEvaluation(LocalDate witsDate, LocalTime witsTime, LocalDate witsDate1,
                                    LocalTime witsTime1, double depth, double bitDepth, double gammaMeasuredDepth,
                                    double gammaRayRaw, double gammaRayCorrected) {
        super(witsDate, witsTime);
        this.witsDate = witsDate1;
        this.witsTime = witsTime1;
        this.depth = depth;
        this.bitDepth = bitDepth;
        this.gammaMeasuredDepth = gammaMeasuredDepth;
        this.gammaRayRaw = gammaRayRaw;
        this.gammaRayCorrected = gammaRayCorrected;
    }

    public WITSPackageMwdEvaluation() {
    }

    public LocalDate getWitsDate() {
        return witsDate;
    }

    public void setWitsDate(LocalDate witsDate) {
        this.witsDate = witsDate;
    }

    public LocalTime getWitsTime() {
        return witsTime;
    }

    public void setWitsTime(LocalTime witsTime) {
        this.witsTime = witsTime;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(double bitDepth) {
        this.bitDepth = bitDepth;
    }

    public double getGammaMeasuredDepth() {
        return gammaMeasuredDepth;
    }

    public void setGammaMeasuredDepth(double gammaMeasuredDepth) {
        this.gammaMeasuredDepth = gammaMeasuredDepth;
    }

    public double getGammaRayRaw() {
        return gammaRayRaw;
    }

    public void setGammaRayRaw(double gammaRayRaw) {
        this.gammaRayRaw = gammaRayRaw;
    }

    public double getGammaRayCorrected() {
        return gammaRayCorrected;
    }

    public void setGammaRayCorrected(double gammaRayCorrected) {
        this.gammaRayCorrected = gammaRayCorrected;
    }

    @Override
    public String toString() {
        return "WITSPackageMwdEvaluation{" +
                "witsDate=" + witsDate +
                ", witsTime=" + witsTime +
                ", depth=" + depth +
                ", bitDepth=" + bitDepth +
                ", gammaMeasuredDepth=" + gammaMeasuredDepth +
                ", gammaRayRaw=" + gammaRayRaw +
                ", gammaRayCorrected=" + gammaRayCorrected +
                '}';
    }
}
