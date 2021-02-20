package de.siegmar.fastcsv.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import de.siegmar.fastcsv.writer.CsvWriter;

public class FastCsv {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalStateException("Wrong usage");
        }

        final String mode = args[0];
        final Path srcFile = Paths.get(args[1]);
        final Path destFile = Paths.get(args[2]);

        if ("write".equals(mode)) {
            write(srcFile, destFile);
        } else if ("read".equals(mode)) {
            read(srcFile, destFile);
        } else {
            throw new IllegalStateException("Wrong mode: " + mode);
        }
    }

    private static void write(final Path jsonFile, final Path csvFile) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Test test = objectMapper.readValue(jsonFile.toFile(), Test.class);
        System.out.println("Work on test " + test.getId());

        try (CsvWriter csv = CsvWriter.builder().build(csvFile, StandardCharsets.UTF_8)) {
            for (final Record record : test.getRecords()) {
                csv.writeRow(record.getFields());
            }
        }

        System.out.println("done");
    }

    private static void read(final Path csvFile, final Path jsonFile) throws IOException {
        try (JsonGenerator jGenerator = new JsonFactory().createGenerator(jsonFile.toFile(), JsonEncoding.UTF8);
             CsvReader csv = CsvReader.builder().build(csvFile, StandardCharsets.UTF_8)) {
            for (final CsvRow csvRow : csv) {
                jGenerator.writeStartArray();
                for (final String field : csvRow.getFields()) {
                    jGenerator.writeString(field);
                }
                jGenerator.writeEndArray();
            }
        }
    }

}
