package ca.uqac.florentinth.Core;

import ca.uqac.florentinth.Audio.AudioHelper;
import ca.uqac.florentinth.Audio.PreProcessing.AudioNormalizer;
import ca.uqac.florentinth.Audio.PreProcessing.VoiceActivityDetection;
import ca.uqac.florentinth.Config.Audio;
import ca.uqac.florentinth.Config.Features;
import ca.uqac.florentinth.Dataset.CSVWriter;
import ca.uqac.florentinth.Features.FeaturesExtraction;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class SpeakerRecognition<T> {

    private final ConcurrentHashMap<T, VoicePrint> voicePrints = new ConcurrentHashMap<T, VoicePrint>();
    private final float sampleRate;
    private final AtomicBoolean universalVoicePrintWasSetByUser = new AtomicBoolean();
    private VoicePrint universalVoicePrint;

    public SpeakerRecognition(float sampleRate) {
        if (sampleRate < Audio.getInstance().getMinSampleRate()) {
            System.err.println("[ERROR] Sample rate should be at least : " + Audio.getInstance()
                    .getMinSampleRate() + "Hz." + " Received : " + sampleRate + "Hz");
        }

        this.sampleRate = sampleRate;
    }

    private double[] extractVoiceFeatures(double[] voiceSample, float sampleRate) {
        VoiceActivityDetection voiceActivityDetection = new VoiceActivityDetection();
        AudioNormalizer audioNormalizer = new AudioNormalizer();
        FeaturesExtraction featuresExtraction = new FeaturesExtraction(sampleRate, Features.getInstance()
                .getNumberFeatures());
        System.out.println("Start pre-processing");
        System.out.println("Removing silences");
        voiceActivityDetection.removeSilences(voiceSample, sampleRate);
        System.out.println("Normalizing gain");
        audioNormalizer.normalizeAudio(voiceSample);
        System.out.println("End Pre-processing");
        System.out.println("Start voice features extraction");
        double[] voiceFeatures = featuresExtraction.extractVoiceFeatures(voiceSample);
        System.out.println("End voice features extraction");
        return voiceFeatures;
    }

    public synchronized VoicePrint createVoicePrint(T userKey, double[] voiceSample, String dataset) {
        if (userKey == null) {
            throw new NullPointerException("The userKey is null");
        }

        if (voicePrints.containsKey(userKey)) {
            throw new IllegalArgumentException("The userKey [" + userKey + "] already exists");
        }

        double[] voiceFeatures = extractVoiceFeatures(voiceSample, sampleRate);
        VoicePrint voicePrint = new VoicePrint(voiceFeatures);

        System.out.println("Start writing in the dataset");
        CSVWriter csvWriter = new CSVWriter(new File(dataset));
        csvWriter.writeDataset(userKey.toString(), voiceFeatures);
        System.out.println("End writing in the dataset");

        synchronized (this) {
            if (!universalVoicePrintWasSetByUser.get()) {
                if (universalVoicePrint == null) {
                    universalVoicePrint = new VoicePrint(voicePrint);
                } else {
                    universalVoicePrint.mergeVoicePrintFeatures(voiceFeatures);
                }
            }
        }

        return voicePrint;
    }

    public VoicePrint createVoicePrintFromFile(T userKey, File voiceSample, String dataset) throws
            UnsupportedAudioFileException, IOException {
        return createVoicePrint(userKey, AudioHelper.convertFileToDoubleArray(voiceSample, sampleRate), dataset);
    }
}
