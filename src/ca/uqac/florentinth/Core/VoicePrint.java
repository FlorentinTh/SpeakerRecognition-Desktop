package ca.uqac.florentinth.Core;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
public class VoicePrint {

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private double[] voiceFeatures;
    private int meanCount;

    public VoicePrint(double[] voiceFeatures) {
        this.voiceFeatures = voiceFeatures;
        this.meanCount = 1;
    }

    public VoicePrint(VoicePrint voicePrint) {
        this(Arrays.copyOf(voicePrint.voiceFeatures, voicePrint.voiceFeatures.length));
    }

    public void mergeVoicePrintFeatures(double[] voiceFeatures) {
        if (this.voiceFeatures.length != voiceFeatures.length) {
            System.err.println("[ERROR] VoicePrints features lengths are different : [" + voiceFeatures.length +
                    "] " + "expected [" + this.voiceFeatures.length + "]");
        }

        writeLock.lock();

        try {
            for (int i = 0; i < this.voiceFeatures.length; i++) {
                this.voiceFeatures[i] = (this.voiceFeatures[i] * meanCount + voiceFeatures[i]) / (meanCount + 1);
            }

            meanCount++;
        } finally {
            writeLock.unlock();
        }
    }
}


