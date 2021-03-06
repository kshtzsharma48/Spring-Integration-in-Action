package siia.booking.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import siia.booking.domain.cancellation.CancellationConfirmation;
import siia.booking.domain.cancellation.CancellationRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Marius Bogoevici
 */
@ContextConfiguration("classpath:cancellations.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class CancellationsTest {

    @Autowired @Qualifier("input")
    MessageChannel input;

    @Autowired @Qualifier("confirmed")
    PollableChannel confirmed;

    @Autowired @Qualifier("rejected")
    PollableChannel rejected;

    @Test
    public void testCancellations() {
        CancellationRequest cancellationRequest = new CancellationRequest();
        cancellationRequest.setReservationCode("GOLD123456");
        input.send(MessageBuilder.withPayload(cancellationRequest).build());
        Message<?> confirmedMessage = confirmed.receive(0);
        assertNotNull(confirmedMessage);
        assertEquals(CancellationConfirmation.class, confirmedMessage.getPayload().getClass());
        CancellationConfirmation confirmation = (CancellationConfirmation) confirmedMessage.getPayload();
        assertEquals("GOLD123456", confirmation.getReservationCode());
    }

    @Test
    public void testRejection() {
        CancellationRequest cancellationRequest = new CancellationRequest();
        cancellationRequest.setReservationCode("SILVER123456");
        input.send(MessageBuilder.withPayload(cancellationRequest).build());
        Message<?> confirmedMessage = confirmed.receive(0);
        assertNull(confirmedMessage);
        Message<?> rejectedMessage = rejected.receive(0);
        assertNotNull(rejectedMessage);
        assertEquals(CancellationRequest.class, rejectedMessage.getPayload().getClass());
        CancellationRequest request = (CancellationRequest) rejectedMessage.getPayload();
        assertEquals("SILVER123456", request.getReservationCode());
    }

}
