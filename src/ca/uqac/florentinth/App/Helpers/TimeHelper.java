package ca.uqac.florentinth.App.Helpers;

import java.util.Date;

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
public abstract class TimeHelper {

    public static long startProcessingTimeEvaluation() {
        return new Date().getTime();
    }

    public static void endProcessingTimeEvaluation(long startVal) {
        long end = new Date().getTime();
        long time = end - startVal;

        if (time >= 1000) {
            if ((time / 1000) >= 60) {
                System.out.println("Time elapsed : " + (time / 1000) / 60 + "m");
            } else {
                System.out.println("Time elapsed : " + time / 1000 + "s");
            }
        } else {
            System.out.println("Time elapsed : " + time + "ms");
        }

        System.out.println("------------------------------");
    }
}
