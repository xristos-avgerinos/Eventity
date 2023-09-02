package com.unipi.chrisavg.eventity;

public class Reservation {
    private String EventId;
    private String UserId;
    private String TicketPersonFirstName;
    private String TicketPersonLastName;

    public Reservation(String eventId, String userId, String ticketPersonFirstName, String ticketPersonLastName) {
        EventId = eventId;
        UserId = userId;
        TicketPersonFirstName = ticketPersonFirstName;
        TicketPersonLastName = ticketPersonLastName;
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
}
