package battlecode.world.signal;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * An occurence or piece of information about a round during a match.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="signal")
@JsonSubTypes({
        @Type(value = AttackSignal.class, name="attack"),
        @Type(value = BroadcastSignal.class, name="broadcast"),
        @Type(value = BuildSignal.class, name="build"),
        @Type(value = BytecodesUsedSignal.class, name="bytecodesUsed"),
        @Type(value = ClearRubbleSignal.class, name="clearRubble"),
        @Type(value = ControlBitsSignal.class, name="controlBits"),
        @Type(value = DeathSignal.class, name="death"),
        @Type(value = HealthChangeSignal.class, name="healthChange"),
        @Type(value = IndicatorDotSignal.class, name="indicatorDot"),
        @Type(value = IndicatorLineSignal.class, name="indicatorLine"),
        @Type(value = IndicatorStringSignal.class, name="indicatorString"),
        @Type(value = InfectionSignal.class, name="infection"),
        @Type(value = MatchObservationSignal.class, name="matchObservation"),
        @Type(value = MovementOverrideSignal.class, name="movementOverride"),
        @Type(value = MovementSignal.class, name="movement"),
        @Type(value = PartsChangeSignal.class, name="partsChange"),
        @Type(value = RobotDelaySignal.class, name="robotDelay"),
        @Type(value = RubbleChangeSignal.class, name="rubbleChange"),
        @Type(value = SpawnSignal.class, name="spawn"),
        @Type(value = TeamResourceSignal.class, name="teamResource"),
        @Type(value = TypeChangeSignal.class, name="typeChange"),
})
public interface InternalSignal extends Serializable {}
