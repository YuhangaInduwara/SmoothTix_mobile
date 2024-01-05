package com.smooth.smoothtix;

public class Booking {
    private String scheduleId;
    private String busProfileId;
    private String dateTime;
    private String routeNo;
    private String start;
    private String destination;

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public void setBusProfileId(String busProfileId) {
        this.busProfileId = busProfileId;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setRouteNo(String routeNo) {
        this.routeNo = routeNo;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getBusProfileId() {
        return busProfileId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getStart() {
        return start;
    }

    public String getDestination() {
        return destination;
    }

}
