package ca.uqac.florentinth.App;

import ca.uqac.florentinth.App.Helpers.FileHelper;
import ca.uqac.florentinth.Learning.Learning;

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
public class Learn {
    private static final String TRAINING_DATASET = FileHelper.getBasePath() +
            "\\datasets\\UQACStuds\\dataset\\training\\UQACStuds-training.csv";
    private static final String TESTING_DATASET = FileHelper.getBasePath() +
            "\\datasets\\UQACStuds\\dataset\\testing\\UQACStuds-testing.csv";

    public static void main(String[] args) throws Exception {
        Learning learning = new Learning();
        learning.trainClassifierSuppliedTest(TRAINING_DATASET, TESTING_DATASET);
    }
}
