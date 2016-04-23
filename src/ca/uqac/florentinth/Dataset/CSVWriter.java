package ca.uqac.florentinth.Dataset;

import ca.uqac.florentinth.Classes.Sample;
import ca.uqac.florentinth.Config.Features;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2016 Florentin Thullier.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CSVWriter {

    private static final String LINE_SEPARATOR = "\n";

    private File dataset;
    private FileWriter fileWriter;
    private CSVPrinter csvPrinter;

    public CSVWriter(File file) {
        this.dataset = file;
    }

    private void openCSV(File file) throws IOException {
        fileWriter = new FileWriter(file, true);
        csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withRecordSeparator(LINE_SEPARATOR));
    }

    private void closeCSV() throws IOException {
        fileWriter.flush();
        fileWriter.close();
        csvPrinter.close();
    }

    private void deleteDataset() {
        if (dataset.exists()) {
            dataset.delete();
        }
    }

    public void writeDatasetHeader() {

        deleteDataset();

        try {
            openCSV(dataset);
            int numberFeatures = Features.getInstance().getNumberFeatures();
            String[] headerValues = new String[numberFeatures + 2];

            for (int i = 0; i < numberFeatures; i++) {
                headerValues[i] = "a" + i;
            }

            headerValues[numberFeatures] = "class";
            csvPrinter.printRecord(headerValues);

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load CSV instance: " + e.getMessage());
        } finally {
            try {
                closeCSV();
            } catch (IOException e) {
                System.err.println("[ERROR] Failed closing CSV file: " + e.getMessage());
            }
        }
    }

    public void writeDataset(String name, double[] features) {
        Sample sample = new Sample(name, features);
        List<Sample> samples = new ArrayList<>();
        samples.add(sample);

        try {
            openCSV(dataset);
            for (Sample s : samples) {
                List dataRecords = new ArrayList<>();

                for (int i = 0; i < features.length; i++) {
                    dataRecords.add(String.valueOf(features[i]));
                }

                dataRecords.add(s.getName());

                csvPrinter.printRecord(dataRecords);

            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load CSV instance: " + e.getMessage());
        } finally {
            try {
                closeCSV();
            } catch (IOException e) {
                System.err.println("[ERROR] Failed closing CSV file: " + e.getMessage());
            }
        }
    }
}
