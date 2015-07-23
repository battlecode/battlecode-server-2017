package battlecode.server;

import battlecode.serial.notification.PauseNotification;
import battlecode.serial.notification.ResumeNotification;
import battlecode.serial.notification.RunNotification;
import battlecode.serial.notification.StartNotification;
import battlecode.server.controller.Controller;
import battlecode.server.controller.PassthroughController;
import battlecode.server.http.CommandAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static org.junit.Assert.assertEquals;

/**
 * Test our incoming JSON API.
 *
 * Created by james on 7/22/15.
 */
public class CommandAdapterTest {
    @Test
    public void testObservesCorrectObjects() throws Exception {
        final List<Object> observedObjects = new ArrayList<>();
        final PassthroughController sink = new PassthroughController();
        sink.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                observedObjects.add(arg);
            }
        });
        final CommandAdapter adapter = new CommandAdapter(sink);
        final ObjectMapper mapper = new ObjectMapper();
        adapter.readNode(mapper.readTree("{\"action\":\"start\"}"));
        adapter.readNode(mapper.readTree("{\"action\":\"run\"}"));
        adapter.readNode(mapper.readTree("{\"action\":\"pause\"}"));
        adapter.readNode(mapper.readTree("{\"action\":\"resume\"}"));
        assertEquals(observedObjects.get(0), StartNotification.INSTANCE);
        assertEquals(observedObjects.get(1), RunNotification.forever());
        assertEquals(observedObjects.get(2), PauseNotification.INSTANCE);
        assertEquals(observedObjects.get(3), ResumeNotification.INSTANCE);
    }
}
