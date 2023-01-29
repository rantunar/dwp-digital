import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.enums.ErrorCodes;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {
    @InjectMocks private TicketServiceImpl ticketService;

    @Mock private SeatReservationService seatReservationService;
    @Mock private TicketPaymentService ticketPaymentService;

    @Test
    public void givenNullAccountId_whenPurchaseTicket_thenFailed(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(null, ticketTypeRequest));
        assertNotNull(invalidPurchaseException);
        assertEquals(ErrorCodes.ERROR02.name(), invalidPurchaseException.getErrorCode().name());
        assertEquals(ErrorCodes.ERROR02.getMsg(), invalidPurchaseException.getErrorCode().getMsg());
    }

    @Test
    public void givenZeroAccountId_whenPurchaseTicket_thenFailed(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(0L, ticketTypeRequest));
        assertNotNull(invalidPurchaseException);
        assertEquals(ErrorCodes.ERROR02.name(), invalidPurchaseException.getErrorCode().name());
        assertEquals(ErrorCodes.ERROR02.getMsg(), invalidPurchaseException.getErrorCode().getMsg());
    }

    @Test
    public void givenTotalTicketCountMoreThan20_whenPurchaseTicket_thenFailed(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, ticketTypeRequest));
        assertNotNull(invalidPurchaseException);
        assertEquals(ErrorCodes.ERROR03.name(), invalidPurchaseException.getErrorCode().name());
        assertEquals(ErrorCodes.ERROR03.getMsg(), invalidPurchaseException.getErrorCode().getMsg());
    }

    @Test
    public void givenNoAdultTicket_whenPurchaseTicket_thenFailed(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 20);
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, ticketTypeRequest));
        assertNotNull(invalidPurchaseException);
        assertEquals(ErrorCodes.ERROR04.name(), invalidPurchaseException.getErrorCode().name());
        assertEquals(ErrorCodes.ERROR04.getMsg(), invalidPurchaseException.getErrorCode().getMsg());
    }

    @Test
    public void givenPurchaseDataValid_whenPurchaseTicket_thenSucceed(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
        ticketService.purchaseTickets(1L, ticketTypeRequest);
        Mockito.verify(seatReservationService).reserveSeat(1L,20);
        Mockito.verify(ticketPaymentService).makePayment(1L,400);
    }

    @Test
    public void givenCombinedAdultChildInfant_whenPurchaseTicket_thenSucceed(){
        List<TicketTypeRequest> ticketTypeRequests = new ArrayList<>();
        ticketTypeRequests.add(new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5));
        ticketTypeRequests.add(new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10));
        ticketTypeRequests.add(new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5));
        ticketService.purchaseTickets(1L, ticketTypeRequests.toArray(new TicketTypeRequest[ticketTypeRequests.size()]));
        Mockito.verify(seatReservationService).reserveSeat(1L,15);
        Mockito.verify(ticketPaymentService).makePayment(1L,200);
    }

    @Test
    public void givenEmptyPurchaseData_whenPurchaseTicket_thenFailed(){
        List<TicketTypeRequest> ticketTypeRequests = new ArrayList<>();
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, ticketTypeRequests.toArray(new TicketTypeRequest[ticketTypeRequests.size()])));
        assertEquals(ErrorCodes.ERROR04.name(), invalidPurchaseException.getErrorCode().name());
        assertEquals(ErrorCodes.ERROR04.getMsg(), invalidPurchaseException.getErrorCode().getMsg());
    }

    @Test
    public void givenNullPurchaseData_whenPurchaseTicket_thenFailed(){
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, null));
        assertEquals(ErrorCodes.ERROR05.name(), invalidPurchaseException.getErrorCode().name());
        assertEquals(ErrorCodes.ERROR05.getMsg(), invalidPurchaseException.getErrorCode().getMsg());
    }

    @Test
    public void givenTicketPaymentServiceThrowError_whenPurchaseTicket_thenFailed(){
        doThrow(new NullPointerException("Payment Failed!!")).when(ticketPaymentService).makePayment(1L, 20);
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, ticketTypeRequest));
        assertEquals(ErrorCodes.ERROR01.name(), invalidPurchaseException.getErrorCode().name());
        assertEquals(ErrorCodes.ERROR01.getMsg(), invalidPurchaseException.getErrorCode().getMsg());
    }

    @Test
    public void givenSeatReservationServiceThrowError_whenPurchaseTicket_thenFailed(){
        doThrow(new NullPointerException("Seat Reservation Failed!!")).when(seatReservationService).reserveSeat(1L, 20);
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, ticketTypeRequest));
        assertEquals(ErrorCodes.ERROR01.name(), invalidPurchaseException.getErrorCode().name());
        assertEquals(ErrorCodes.ERROR01.getMsg(), invalidPurchaseException.getErrorCode().getMsg());
    }
}
