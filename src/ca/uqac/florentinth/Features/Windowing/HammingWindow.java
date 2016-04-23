package ca.uqac.florentinth.Features.Windowing;

import java.util.HashMap;
import java.util.Map;

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
public class HammingWindow extends Window {

    private static final Map<Integer, double[]> FACTORS_BY_WINDOW_SIZE = new HashMap<>();

    public HammingWindow(int windowSize) {
        super(windowSize);
    }

    @Override
    protected double[] getPrecomputedFactors(int windowSize) {
        synchronized (HammingWindow.class) {
            double[] factors;

            if (FACTORS_BY_WINDOW_SIZE.containsKey(windowSize)) {
                factors = FACTORS_BY_WINDOW_SIZE.get(windowSize);
            } else {
                factors = new double[windowSize];
                int sizeMinusOne = windowSize - 1;

                for (int i = 0; i < windowSize; i++) {
                    factors[i] = 0.54d - (0.46d * Math.cos((2 * Math.PI * i) / sizeMinusOne));
                }

                FACTORS_BY_WINDOW_SIZE.put(windowSize, factors);
            }
            return factors;
        }
    }
}
