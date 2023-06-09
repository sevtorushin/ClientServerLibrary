package entity;

import annotations.WITSPackageCode;
import annotations.WITSRecordCode;

import java.time.LocalDate;
import java.time.LocalTime;

@WITSPackageCode(code = "07")
public class WITSPackageDirectional extends WITSPackage {
    @WITSRecordCode(code = "05")
    private LocalDate witsDate;
    @WITSRecordCode(code = "06")
    private LocalTime witsTime;
    @WITSRecordCode(code = "08")
    private double surveyDepth;
    @WITSRecordCode(code = "11")
    private double depth;
    @WITSRecordCode(code = "16")
    private double mtf;
    @WITSRecordCode(code = "17")
    private double gtf;

    public WITSPackageDirectional(LocalDate witsDate, LocalTime witsTime, LocalDate witsDate1,
                                  LocalTime witsTime1, double surveyDepth, double depth, double mtf, double gtf) {
        super(witsDate, witsTime);
        this.witsDate = witsDate1;
        this.witsTime = witsTime1;
        this.surveyDepth = surveyDepth;
        this.depth = depth;
        this.mtf = mtf;
        this.gtf = gtf;
    }

    public WITSPackageDirectional() {
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

    public double getSurveyDepth() {
        return surveyDepth;
    }

    public void setSurveyDepth(double surveyDepth) {
        this.surveyDepth = surveyDepth;
    }

    public double getMtf() {
        return mtf;
    }

    public void setMtf(double mtf) {
        this.mtf = mtf;
    }

    public double getGtf() {
        return gtf;
    }

    public void setGtf(double gtf) {
        this.gtf = gtf;
    }

    @Override
    public String toString() {
        return "WITSPackageDirectional{" +
                "witsDate=" + witsDate +
                ", witsTime=" + witsTime +
                ", surveyDepth=" + surveyDepth +
                ", depth=" + depth +
                ", mtf=" + mtf +
                ", gtf=" + gtf +
                '}';
    }
}
