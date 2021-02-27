package de.siegmar.fastcsv.cli;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import de.siegmar.fastcsv.writer.CsvWriter;

public class Impl {

    public void read(final Path csvFile, final Path jsonFile) throws Exception {
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

    public void write(final Test test, final Path csvFile) throws Exception {
        System.out.println("Work on test " + test.getId());

        try (CsvWriter csv = CsvWriter.builder().build(csvFile, StandardCharsets.UTF_8)) {
            for (final Test.Record record : test.getRecords()) {
                csv.writeRow(record.getFields());
            }
        }

        System.out.println("done");
    }

}
