package ca.uqac.florentinth.Features.Windowing;

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
public abstract class Window {

    private int windowSize;
    private double[] factors;

    public Window(int windowSize) {
        this.windowSize = windowSize;
        this.factors = getPrecomputedFactors(windowSize);
    }

    protected abstract double[] getPrecomputedFactors(int windowSize);

    public void applyWindow(double[] window) {
        if (window.length == this.windowSize) {
            for (int i = 0; i < window.length; i++) {
                window[i] *= factors[i];
            }
        } else {
            System.err.println("[ERROR] Incompatible window size for this WindowFunction instance : expected " +
                    windowSize + ", received " + window.length);
        }
    }
}
