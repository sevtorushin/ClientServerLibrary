package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public abstract class WITSPackage implements Package, Serializable {
    private LocalDate witsDate;
    private LocalTime witsTime;

    public WITSPackage(LocalDate witsDate, LocalTime witsTime) {
        this.witsDate = witsDate;
        this.witsTime = witsTime;
    }

    public WITSPackage() {
    }
}
