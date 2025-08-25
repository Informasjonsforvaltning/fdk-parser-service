package no.digdir.fdk.parseservice.utils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.io.ParsingEncoder;
import org.apache.avro.io.parsing.JsonGrammarGenerator;
import org.apache.avro.io.parsing.Parser;
import org.apache.avro.io.parsing.Symbol;
import org.apache.avro.util.Utf8;

/**
 * Copy of org.apache.avro.io.JsonEncoder without proper union support, to correctly handle nullables.
 * To achieve this a few lines were removed from the writeIndex method.
 */
public class FlatteningJsonEncoder extends ParsingEncoder implements Parser.ActionHandler {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    final Parser parser;
    private JsonGenerator out;
    private boolean includeNamespace;
    protected BitSet isEmpty;

    public FlatteningJsonEncoder(Schema sc, OutputStream out) throws IOException {
        this(sc, getJsonGenerator(out, EnumSet.noneOf(JsonOptions.class)));
    }

    public FlatteningJsonEncoder(Schema sc, OutputStream out, boolean pretty) throws IOException {
        this(sc, getJsonGenerator(out, pretty ? EnumSet.of(JsonOptions.Pretty) : EnumSet.noneOf(JsonOptions.class)));
    }

    public FlatteningJsonEncoder(Schema sc, OutputStream out, Set<JsonOptions> options) throws IOException {
        this(sc, getJsonGenerator(out, options));
    }

    FlatteningJsonEncoder(Schema sc, JsonGenerator out) throws IOException {
        this.includeNamespace = true;
        this.isEmpty = new BitSet();
        this.configure(out);
        this.parser = new Parser((new JsonGrammarGenerator()).generate(sc), this);
    }

    public void flush() throws IOException {
        this.parser.processImplicitActions();
        if (this.out != null) {
            this.out.flush();
        }

    }

    private static JsonGenerator getJsonGenerator(OutputStream out, Set<JsonOptions> options) throws IOException {
        Objects.requireNonNull(out, "OutputStream cannot be null");
        JsonGenerator g = (new JsonFactory()).createGenerator(out, JsonEncoding.UTF8);
        if (options.contains(JsonOptions.NoFlushStream)) {
            g = g.configure(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM, false);
        }

        PrettyPrinter pp;
        if (options.contains(JsonOptions.Pretty)) {
            pp = new DefaultPrettyPrinter(LINE_SEPARATOR);
        } else {
            pp = new MinimalPrettyPrinter(LINE_SEPARATOR);
        }

        g.setPrettyPrinter(pp);
        return g;
    }

    public boolean isIncludeNamespace() {
        return this.includeNamespace;
    }

    public void setIncludeNamespace(final boolean includeNamespace) {
        this.includeNamespace = includeNamespace;
    }

    public FlatteningJsonEncoder configure(OutputStream out) throws IOException {
        return this.configure(out, true);
    }

    public FlatteningJsonEncoder configure(OutputStream out, boolean autoflush) throws IOException {
        EnumSet<JsonOptions> jsonOptions = EnumSet.noneOf(JsonOptions.class);
        if (!autoflush) {
            jsonOptions.add(JsonOptions.NoFlushStream);
        }

        this.configure(getJsonGenerator(out, jsonOptions));
        return this;
    }

    private FlatteningJsonEncoder configure(JsonGenerator generator) throws IOException {
        Objects.requireNonNull(generator, "JsonGenerator cannot be null");
        if (null != this.parser) {
            this.flush();
        }

        this.out = generator;
        return this;
    }

    public void writeNull() throws IOException {
        this.parser.advance(Symbol.NULL);
        this.out.writeNull();
    }

    public void writeBoolean(boolean b) throws IOException {
        this.parser.advance(Symbol.BOOLEAN);
        this.out.writeBoolean(b);
    }

    public void writeInt(int n) throws IOException {
        this.parser.advance(Symbol.INT);
        this.out.writeNumber(n);
    }

    public void writeLong(long n) throws IOException {
        this.parser.advance(Symbol.LONG);
        this.out.writeNumber(n);
    }

    public void writeFloat(float f) throws IOException {
        this.parser.advance(Symbol.FLOAT);
        this.out.writeNumber((double)f + (double)0.0F);
    }

    public void writeDouble(double d) throws IOException {
        this.parser.advance(Symbol.DOUBLE);
        this.out.writeNumber(d);
    }

    public void writeString(Utf8 utf8) throws IOException {
        this.writeString(utf8.toString());
    }

    public void writeString(String str) throws IOException {
        this.parser.advance(Symbol.STRING);
        if (this.parser.topSymbol() == Symbol.MAP_KEY_MARKER) {
            this.parser.advance(Symbol.MAP_KEY_MARKER);
            this.out.writeFieldName(str);
        } else {
            this.out.writeString(str);
        }

    }

    public void writeBytes(ByteBuffer bytes) throws IOException {
        if (bytes.hasArray()) {
            this.writeBytes(bytes.array(), bytes.position(), bytes.remaining());
        } else {
            byte[] b = new byte[bytes.remaining()];
            bytes.duplicate().get(b);
            this.writeBytes(b);
        }

    }

    public void writeBytes(byte[] bytes, int start, int len) throws IOException {
        this.parser.advance(Symbol.BYTES);
        this.writeByteArray(bytes, start, len);
    }

    private void writeByteArray(byte[] bytes, int start, int len) throws IOException {
        this.out.writeString(new String(bytes, start, len, StandardCharsets.ISO_8859_1));
    }

    public void writeFixed(byte[] bytes, int start, int len) throws IOException {
        this.parser.advance(Symbol.FIXED);
        Symbol.IntCheckAction top = (Symbol.IntCheckAction)this.parser.popSymbol();
        if (len != top.size) {
            throw new AvroTypeException("Incorrect length for fixed binary: expected " + top.size + " but received " + len + " bytes.");
        } else {
            this.writeByteArray(bytes, start, len);
        }
    }

    public void writeEnum(int e) throws IOException {
        this.parser.advance(Symbol.ENUM);
        Symbol.EnumLabelsAction top = (Symbol.EnumLabelsAction)this.parser.popSymbol();
        if (e >= 0 && e < top.size) {
            this.out.writeString(top.getLabel(e));
        } else {
            throw new AvroTypeException("Enumeration out of range: max is " + top.size + " but received " + e);
        }
    }

    public void writeArrayStart() throws IOException {
        this.parser.advance(Symbol.ARRAY_START);
        this.out.writeStartArray();
        this.push();
        this.isEmpty.set(this.depth());
    }

    public void writeArrayEnd() throws IOException {
        if (!this.isEmpty.get(this.pos)) {
            this.parser.advance(Symbol.ITEM_END);
        }

        this.pop();
        this.parser.advance(Symbol.ARRAY_END);
        this.out.writeEndArray();
    }

    public void writeMapStart() throws IOException {
        this.push();
        this.isEmpty.set(this.depth());
        this.parser.advance(Symbol.MAP_START);
        this.out.writeStartObject();
    }

    public void writeMapEnd() throws IOException {
        if (!this.isEmpty.get(this.pos)) {
            this.parser.advance(Symbol.ITEM_END);
        }

        this.pop();
        this.parser.advance(Symbol.MAP_END);
        this.out.writeEndObject();
    }

    public void startItem() throws IOException {
        if (!this.isEmpty.get(this.pos)) {
            this.parser.advance(Symbol.ITEM_END);
        }

        super.startItem();
        this.isEmpty.clear(this.depth());
    }

    /**
     * Normally JsonEncoder would write { "type": value }
     * Instead the union type wrapper is skipped by removing this if statement:
     * if (symbol != Symbol.NULL && this.includeNamespace) {
     *     this.out.writeStartObject();
     *     this.out.writeFieldName(top.getLabel(unionIndex));
     *     this.parser.pushSymbol(Symbol.UNION_END);
     * }
     */
    public void writeIndex(int unionIndex) throws IOException {
        this.parser.advance(Symbol.UNION);
        Symbol.Alternative top = (Symbol.Alternative)this.parser.popSymbol();
        Symbol symbol = top.getSymbol(unionIndex);

        this.parser.pushSymbol(symbol);
    }

    public Symbol doAction(Symbol input, Symbol top) throws IOException {
        if (top instanceof Symbol.FieldAdjustAction) {
            Symbol.FieldAdjustAction fa = (Symbol.FieldAdjustAction)top;
            this.out.writeFieldName(fa.fname);
        } else if (top == Symbol.RECORD_START) {
            this.out.writeStartObject();
        } else if (top != Symbol.RECORD_END && top != Symbol.UNION_END) {
            if (top != Symbol.FIELD_END) {
                throw new AvroTypeException("Unknown action symbol " + String.valueOf(top));
            }
        } else {
            this.out.writeEndObject();
        }

        return null;
    }

    enum JsonOptions {
        Pretty,
        NoFlushStream;
    }
}
