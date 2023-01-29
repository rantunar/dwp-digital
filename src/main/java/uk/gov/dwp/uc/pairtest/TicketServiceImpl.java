package uk.gov.dwp.uc.pairtest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.enums.ErrorCodes;
import uk.gov.dwp.uc.pairtest.enums.TicketPrices;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.utils.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    @NonNull private final SeatReservationService seatReservationService;
    @NonNull private final TicketPaymentService ticketPaymentService;

    /**
     * This is the main method used to purchase ticket for an account
     * if there is any validation fails or the purchase data is null then throw exception
     * @param accountId
     * @param ticketTypeRequests
     * @throws InvalidPurchaseException
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if(ticketTypeRequests == null) throw new InvalidPurchaseException(ErrorCodes.ERROR05,String.format("Purchase data is null for Account id = [%s]",accountId));
        List<TicketTypeRequest> ticketTypeRequestList = Arrays.stream(ticketTypeRequests).collect(Collectors.toList());
        validatePurchaseRequest(accountId, ticketTypeRequestList);
        try {
            int totalAmountToPay = ticketTypeRequestList.stream().map(item -> TicketPrices.valueOf(item.getTicketType().name()).getPrice() * item.getNoOfTickets()).mapToInt(Integer::intValue).sum();
            int totalSeatsToAllocate = ticketTypeRequestList.stream().filter(e -> !e.getTicketType().equals(TicketTypeRequest.Type.INFANT)).mapToInt(TicketTypeRequest::getNoOfTickets).sum();
            ticketPaymentService.makePayment(accountId, totalAmountToPay);
            seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
        } catch (final Exception e) {
            throw new InvalidPurchaseException(ErrorCodes.ERROR01,e.getMessage());
        }
    }

    /**
     * validate the purchase request data, check account id and purchase information
     * @param accountId
     * @param ticketTypeRequests
     */
    private void validatePurchaseRequest(Long accountId, List<TicketTypeRequest> ticketTypeRequests) {
        if(accountId == null || accountId <= 0) throw new InvalidPurchaseException(ErrorCodes.ERROR02,String.format("Account id = [%s] is not a valid data",accountId));
        if(isMaxTicketCountExceeded(ticketTypeRequests)) throw new InvalidPurchaseException(ErrorCodes.ERROR03,String.format("Max ticket purchase count exceed the limit of = [%s]",Constants.MAX_NO_TICKET_ALLOWED));
        if(!isAdultTicketPresent(ticketTypeRequests)) throw new InvalidPurchaseException(ErrorCodes.ERROR04,String.format("No adult ticket is present for account id = [%s]",accountId));
    }

    /**
     * check the maximum ticket count defined in constant is less than or equal to the requested data
     * if not then throw exception
     * @param ticketTypeRequests
     * @return
     */
    private boolean isMaxTicketCountExceeded(List<TicketTypeRequest> ticketTypeRequests){
        int totalNoOfTickets = ticketTypeRequests.stream().mapToInt(TicketTypeRequest::getNoOfTickets).sum();
        return totalNoOfTickets > Constants.MAX_NO_TICKET_ALLOWED;
    }

    /**
     * check is at least one adult ticket has been purchased
     * or throw exception
     * @param ticketTypeRequests
     * @return
     */
    private boolean isAdultTicketPresent(List<TicketTypeRequest> ticketTypeRequests){
        return ticketTypeRequests.stream().anyMatch(e -> e.getTicketType().equals(TicketTypeRequest.Type.ADULT));
    }

}
