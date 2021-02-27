package de.siegmar.sfm.cli;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.simpleflatmapper.lightningcsv.ClosableCsvWriter;
import org.simpleflatmapper.lightningcsv.CloseableCsvReader;
import org.simpleflatmapper.lightningcsv.CsvParser;
import org.simpleflatmapper.lightningcsv.CsvWriter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import de.siegmar.fastcsv.cli.Test;

public class Impl {

    public void read(final Path csvFile, final Path jsonFile) throws Exception {
        try (JsonGenerator jGenerator = new JsonFactory().createGenerator(jsonFile.toFile(), JsonEncoding.UTF8);
             CloseableCsvReader csv = CsvParser.reader(csvFile.toFile())) {
            for (final String[] csvRow : csv) {
                jGenerator.writeStartArray();
                for (final String field : csvRow) {
                    jGenerator.writeString(field);
                }
                jGenerator.writeEndArray();
            }
        }
    }

    public void write(final Test test, final Path csvFile) throws Exception {
        System.out.println("Work on test " + test.getId());

        try (ClosableCsvWriter csv = CsvWriter.dsl().to(csvFile, StandardCharsets.UTF_8)) {
            for (final Test.Record record : test.getRecords()) {
                csv.appendRow(record.getFields());
            }
        }

        System.out.println("done");
    }

}
