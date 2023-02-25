package uk.gov.dwp.uc.pairtest.domain;

/** Immutable Object */
public final class TicketTypeRequest {

  private final Integer noOfTickets;
  private final Type type;

  public TicketTypeRequest(Type type, Integer noOfTickets) {
    this.type = type;
    this.noOfTickets = noOfTickets;
  }

  public Integer getNoOfTickets() {
    return noOfTickets;
  }

  public Type getTicketType() {
    return type;
  }

  public enum Type {
    ADULT,
    CHILD,
    INFANT
  }
}
