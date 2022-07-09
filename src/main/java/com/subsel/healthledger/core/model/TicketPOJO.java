package com.subsel.healthledger.core.model;

import lombok.Data;

@Data
public class TicketPOJO {
    private final String limit;
    private final String uname;
    private final String orgMsp;
    private final String key;
    private final String ticketId;
}
