

package org.saga.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.saga.server.txevent.TxEvent;
import org.saga.server.txevent.TxEventEnvelopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.saga.common.EventType.TxStartedEvent;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AlphaEventController.class)
@ActiveProfiles("test")
public class AlphaEventControllerTest {
  private final TxEvent someEvent = someEvent();

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TxEventEnvelopeRepository eventRepository;

  @Before
  public void setUp() throws Exception {
    when(eventRepository.findAll()).thenReturn(singletonList(someEvent));
  }

  @Test
  public void retrievesEventsFromRepo() throws Exception {
    mockMvc.perform(get("/saga/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$.[0].globalTxId", is(someEvent.globalTxId())))
        .andExpect(jsonPath("$.[0].localTxId", is(someEvent.localTxId())));
  }

  private TxEvent someEvent() {
    return new TxEvent(
        uniquify("serviceName"),
        uniquify("instanceId"),
        uniquify("globalTxId"),
        uniquify("localTxId"),
        UUID.randomUUID().toString(),
        TxStartedEvent.name(),
        this.getClass().getCanonicalName(),
        uniquify("blah").getBytes());
  }
}
