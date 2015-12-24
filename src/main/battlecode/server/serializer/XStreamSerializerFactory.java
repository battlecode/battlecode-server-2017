package battlecode.server.serializer;

import battlecode.common.MapLocation;
import battlecode.world.DominationFactor;
import battlecode.world.GameMap;
import battlecode.world.ZombieSpawnSchedule;
import battlecode.world.signal.Signal;
import battlecode.serial.ExtensibleMetadata;
import battlecode.serial.RoundDelta;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.*;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * Serialize things to XML, with XStream.
 *
 * Created by james on 7/24/15.
 */
public class XStreamSerializerFactory implements SerializerFactory {
    static private XStream xstream;

    public static class IntArrayConverter implements SingleValueConverter {

        public boolean canConvert(Class cls) {
            return cls.equals(int[].class);
        }

        public String toString(Object obj) {
            return StringUtils.join(ArrayUtils.toObject((int[]) obj), ",");
        }

        public Object fromString(String name) {
            // java.lang.String.split doesn't do what we want when
            // the string is empty
            String[] strings = StringUtils.split(name, ",");
            int[] obj = new int[strings.length];
            int i;
            try {
                for (i = 0; i < strings.length; i++)
                    obj[i] = Integer.parseInt(strings[i]);
            } catch (NumberFormatException e) {
                throw new ConversionException("Invalid int []", e);
            }
            return obj;
        }
    }

    public static class LongArrayConverter implements SingleValueConverter {

        public boolean canConvert(Class cls) {
            return cls.equals(long[].class);
        }

        public String toString(Object obj) {
            return StringUtils.join(ArrayUtils.toObject((long[]) obj), ",");
        }

        public Object fromString(String name) {
            String[] strings = StringUtils.split(name, ",");
            long[] obj = new long[strings.length];
            int i;
            try {
                for (i = 0; i < strings.length; i++)
                    obj[i] = Long.parseLong(strings[i]);
            } catch (NumberFormatException e) {
                throw new ConversionException("Invalid long []", e);
            }
            return obj;
        }

    }

    public static class DoubleArrayConverter implements SingleValueConverter {

        public boolean canConvert(Class cls) {
            return cls.equals(double[].class);
        }

        public String toString(Object obj) {
            return StringUtils.join(ArrayUtils.toObject((double[]) obj), ",");
        }

        public Object fromString(String name) {
            String[] strings = StringUtils.split(name, ",");
            double[] obj = new double[strings.length];
            int i;
            try {
                for (i = 0; i < strings.length; i++)
                    obj[i] = Double.parseDouble(strings[i]);
            } catch (NumberFormatException e) {
                throw new ConversionException("Invalid double []", e);
            }
            return obj;
        }
    }

    public static class MapLocationConverter implements SingleValueConverter {

        public boolean canConvert(Class cls) {
            return cls.equals(MapLocation.class);
        }

        public String toString(Object obj) {
            MapLocation loc = (MapLocation) obj;
            return String.format("%s,%s", loc.x, loc.y);
        }

        public Object fromString(String name) {
            String[] coords = StringUtils.split(name, ",");
            if (coords.length != 2)
                throw new ConversionException("Invalid MapLocation");
            try {
                return new MapLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
            } catch (NumberFormatException e) {
                throw new ConversionException("Invalid MapLocation", e);
            }
        }

    }

    public static class RoundDeltaConverter implements Converter {

        public boolean canConvert(Class cls) {
            return cls.equals(RoundDelta.class);
        }

        public void marshal(Object value, HierarchicalStreamWriter writer,
                            MarshallingContext context) {
            context.convertAnother(((RoundDelta) value).getSignals());
        }

        public Object unmarshal(HierarchicalStreamReader reader,
                                UnmarshallingContext context) {
            RoundDelta rd = new RoundDelta();
            rd.setSignals((Signal[]) context.convertAnother(rd, Signal[].class));
            return rd;
        }

    }

    public static class ExtensibleMetadataConverter implements Converter {

        public boolean canConvert(Class cls) {
            return cls.equals(ExtensibleMetadata.class);
        }

        public void marshal(Object value, HierarchicalStreamWriter writer,
                            MarshallingContext context) {
            ExtensibleMetadata metadata = (ExtensibleMetadata) value;
            for (String s : metadata.keySet()) {
                Object o = metadata.get(s, null);
                if (s.equals("maps"))
                    writer.addAttribute(s, StringUtils.join((String[]) o, ","));
                else
                    writer.addAttribute(s, o.toString());
            }
        }

        public Object unmarshal(HierarchicalStreamReader reader,
                                UnmarshallingContext context) {
            ExtensibleMetadata metadata = new ExtensibleMetadata();
            int i;
            String name, value;
            for (i = 0; i < reader.getAttributeCount(); i++) {
                name = reader.getAttributeName(i);
                value = reader.getAttribute(i);
                if (name.equals("maps"))
                    metadata.put(name, value.split(","));
                else
                    metadata.put(name, value);
            }
            return metadata;
        }

    }

    public static class ZombieScheduleConverter implements Converter {

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {

        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return null;
        }

        @Override
        public boolean canConvert(Class type) {
            return type.equals(ZombieSpawnSchedule.class);
        }
    }

    static protected void initXStream() {
        if (xstream != null) return;
        xstream = new XStream();
        xstream.registerConverter(new IntArrayConverter());
        xstream.registerConverter(new LongArrayConverter());
        xstream.registerConverter(new DoubleArrayConverter());
        xstream.registerConverter(new MapLocationConverter());
        xstream.registerConverter(new ExtensibleMetadataConverter());
        xstream.registerConverter(new RoundDeltaConverter());
        xstream.useAttributeFor(int.class);
        xstream.useAttributeFor(int[].class);
        xstream.useAttributeFor(long.class);
        xstream.useAttributeFor(long[].class);
        xstream.useAttributeFor(double.class);
        xstream.useAttributeFor(double[].class);
        xstream.useAttributeFor(boolean.class);
        xstream.useAttributeFor(String.class);
        xstream.useAttributeFor(battlecode.common.Direction.class);
        xstream.useAttributeFor(battlecode.common.MapLocation.class);
        xstream.useAttributeFor(battlecode.common.RobotType.class);
        xstream.useAttributeFor(battlecode.common.Team.class);
        xstream.useAttributeFor(DominationFactor.class);
        xstream.aliasPackage("sig", "battlecode.world.signal");
        xstream.aliasPackage("ser", "battlecode.serial");
        xstream.alias("game-map", GameMap.class);
        xstream.alias("initial-robot", GameMap.InitialRobotInfo.class);
    }

    static public XStream getXStream() {
        initXStream();
        return xstream;
    }

    @Override
    public <T> Serializer<T> createSerializer(final OutputStream output,
                                              final InputStream input,
                                              final Class<T> messageClass)
            throws IOException {

        final ObjectOutputStream wrappedOutput;
        if (output != null) {
            wrappedOutput = getXStream().createObjectOutputStream(output);
        } else {
            wrappedOutput = null;
        }

        final ObjectInputStream wrappedInput;
        if (input != null) {
            wrappedInput = getXStream().createObjectInputStream(input);
        } else {
            wrappedInput = null;
        }

        return new StandardSerializer<>(
                wrappedOutput,
                wrappedInput,
                messageClass
        );
    }
}

