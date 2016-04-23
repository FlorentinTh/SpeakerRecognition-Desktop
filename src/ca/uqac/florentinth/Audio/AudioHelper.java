package ca.uqac.florentinth.Audio;

import ca.uqac.florentinth.Config.Audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

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
public class AudioHelper {

    private static final int BUFFER_SIZE = 8192;

    private static short byteArrayToShort(byte[] bytes, int offset, boolean bigEndian) {
        int low, high;

        if (bigEndian) {
            low = bytes[offset + 1];
            high = bytes[offset + 0];
        } else {
            low = bytes[offset + 0];
            high = bytes[offset + 1];
        }
        return (short) ((high << 8) | (0xFF & low));
    }

    public static double[] readAudioInputStream(AudioInputStream audioInputStream) throws IOException,
            UnsupportedAudioFileException {
        AudioFormat originalAudioFormat = audioInputStream.getFormat();
        AudioFormat audioFormat = new AudioFormat(originalAudioFormat.getSampleRate(), Audio.getInstance()
                .getAudioSampleSizeInBits(), Audio.getInstance().getAudioChannelNumber(), true, true);

        AudioInputStream inputStream = null;

        if (!originalAudioFormat.matches(audioFormat)) {
            if (AudioSystem.isConversionSupported(audioFormat, originalAudioFormat)) {
                inputStream = AudioSystem.getAudioInputStream(audioFormat, audioInputStream);
            } else {
                System.err.println("[ERROR] Unable to convert the audio file");
            }
        } else {
            inputStream = audioInputStream;
        }

        double[] audioSample = new double[(int) inputStream.getFrameLength()];
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesToRead = 0, offset = 0;

        while ((bytesToRead = inputStream.read(buffer)) > -1) {
            int counter = (bytesToRead / 2) + (bytesToRead % 2);

            for (int i = 0; i < counter; i++) {
                double tmp = (double) byteArrayToShort(buffer, 2 * i, audioFormat.isBigEndian()) / 32768;
                audioSample[offset + i] = tmp;
            }

            offset += counter;
        }

        inputStream.close();

        return audioSample;
    }

    public static double[] convertFileToDoubleArray(File voiceSample, float voiceSampleRate) throws
            UnsupportedAudioFileException, IOException {
        AudioInputStream audioFile = AudioSystem.getAudioInputStream(voiceSample);
        AudioFormat audioFileFormat = audioFile.getFormat();
        float delta = Math.abs(audioFileFormat.getSampleRate() - voiceSampleRate);

        if (delta > 5 * Math.ulp(0.0f)) {
            System.err.println("[ERROR] Minimum rate require for the sample is not satisfied : " +
                    audioFileFormat.getSampleRate() + " instead of : " + Audio.getInstance().getMinSampleRate());
        }

        return AudioHelper.readAudioInputStream(audioFile);
    }
}
