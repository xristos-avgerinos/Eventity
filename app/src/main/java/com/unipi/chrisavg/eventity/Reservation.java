package com.unipi.chrisavg.eventity;

public class Reservation {

    private String EventId;
    private String UserId;
    private String TicketPersonFirstName;
    private String TicketPersonLastName;
    private String TicketQRCodeURL;
    private int Seat;

    public Reservation(String eventId, String userId, String ticketPersonFirstName, String ticketPersonLastName,String ticketQRCodeURL,int seat) {
        EventId = eventId;
        UserId = userId;
        TicketPersonFirstName = ticketPersonFirstName;
        TicketPersonLastName = ticketPersonLastName;
        TicketQRCodeURL = ticketQRCodeURL;
        Seat = seat;
    }

    public Reservation() {
    }

    public String getEventId() {
        return EventId;
    }

    public void setEventId(String eventId) {
        EventId = eventId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getTicketPersonFirstName() {
        return TicketPersonFirstName;
    }

    public void setTicketPersonFirstName(String ticketPersonFirstName) {
        TicketPersonFirstName = ticketPersonFirstName;
    }

    public String getTicketPersonLastName() {
        return TicketPersonLastName;
    }

    public void setTicketPersonLastName(String ticketPersonLastName) {
        TicketPersonLastName = ticketPersonLastName;
    }

    public String getTicketQRCodeURL() {
        return TicketQRCodeURL;
    }

    public void setTicketQRCodeURL(String ticketQRCodeURL) {
        TicketQRCodeURL = ticketQRCodeURL;
    }

    public int getSeat() {
        return Seat;
    }

    public void setSeat(int seat) {
        Seat = seat;
    }

}

