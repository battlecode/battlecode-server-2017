package battlecode.server.http;

import battlecode.serial.notification.PauseNotification;
import battlecode.serial.notification.ResumeNotification;
import battlecode.serial.notification.RunNotification;
import battlecode.serial.notification.StartNotification;
import battlecode.server.controller.Controller;
import battlecode.server.controller.PassthroughController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Converts JSON trees to game commands and passes them to a Controller.
 * Could be connected to a websocket pipeline or a REST pipeline.
 *
 * API:
 * {
 *      "action": "start"
 *              | "run"
 *              | "pause"
 *              | "resume",
 * }
 * SimpleChannelInboundHandlers are ChannelHandlers that only accept
 * a particular type of object; in this case, JsonNodes.
 *
 * Created by james on 7/19/15.
 */
public final class CommandAdapter extends SimpleChannelInboundHandler<JsonNode> {
    private final PassthroughController sink;

    public CommandAdapter(final PassthroughController sink) {
        this.sink = sink;
    }

    /**
     * Called from a pipeline with an incoming JsonNode payload.
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final JsonNode msg) throws Exception {
        readNode(msg);
    }

    /**
     * Feed the adapter a JSON message.
     *
     * @param msg the JSON command payload
     * @throws InvalidCommandException
     */
    public void readNode(final JsonNode msg) throws InvalidCommandException {
        if (!msg.has("action")) {
            throw new InvalidCommandException(null);
        }
        final JsonNode action = msg.get("action");
        if (!action.isTextual()) {
            throw new InvalidCommandException(action.asText());
        }

        switch (action.textValue()) {
            case "start":
                sink.update(StartNotification.INSTANCE);
                break;
            case "run":
                sink.update(RunNotification.forever());
                break;
            case "pause":
                sink.update(PauseNotification.INSTANCE);
                break;
            case "resume":
                sink.update(ResumeNotification.INSTANCE);
                break;
            default:
                throw new InvalidCommandException(msg.textValue());
        }
    }

    public static final class InvalidCommandException extends Exception {
        public final String invalidCommand;
        public InvalidCommandException(final String command) {
            this.invalidCommand = command;
        }
        @Override
        public String getMessage() {
            return "Invalid command: " + invalidCommand;
        }
    }
}
