package ca.uqac.florentinth.App;

import ca.uqac.florentinth.App.Helpers.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
public class Ahumada {
    private static final File SOURCE = new File(FileHelper.getBasePath() +
            "\\datasets\\Ahumada_25\\WAVFiles\\source");
    private static final File DESTINATION = new File(FileHelper.getBasePath() +
            "\\datasets\\Ahumada_25\\WAVFiles\\all");
    private static final String FOLDER_NAME = "M7";

    public static void main(String[] args) throws IOException {
        File[] folders = SOURCE.listFiles();
        for (int i = 0; i < folders.length; i++) {
            File[] subfolders = folders[i].listFiles();
            for (int j = 0; j < subfolders.length; j++) {
                if (subfolders[j].getName().equals(FOLDER_NAME)) {
                    File[] files = subfolders[j].listFiles();
                    for (int k = 0; k < files.length; k++) {
                        String filename = files[k].getName().split("\\.")[0];
                        if ((filename.substring(filename.length() - 3)).compareToIgnoreCase("f00") == 0) {
                            Files.copy(
                                    files[k].toPath(),
                                    new File(DESTINATION + "\\" + files[k].getName()).toPath()
                            );
                        }
                    }
                }
            }
        }
    }
}
