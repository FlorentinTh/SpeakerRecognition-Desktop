package ca.uqac.florentinth.App.Helpers;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

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
public abstract class FileHelper {

    public static String getBasePath() {
        return new File("").getAbsolutePath();
    }

    public static void removeExistingFiles(File folder) {
        File[] files = folder.listFiles();
        if (files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
    }

    public static void CSVToARFF(File input, File output) throws IOException {
        CSVLoader csvDataset = new CSVLoader();
        csvDataset.setSource(input);
        Instances arffDataset = csvDataset.getDataSet();
        ArffSaver saver = new ArffSaver();
        saver.setInstances(arffDataset);
        saver.setFile(output);
        saver.writeBatch();
    }
}
