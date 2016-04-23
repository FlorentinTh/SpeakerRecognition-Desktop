package ca.uqac.florentinth.App;

import ca.uqac.florentinth.App.Helpers.FileHelper;
import ca.uqac.florentinth.Core.SpeakerRecognition;
import ca.uqac.florentinth.Dataset.CSVWriter;

import javax.sound.sampled.UnsupportedAudioFileException;
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
public class DatasetBuilder {
    private static final File AUDIO_FILE_INPUT = new File(FileHelper.getBasePath() + "\\datasets\\UQACStuds" +
            "\\WAVFiles\\all");
    private static final String DATASET_BASE_PATH = FileHelper.getBasePath() +
            "\\datasets\\UQACStuds\\dataset\\training";
    private static final String DATASET_FILENAME = "UQACStuds-training.csv";
    private static final String DATASET_PATH = DATASET_BASE_PATH + "\\" + DATASET_FILENAME;

    private static final float WAV_SAMPLING_RATE = 44100.0f;

    public static void main(String[] args) {
        SpeakerRecognition<String> speakerRecognition = new SpeakerRecognition<>(WAV_SAMPLING_RATE);
        File[] files = AUDIO_FILE_INPUT.listFiles();

        FileHelper.removeExistingFiles(new File(DATASET_BASE_PATH));

        CSVWriter csvWriter = new CSVWriter(new File(DATASET_PATH));
        csvWriter.writeDatasetHeader();

        for (int i = 0; i < files.length; i++) {
            String file = files[i].getName();
            String filename;

            filename = file.split("\\.")[0];
            filename = filename.substring(0, filename.length() - 2);

            try {
                speakerRecognition.createVoicePrintFromFile(filename, files[i], DATASET_PATH);
            } catch (UnsupportedAudioFileException e) {
                System.err.println("[ERROR] Unsupported specified audio file : " + e
                        .getMessage());
            } catch (IOException e) {
                System.err.println("[ERROR] Unable to process specified audio file : " + e.getMessage());
            }
        }

        try {
            FileHelper.CSVToARFF(new File(DATASET_PATH), new File(DATASET_BASE_PATH, DATASET_FILENAME.split("\\.")
                    [0] + ".arff"));
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
