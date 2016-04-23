package ca.uqac.florentinth.Features;

import ca.uqac.florentinth.Config.Audio;

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
public class ExtractFeatureFromWindow {

    private static final int DEFAULT_FEATURES_WINDOW_SIZE = 24;

    protected int windowSize;
    protected float sampleRate;

    public ExtractFeatureFromWindow(float sampleRate) {
        if (sampleRate < Audio.getInstance().getMinSampleRate()) {
            System.err.println("[ERROR] Sample rate should be at least " + Audio.getInstance().getMinSampleRate
                    () + " Received : " + sampleRate + "Hz");
        }

        this.sampleRate = sampleRate;
        this.windowSize = getWindowSize(sampleRate, DEFAULT_FEATURES_WINDOW_SIZE);
    }

    private int getWindowSize(float sampleRate, int windowSize) {
        boolean done = false;
        int bytes = 8;
        float previousMillis = 0.0F;

        while (!done) {
            float millis = 1000 / sampleRate * bytes;

            if (millis < windowSize) {
                previousMillis = millis;
                bytes *= 2;
            } else {
                if (Math.abs(windowSize - millis) > windowSize - previousMillis)
                    bytes /= 2;
                done = true;
            }
        }
        return bytes;
    }
}
