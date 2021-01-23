

package org.saga.server;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@Controller
@RequestMapping("/saga")
@Profile("test")
// Only export this Controller for test
class AlphaEventController {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TxEventEnvelopeRepository eventRepository;

  AlphaEventController(TxEventEnvelopeRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @GetMapping(value = "/events")
  public ResponseEntity<Collection<TxEventVo>> events() {
    LOG.info("Get the events request");
    Iterable<TxEvent> events = eventRepository.findAll();

    List<TxEventVo> eventVos = new LinkedList<>();
    events.forEach(event -> eventVos.add(new TxEventVo(event)));
    LOG.info("Get the event size " + eventVos.size());

    return ResponseEntity.ok(eventVos);
  }

  @DeleteMapping("/events")
  public ResponseEntity<String> clear() {
    eventRepository.deleteAll();
    return ResponseEntity.ok("All events deleted");
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  private static class TxEventVo extends TxEvent {
    private TxEventVo(TxEvent event) {
      super(event);
    }
  }
}
