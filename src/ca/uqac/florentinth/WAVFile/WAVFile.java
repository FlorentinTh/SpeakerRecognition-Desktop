package ca.uqac.florentinth.WAVFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class WAVFile {
    private final static int BUFFER_SIZE = 4096;
    private final static int FMT_CHUNK_ID = 0x20746D66;
    private final static int DATA_CHUNK_ID = 0x61746164;
    private final static int RIFF_CHUNK_ID = 0x46464952;
    private final static int RIFF_TYPE_ID = 0x45564157;
    private File file;
    private IOState ioState;
    private int bytesPerSample;
    private long numFrames;
    private FileOutputStream oStream;
    private FileInputStream iStream;
    private double floatScale;
    private double floatOffset;
    private boolean wordAlignAdjust;
    private int numChannels;
    private long sampleRate;
    private int blockAlign;
    private int validBits;
    private byte[] buffer;
    private int bufferPointer;
    private int bytesRead;
    private long frameCounter;

    private WAVFile() {
        buffer = new byte[BUFFER_SIZE];
    }

    public static WAVFile create(File file, int numChannels, long numFrames, int validBits, long sampleRate)
            throws IOException, WAVFileException {
        WAVFile WAVFile = new WAVFile();
        WAVFile.file = file;
        WAVFile.numChannels = numChannels;
        WAVFile.numFrames = numFrames;
        WAVFile.sampleRate = sampleRate;
        WAVFile.bytesPerSample = (validBits + 7) / 8;
        WAVFile.blockAlign = WAVFile.bytesPerSample * numChannels;
        WAVFile.validBits = validBits;

        if (numChannels < 1 || numChannels > 65535) {
            throw new WAVFileException("Illegal number of channels, valid range 1 to 65536");
        }
        if (numFrames < 0) {
            throw new WAVFileException("Number of frames must be positive");
        }
        if (validBits < 2 || validBits > 65535) {
            throw new WAVFileException("Illegal number of valid bits, valid range 2 to 65536");
        }
        if (sampleRate < 0) {
            throw new WAVFileException("Sample rate must be positive");
        }

        WAVFile.oStream = new FileOutputStream(file);

        long dataChunkSize = WAVFile.blockAlign * numFrames;
        long mainChunkSize = 4 +
                8 +
                16 +
                8 +
                dataChunkSize;

        if (dataChunkSize % 2 == 1) {
            mainChunkSize += 1;
            WAVFile.wordAlignAdjust = true;
        } else {
            WAVFile.wordAlignAdjust = false;
        }

        putLE(RIFF_CHUNK_ID, WAVFile.buffer, 0, 4);
        putLE(mainChunkSize, WAVFile.buffer, 4, 4);
        putLE(RIFF_TYPE_ID, WAVFile.buffer, 8, 4);

        WAVFile.oStream.write(WAVFile.buffer, 0, 12);

        long averageBytesPerSecond = sampleRate * WAVFile.blockAlign;

        putLE(FMT_CHUNK_ID, WAVFile.buffer, 0, 4);
        putLE(16, WAVFile.buffer, 4, 4);
        putLE(1, WAVFile.buffer, 8, 2);
        putLE(numChannels, WAVFile.buffer, 10, 2);
        putLE(sampleRate, WAVFile.buffer, 12, 4);
        putLE(averageBytesPerSecond, WAVFile.buffer, 16, 4);
        putLE(WAVFile.blockAlign, WAVFile.buffer, 20, 2);
        putLE(validBits, WAVFile.buffer, 22, 2);

        WAVFile.oStream.write(WAVFile.buffer, 0, 24);

        putLE(DATA_CHUNK_ID, WAVFile.buffer, 0, 4);
        putLE(dataChunkSize, WAVFile.buffer, 4, 4);

        WAVFile.oStream.write(WAVFile.buffer, 0, 8);

        if (WAVFile.validBits > 8) {
            WAVFile.floatOffset = 0;
            WAVFile.floatScale = Long.MAX_VALUE >> (64 - WAVFile.validBits);
        } else {
            WAVFile.floatOffset = 1;
            WAVFile.floatScale = 0.5 * ((1 << WAVFile.validBits) - 1);
        }

        WAVFile.bufferPointer = 0;
        WAVFile.bytesRead = 0;
        WAVFile.frameCounter = 0;
        WAVFile.ioState = IOState.WRITING;

        return WAVFile;
    }

    public static WAVFile open(File file) throws IOException, WAVFileException {
        WAVFile WAVFile = new WAVFile();
        WAVFile.file = file;

        WAVFile.iStream = new FileInputStream(file);

        int bytesRead = WAVFile.iStream.read(WAVFile.buffer, 0, 12);
        if (bytesRead != 12) {
            throw new WAVFileException("Not enough wav file bytes for header");
        }

        long riffChunkID = getLE(WAVFile.buffer, 0, 4);
        long chunkSize = getLE(WAVFile.buffer, 4, 4);
        long riffTypeID = getLE(WAVFile.buffer, 8, 4);

        if (riffChunkID != RIFF_CHUNK_ID) {
            throw new WAVFileException("Invalid Wav Header data, incorrect riff chunk ID");
        }
        if (riffTypeID != RIFF_TYPE_ID) {
            throw new WAVFileException("Invalid Wav Header data, incorrect riff type ID");
        }

        if (file.length() != chunkSize + 8) {
            {
                throw new WAVFileException("Header chunk size (" + chunkSize + ") does not match file size (" +
                        file
                                .length() + ")");
            }
        }

        boolean foundFormat = false;
        boolean foundData = false;

        while (true) {
            bytesRead = WAVFile.iStream.read(WAVFile.buffer, 0, 8);
            if (bytesRead == -1) {
                throw new WAVFileException("Reached end of file without finding format chunk");
            }
            if (bytesRead != 8) {
                throw new WAVFileException("Could not read chunk header");
            }

            long chunkID = getLE(WAVFile.buffer, 0, 4);
            chunkSize = getLE(WAVFile.buffer, 4, 4);

            long numChunkBytes = (chunkSize % 2 == 1) ? chunkSize + 1 : chunkSize;

            if (chunkID == FMT_CHUNK_ID) {
                foundFormat = true;
                bytesRead = WAVFile.iStream.read(WAVFile.buffer, 0, 16);

                int compressionCode = (int) getLE(WAVFile.buffer, 0, 2);
                if (compressionCode != 1) {
                    throw new WAVFileException("Compression Code " + compressionCode + " not supported");
                }

                WAVFile.numChannels = (int) getLE(WAVFile.buffer, 2, 2);
                WAVFile.sampleRate = getLE(WAVFile.buffer, 4, 4);
                WAVFile.blockAlign = (int) getLE(WAVFile.buffer, 12, 2);
                WAVFile.validBits = (int) getLE(WAVFile.buffer, 14, 2);

                if (WAVFile.numChannels == 0) {
                    throw new WAVFileException("Number of channels specified in header is equal to zero");
                }
                if (WAVFile.blockAlign == 0) {
                    throw new WAVFileException("Block Align specified in header is equal to zero");
                }
                if (WAVFile.validBits < 2) {
                    throw new WAVFileException("Valid Bits specified in header is less than 2");
                }
                if (WAVFile.validBits > 64) {
                    throw new WAVFileException("Valid Bits specified in header is greater than 64, this is " +
                            "greater than a long can hold");
                }

                WAVFile.bytesPerSample = (WAVFile.validBits + 7) / 8;
                if (WAVFile.bytesPerSample * WAVFile.numChannels != WAVFile.blockAlign) {
                    throw new WAVFileException("Block Align does not agree with bytes required for validBits and" +
                            " number of channels");
                }

                numChunkBytes -= 16;
                if (numChunkBytes > 0) {
                    WAVFile.iStream.skip(numChunkBytes);
                }
            } else if (chunkID == DATA_CHUNK_ID) {
                if (foundFormat == false) {
                    throw new WAVFileException("Data chunk found before Format chunk");
                }

                if (chunkSize % WAVFile.blockAlign != 0) {
                    throw new WAVFileException("Data Chunk size is not multiple of Block Align");
                }

                WAVFile.numFrames = chunkSize / WAVFile.blockAlign;
                foundData = true;
                break;
            } else {
                WAVFile.iStream.skip(numChunkBytes);
            }
        }

        if (foundData == false) {
            throw new WAVFileException("Did not find a data chunk");
        }

        if (WAVFile.validBits > 8) {
            WAVFile.floatOffset = 0;
            WAVFile.floatScale = 1 << (WAVFile.validBits - 1);
        } else {
            WAVFile.floatOffset = -1;
            WAVFile.floatScale = 0.5 * ((1 << WAVFile.validBits) - 1);
        }

        WAVFile.bufferPointer = 0;
        WAVFile.bytesRead = 0;
        WAVFile.frameCounter = 0;
        WAVFile.ioState = IOState.READING;

        return WAVFile;
    }

    private static long getLE(byte[] buffer, int pos, int numBytes) {
        numBytes--;
        pos += numBytes;

        long val = buffer[pos] & 0xFF;

        for (int b = 0; b < numBytes; b++) {
            val = (val << 8) + (buffer[--pos] & 0xFF);
        }

        return val;
    }

    private static void putLE(long val, byte[] buffer, int pos, int numBytes) {
        for (int b = 0; b < numBytes; b++) {
            buffer[pos] = (byte) (val & 0xFF);
            val >>= 8;
            pos++;
        }
    }

    public int getNumChannels() {
        return numChannels;
    }

    public long getNumFrames() {
        return numFrames;
    }

    public long getFramesRemaining() {
        return numFrames - frameCounter;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public int getValidBits() {
        return validBits;
    }

    private void writeSample(long val) throws IOException {
        for (int b = 0; b < bytesPerSample; b++) {
            if (bufferPointer == BUFFER_SIZE) {
                oStream.write(buffer, 0, BUFFER_SIZE);
                bufferPointer = 0;
            }

            buffer[bufferPointer] = (byte) (val & 0xFF);
            val >>= 8;
            bufferPointer++;
        }
    }

    private long readSample() throws IOException, WAVFileException {
        long val = 0;

        for (int b = 0; b < bytesPerSample; b++) {
            if (bufferPointer == bytesRead) {
                int read = iStream.read(buffer, 0, BUFFER_SIZE);

                if (read == -1) {
                    throw new WAVFileException("Not enough data available");
                }

                bytesRead = read;
                bufferPointer = 0;
            }

            int v = buffer[bufferPointer];

            if (b < bytesPerSample - 1 || bytesPerSample == 1) {
                v &= 0xFF;
            }

            val += v << (b * 8);

            bufferPointer++;
        }

        return val;
    }

    public int readFrames(int[] sampleBuffer, int numFramesToRead) throws IOException, WAVFileException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(int[] sampleBuffer, int offset, int numFramesToRead) throws IOException,
            WAVFileException {
        if (ioState != IOState.READING) {
            throw new IOException("Cannot read from WAVFile instance");
        }

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[offset] = (int) readSample();
                offset++;
            }

            frameCounter++;
        }

        return numFramesToRead;
    }

    public int readFrames(int[][] sampleBuffer, int numFramesToRead) throws IOException, WAVFileException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(int[][] sampleBuffer, int offset, int numFramesToRead) throws IOException,
            WAVFileException {
        if (ioState != IOState.READING) {
            throw new IOException("Cannot read from WAVFile instance");
        }

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[c][offset] = (int) readSample();
            }

            offset++;
            frameCounter++;
        }

        return numFramesToRead;
    }

    public int writeFrames(int[] sampleBuffer, int numFramesToWrite) throws IOException, WAVFileException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(int[] sampleBuffer, int offset, int numFramesToWrite) throws IOException,
            WAVFileException {
        if (ioState != IOState.WRITING) {
            throw new IOException("Cannot write to WAVFile instance");
        }

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample(sampleBuffer[offset]);
                offset++;
            }

            frameCounter++;
        }

        return numFramesToWrite;
    }

    public int writeFrames(int[][] sampleBuffer, int numFramesToWrite) throws IOException, WAVFileException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(int[][] sampleBuffer, int offset, int numFramesToWrite) throws IOException,
            WAVFileException {
        if (ioState != IOState.WRITING) {
            throw new IOException("Cannot write to WAVFile instance");
        }

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample(sampleBuffer[c][offset]);
            }

            offset++;
            frameCounter++;
        }

        return numFramesToWrite;
    }

    public int readFrames(long[] sampleBuffer, int numFramesToRead) throws IOException, WAVFileException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(long[] sampleBuffer, int offset, int numFramesToRead) throws IOException,
            WAVFileException {
        if (ioState != IOState.READING) {
            throw new IOException("Cannot read from WAVFile instance");
        }

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[offset] = readSample();
                offset++;
            }

            frameCounter++;
        }

        return numFramesToRead;
    }

    public int readFrames(long[][] sampleBuffer, int numFramesToRead) throws IOException, WAVFileException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(long[][] sampleBuffer, int offset, int numFramesToRead) throws IOException,
            WAVFileException {
        if (ioState != IOState.READING) {
            throw new IOException("Cannot read from WAVFile instance");
        }

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[c][offset] = readSample();
            }

            offset++;
            frameCounter++;
        }

        return numFramesToRead;
    }

    public int writeFrames(long[] sampleBuffer, int numFramesToWrite) throws IOException, WAVFileException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(long[] sampleBuffer, int offset, int numFramesToWrite) throws IOException,
            WAVFileException {
        if (ioState != IOState.WRITING) {
            throw new IOException("Cannot write to WAVFile instance");
        }

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample(sampleBuffer[offset]);
                offset++;
            }

            frameCounter++;
        }

        return numFramesToWrite;
    }

    public int writeFrames(long[][] sampleBuffer, int numFramesToWrite) throws IOException, WAVFileException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(long[][] sampleBuffer, int offset, int numFramesToWrite) throws IOException,
            WAVFileException {
        if (ioState != IOState.WRITING) {
            throw new IOException("Cannot write to WAVFile instance");
        }

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample(sampleBuffer[c][offset]);
            }

            offset++;
            frameCounter++;
        }

        return numFramesToWrite;
    }

    public int readFrames(double[] sampleBuffer, int numFramesToRead) throws IOException, WAVFileException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(double[] sampleBuffer, int offset, int numFramesToRead) throws IOException,
            WAVFileException {
        if (ioState != IOState.READING) {
            throw new IOException("Cannot read from WAVFile instance");
        }

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[offset] = floatOffset + (double) readSample() / floatScale;
                offset++;
            }

            frameCounter++;
        }

        return numFramesToRead;
    }

    public int readFrames(double[][] sampleBuffer, int numFramesToRead) throws IOException, WAVFileException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(double[][] sampleBuffer, int offset, int numFramesToRead) throws IOException,
            WAVFileException {
        if (ioState != IOState.READING) {
            throw new IOException("Cannot read from WAVFile instance");
        }

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[c][offset] = floatOffset + (double) readSample() / floatScale;
            }

            offset++;
            frameCounter++;
        }

        return numFramesToRead;
    }

    public int writeFrames(double[] sampleBuffer, int numFramesToWrite) throws IOException, WAVFileException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(double[] sampleBuffer, int offset, int numFramesToWrite) throws IOException,
            WAVFileException {
        if (ioState != IOState.WRITING) {
            throw new IOException("Cannot write to WAVFile instance");
        }

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample((long) (floatScale * (floatOffset + sampleBuffer[offset])));
                offset++;
            }

            frameCounter++;
        }

        return numFramesToWrite;
    }

    public int writeFrames(double[][] sampleBuffer, int numFramesToWrite) throws IOException, WAVFileException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(double[][] sampleBuffer, int offset, int numFramesToWrite) throws IOException,
            WAVFileException {
        if (ioState != IOState.WRITING) {
            throw new IOException("Cannot write to WAVFile instance");
        }

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample((long) (floatScale * (floatOffset + sampleBuffer[c][offset])));
            }

            offset++;
            frameCounter++;
        }

        return numFramesToWrite;
    }

    public void close() throws IOException {
        if (iStream != null) {
            iStream.close();
            iStream = null;
        }

        if (oStream != null) {
            if (bufferPointer > 0) {
                oStream.write(buffer, 0, bufferPointer);
            }

            if (wordAlignAdjust) {
                oStream.write(0);
            }

            oStream.close();
            oStream = null;
        }

        ioState = IOState.CLOSED;
    }


    private enum IOState {READING, WRITING, CLOSED}
}
