package dev.cerus.blockbind.api.entity;

import dev.cerus.blockbind.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A simple container for entity metadata
 */
public class Metadata {

    /**
     * The actual data storage
     */
    private final Map<Integer, Entry> entryMap = new HashMap<>();

    /**
     * Mainly used in the protocol. Dirty means modified.
     */
    private boolean dirty;

    /**
     * Write the data to a byte buffer
     *
     * @param buf The buffer
     */
    public void write(final ByteBuf buf) {
        BufferUtil.writeMap(buf, this.entryMap, ByteBuf::writeInt, (buffer, entry) -> entry.write(buffer));
    }

    /**
     * Read the data from a byte buffer
     *
     * @param buf The buffer
     */
    public void read(final ByteBuf buf) {
        this.entryMap.clear();
        this.entryMap.putAll(BufferUtil.readMap(buf, ByteBuf::readInt, buffer -> {
            final Entry entry = new Entry();
            entry.read(buffer);
            return entry;
        }));
    }

    /**
     * Overwrite the data of this container with the data of the provided container
     *
     * @param other The other container
     */
    public void overwrite(final Metadata other) {
        this.entryMap.clear();
        this.entryMap.putAll(other.entryMap);
        this.dirty = true;
    }

    /**
     * Set a metadata value
     * Will also set the 'dirty' flag to true
     *
     * @param index The index (key)
     * @param entry The value
     */
    public void set(final int index, final Entry entry) {
        this.entryMap.put(index, entry);
        this.dirty = true;
    }

    /**
     * Get a metadata value
     *
     * @param index The index (key)
     *
     * @return The value (or null)
     */
    public Entry get(final int index) {
        return this.entryMap.get(index);
    }

    /**
     * Get a metadata value
     * Will create a new value using the specified mapping function if the key is not present
     *
     * @param index  The index (key)
     * @param defVal The mapping function for non-existent keys
     *
     * @return The value (or null)
     */
    public Entry getSafe(final int index, final Supplier<Entry> defVal) {
        return this.entryMap.computeIfAbsent(index, $ -> defVal.get());
    }

    /**
     * Is this index (key) set?
     *
     * @param index The index (key)
     *
     * @return True or false
     */
    public boolean has(final int index) {
        return this.entryMap.containsKey(index);
    }

    /**
     * Will iterate over the contents of this container
     *
     * @param fun The iterator function
     */
    public void forEach(final BiConsumer<Integer, Entry> fun) {
        this.entryMap.forEach(fun);
    }

    public boolean isDirty() {
        return this.dirty;
    }

    /**
     * Sets 'dirty' flag to false (cleans the container)
     */
    public void clean() {
        this.dirty = false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Metadata metadata = (Metadata) o;
        return Objects.equals(this.entryMap, metadata.entryMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entryMap);
    }

    public enum EntryType {
        BYTE(0) {
            @Override
            public Object read(final ByteBuf buf) {
                return buf.readByte();
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                buf.writeByte((byte) o);
            }
        },
        INT(1) {
            @Override
            public Object read(final ByteBuf buf) {
                return buf.readInt();
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                buf.writeInt((int) o);
            }
        },
        LONG(2) {
            @Override
            public Object read(final ByteBuf buf) {
                return buf.readLong();
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                buf.writeLong((long) o);
            }
        },
        FLOAT(3) {
            @Override
            public Object read(final ByteBuf buf) {
                return buf.readFloat();
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                buf.writeFloat((float) o);
            }
        },
        DOUBLE(4) {
            @Override
            public Object read(final ByteBuf buf) {
                return buf.readDouble();
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                buf.writeDouble((double) o);
            }
        },
        STRING(5) {
            @Override
            public Object read(final ByteBuf buf) {
                return BufferUtil.readString(buf);
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                BufferUtil.writeString(buf, (String) o);
            }
        },
        CHAT(6) {
            @Override
            public Object read(final ByteBuf buf) {
                return BufferUtil.readString(buf);
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                BufferUtil.writeString(buf, (String) o);
            }
        },
        OPTIONAL_CHAT(7) {
            @Override
            public Object read(final ByteBuf buf) {
                return BufferUtil.readOptional(buf, BufferUtil::readString);
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                BufferUtil.writeOptional(buf, Optional.ofNullable((String) o), BufferUtil::writeString);
            }
        },
        BOOLEAN(8) {
            @Override
            public Object read(final ByteBuf buf) {
                return buf.readBoolean();
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                buf.writeBoolean((Boolean) o);
            }
        },
        POSE(9) {
            @Override
            public Object read(final ByteBuf buf) {
                return buf.readByte();
            }

            @Override
            public void write(final ByteBuf buf, final Object o) {
                buf.writeByte((byte) o);
            }
        };

        private final int i;

        EntryType(final int i) {
            this.i = i;
        }

        public static EntryType getById(final int id) {
            for (final EntryType value : values()) {
                if (value.i == id) {
                    return value;
                }
            }
            return null;
        }

        public abstract Object read(ByteBuf buf);

        public abstract void write(ByteBuf buf, Object o);

    }

    public static class Entry {

        private EntryType type;
        private Object value;

        public Entry(final EntryType type, final Object value) {
            this.type = type;
            this.value = value;
        }

        public Entry() {
        }

        public void read(final ByteBuf buf) {
            this.type = EntryType.getById(buf.readByte());
            this.value = this.type.read(buf);
        }

        public void write(final ByteBuf buf) {
            buf.writeByte(this.type.i);
            this.type.write(buf, this.value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Entry entry = (Entry) o;
            return this.type == entry.type && Objects.equals(this.value, entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.value);
        }

        public EntryType getType() {
            return this.type;
        }

        public Object getValue() {
            return this.value;
        }

    }

}
