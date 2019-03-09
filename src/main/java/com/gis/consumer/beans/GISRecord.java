package com.gis.consumer.beans;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

import java.io.Serializable;

@Table(keyspace = "gis", name = "gisrecord")
public class GISRecord implements Serializable {
    private String crimeid;
    private String month;
    private String reportedby;
    private String fallswithin;
    private String longitude;
    private String latitude;
    private String location;
    private String lsoacode;
    private String lsoaname;
    private String crimetype;
    private String outcomecategory;

    public String getCrimeid() {
        return crimeid;
    }

    public void setCrimeid(String crimeid) {
        this.crimeid = crimeid;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getReportedby() {
        return reportedby;
    }

    public void setReportedby(String reportedby) {
        this.reportedby = reportedby;
    }

    public String getFallswithin() {
        return fallswithin;
    }

    public void setFallswithin(String fallswithin) {
        this.fallswithin = fallswithin;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLsoacode() {
        return lsoacode;
    }

    public void setLsoacode(String lsoacode) {
        this.lsoacode = lsoacode;
    }

    public String getLsoaname() {
        return lsoaname;
    }

    public void setLsoaname(String lsoaname) {
        this.lsoaname = lsoaname;
    }

    public String getCrimetype() {
        return crimetype;
    }

    public void setCrimetype(String crimetype) {
        this.crimetype = crimetype;
    }

    public String getOutcomecategory() {
        return outcomecategory;
    }

    public void setOutcomecategory(String outcomecategory) {
        this.outcomecategory = outcomecategory;
    }
}
