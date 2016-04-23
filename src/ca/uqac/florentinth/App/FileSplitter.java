package ca.uqac.florentinth.App;

import ca.uqac.florentinth.App.Helpers.FileHelper;
import ca.uqac.florentinth.WAVFile.WAVFile;

import java.io.File;

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
public class FileSplitter {
    private static final String SOURCE = FileHelper.getBasePath() + "\\datasets\\Ted_LIUM\\WAVFiles\\all" +
            "\\training";
    private static final String DESTINATION_TRAINING = FileHelper.getBasePath() +
            "\\datasets\\Ted_LIUM\\WAVFiles\\training";
    private static final String DESTINATION_TESTING = FileHelper.getBasePath() +
            "\\datasets\\Ted_LIUM\\WAVFiles\\testing";

    private static final int CHUNK_LENGTH = 12;
    private static final int CHUNK_OUTPUT = 6;
    private static final int STARTING_FRAME = 2;
    private static final int INSTANCE_TRAINING = 5;

    public static void main(String[] args) {
        File[] files = new File(SOURCE).listFiles();

        FileHelper.removeExistingFiles(new File(DESTINATION_TRAINING));
        FileHelper.removeExistingFiles(new File(DESTINATION_TESTING));

        for (int i = 0; i < files.length; i++) {
            try {
                WAVFile inputWAVFile = WAVFile.open(files[i]);

                int numChannels = inputWAVFile.getNumChannels();
                int maxFramesPerFile = (int) inputWAVFile.getSampleRate() * CHUNK_LENGTH;

                double[] buffer = new double[(maxFramesPerFile * numChannels)];

                int framesRead = 1;
                int fileCount = 0;

                while (framesRead != 0) {

                    framesRead = inputWAVFile.readFrames(buffer, maxFramesPerFile);

                    if (fileCount < (CHUNK_OUTPUT + STARTING_FRAME)) {
                        if (fileCount >= STARTING_FRAME) {
                            if (fileCount < INSTANCE_TRAINING) {
                                WAVFile outputWavFile = WAVFile.create(
                                        new File(DESTINATION_TRAINING + "\\" + files[i].getName().split("\\.")
                                                [0] + "-" + (
                                                (fileCount - STARTING_FRAME) + 1) + ".wav"),
                                        inputWAVFile.getNumChannels(),
                                        framesRead,
                                        inputWAVFile.getValidBits(),
                                        inputWAVFile.getSampleRate()
                                );

                                outputWavFile.writeFrames(buffer, framesRead);
                                outputWavFile.close();
                            } else {
                                WAVFile outputWavFile = WAVFile.create(
                                        new File(DESTINATION_TESTING + "\\" + files[i].getName().split("\\.")[0]
                                                + "-" + (
                                                (fileCount - STARTING_FRAME) + 1) + ".wav"),
                                        inputWAVFile.getNumChannels(),
                                        framesRead,
                                        inputWAVFile.getValidBits(),
                                        inputWAVFile.getSampleRate()
                                );

                                outputWavFile.writeFrames(buffer, framesRead);
                                outputWavFile.close();
                            }
                        }
                    } else {
                        break;
                    }
                    fileCount++;
                }

                inputWAVFile.close();

            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
}
