package io.quati.feature.dump;

import io.quati.api.Feature;
import io.quati.core.AbstractFeature;
import io.quati.util.Json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

@Feature(
        name = "dump",
        description = "dump the database tables to local storage",
        commands = {

        })
public class DumpFeature extends AbstractFeature {

    public record DumpMetaData(int columnCount, String[] columnNames, String[] columnTypes) {
        public static DumpMetaData of(ResultSetMetaData metaData) throws SQLException {
            var names = new ArrayList<String>();
            var types = new ArrayList<String>();
            for (int column = 1; column <= metaData.getColumnCount(); column++) {
                names.add(metaData.getColumnName(column));
                types.add(metaData.getColumnClassName(column)
                        .replace("java.lang.", "")
                        .replace("java.math.", "")
                        .replace("java.sql.", ""));
            }
            return new DumpMetaData(
                    metaData.getColumnCount(),
                    names.toArray(new String[0]),
                    types.toArray(new String[0]));
        }

        public Map<?, ?> toMap() {
            return Map.of(
                    "c", columnCount,
                    "n", List.of(columnNames),
                    "t", List.of(columnTypes));
        }

        public static DumpMetaData fromMap(Map<?, ?> map) {
            try {
                var names = new ArrayList<String>();
                var types = new ArrayList<String>();
                for (var name : ((List<?>) map.get("n")))
                    names.add(String.valueOf(name));
                for (var type : ((List<?>) map.get("t")))
                    types.add(String.valueOf(type));
                return new DumpMetaData(
                        (int) map.get("c"),
                        names.toArray(new String[0]),
                        types.toArray(new String[0]));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void writeTo(SeekableByteChannel channel) {
            try {
                var bytes = Json.toStr(toMap()).getBytes(StandardCharsets.UTF_8);
                var buffer = ByteBuffer.allocateDirect(bytes.length + 4);
                buffer.putInt(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                channel.write(buffer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static DumpMetaData readFrom(SeekableByteChannel channel) {
            try {
                var buffer = ByteBuffer.allocateDirect(4);
                channel.read(buffer);
                buffer.flip();
                int size = buffer.getInt();
                buffer = ByteBuffer.allocateDirect(size);
                channel.read(buffer);
                buffer.flip();
                var bytes = new byte[size];
                buffer.get(bytes);
                return fromMap((Map<?, ?>) Json.parse(new String(bytes, StandardCharsets.UTF_8)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Object[] record(ResultSet rs) {
            try {
                var array = new Object[columnCount];
                for (int index = 0; index < columnCount; index++)
                    switch (columnTypes[index]) {
                        case "Byte", "Short", "Integer", "Long", "Float", "Double", "Boolean",
                             "BigInteger", "BigDecimal", "Date", "Time", "Timestamp", "Blob", "Clob"
                                -> array[index] = rs.getObject(index + 1);
                        default -> array[index] = rs.getString(index + 1);
                    }
                return array;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void dump(String name, String table, ResultSet rs) {
        var dump = context
                .repository()
                .resolve(name)
                .resolve(table);
        try (var channel = Files.newByteChannel(dump, CREATE, TRUNCATE_EXISTING, WRITE)) {
            var metaData = DumpMetaData.of(rs.getMetaData());
            metaData.writeTo(channel);
            while (rs.next()) {
                var header = new boolean[metaData.columnCount];
                var record = metaData.record(rs);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Byte.class.getName());
        System.out.println(Short.class.getName());
        System.out.println(Integer.class.getName());
        System.out.println(Long.class.getName());
        System.out.println(Float.class.getName());
        System.out.println(Double.class.getName());
        System.out.println(BigInteger.class.getName());
        System.out.println(BigDecimal.class.getName());
        System.out.println();

        System.out.println(Date.class.getName());
        System.out.println(Time.class.getName());
        System.out.println(Timestamp.class.getName());
        System.out.println();

        System.out.println(Blob.class.getName());
        System.out.println(Clob.class.getName());

    }

    private static byte[] toByteArray(boolean[] flags) {
        int size = flags.length / 8 + (int) Math.pow(flags.length % 8, 0);
        var array = new byte[size];
        for (int index = 0; index < array.length; index++) {
            for (int offset = 0; offset < 8 && (index * 8 + offset) < flags.length; offset++) {
                byte flag = flags[index * 8 + offset] ? (byte) 1 : 0;
                array[index] += (byte) (flag * Math.pow(2, 7 - offset));
            }
        }
        return array;
    }

    private static void toBooleanArray(byte[] bytes, boolean[] flags) {
        for (int index = 0; index < bytes.length; index++)
            for (int offset = 0; offset < 8 && (index * 8 + offset) < flags.length; offset++)
                flags[index * 8 + offset] = (bytes[index] & (1 << (7 - offset))) != 0;
    }
}
