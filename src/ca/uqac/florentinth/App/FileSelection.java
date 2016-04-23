package ca.uqac.florentinth.App;

import ca.uqac.florentinth.App.Helpers.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
public class FileSelection {

    private final static String SOURCE = FileHelper.getBasePath() + "\\datasets\\Ted_LIUM\\WAVFiles\\all";
    private final static String DESTINATION = FileHelper.getBasePath() + "\\datasets\\Ted_LIUM\\WAVFiles\\subset";

    private final static int FILE_NUMBER = 25;

    private static void isIndexExists(List<Integer> indexes, int maxBound) {
        Random random = new Random();
        int index = 1 + random.nextInt(maxBound);

        if (!indexes.contains(index)) {
            indexes.add(index);
        } else {
            isIndexExists(indexes, maxBound);
        }
    }

    public static void main(String[] args) throws IOException {
        List<File> sourceFolder = Arrays.asList(new File(SOURCE).listFiles());

        FileHelper.removeExistingFiles(new File(DESTINATION));

        final int filesNumber = sourceFolder.size();
        List<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < FILE_NUMBER; i++) {
            isIndexExists(indexes, filesNumber);
        }

        for (int i = 0; i < indexes.size(); i++) {
            Files.copy(
                    new File(SOURCE + "\\" + sourceFolder.get(indexes.get(i)).getName()).toPath(),
                    new File(DESTINATION + "\\" + sourceFolder.get(indexes.get(i)).getName()).toPath()
            );
        }
    }
}
