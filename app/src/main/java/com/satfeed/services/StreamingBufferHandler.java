package com.satfeed.services;

import android.util.Log;

import com.satfeed.FeedStreamerApplication;

import java.nio.ByteBuffer;
import java.util.Arrays;

/*
 * Created by Andrew Brin on 4/27/2016.
 */
public class StreamingBufferHandler {
        /*
        Helper class for the Streaming Client; wraps the buffer that is persisted through connection mishaps
        Maintains a boolean state accessible by isInitialized()
        */
        int packetLength;
        private ByteBuffer streamingBuffer;
        private ByteBuffer header = ByteBuffer.allocate(12);
        private byte[] binner;
        private byte[] xorSum;
        final byte[] sequenceNumber = new byte[4];
        final byte[] checksum = new byte[4];
        private boolean isInitialized;

        public StreamingBufferHandler() {
            isInitialized = false;
        }

        /*
        This state returns true if a full header has been supplied and will continue to return true until
        the xorChecksum has been performed */
        public boolean isInitialized() {
            return isInitialized;
        }

        public void bufferHeader(byte[] headerBytes) {
            try {
                header.put(headerBytes); //  Putting bytes into header
            } catch (Throwable e) {
                throw e;
            }
            if (!header.hasRemaining()) initializeToHeader();
        }

        public void initializeToHeader() { //  Initialize the remaining fields based on the header
            header.flip();
            header.get(sequenceNumber);
            header.get(checksum);
            this.packetLength = header.getInt();
            header.clear();
            if (packetLength > 8000)
                throw new RuntimeException("lost the stream; length has exceeded bounds of rationality: " + Integer.toString(packetLength) + " bytes requested");
            this.streamingBuffer = ByteBuffer.allocate(packetLength);
            this.binner = new byte[packetLength];
            isInitialized = true;
        }

        public void buffer(byte[] buffer) {
            try {
                streamingBuffer.put(buffer); //  put bytes into streaming buffer
            } catch (Throwable e) {
                Log.e(FeedStreamerApplication.TAG, "could not put into streaming buffer: " + e);
            }
        }

        public byte[] checkSumAndGet() {
            if (streamingBuffer.hasRemaining()) {
                Log.e(FeedStreamerApplication.TAG, "attempted checksum on unfull Buffer");
                return null;
            }
            streamingBuffer.flip();
            try {
                streamingBuffer.get(binner); //  attempting to get buffer to binner
            } catch (Throwable e) {
                Log.e(FeedStreamerApplication.TAG, "index out of bounds for ioget: " + e.getMessage());
            }
            isInitialized = false;
            int remainder = packetLength % 4;
            xorSum = sequenceNumber.clone();
            int i;
            for (i = 0; i < packetLength / 4; i++) {
                xorSum = xorThis(xorSum, binner, i);
            }
            if (remainder != 0) {
                xorSum = xorRemainder(xorSum, binner, remainder, i);
            }
            if (Arrays.equals(xorSum, checksum)) {
                return binner;
            } else return null;
        }

        public int getSequenceNumber() {
            return ByteBuffer.wrap(sequenceNumber).getInt();
        }

        public int getHeaderBytesRemaining() {
            return header.remaining();
        }

        public int getPacketBufBytesRemaining() {
            return streamingBuffer.remaining();
        }

        private byte[] xorRemainder(byte[] xorSum, byte[] binner, int remainder, int iterationOnBinner) {
            byte[] response = new byte[4];
            int r;
            for (r = 0; r < remainder; r++) { //  xor sum for the leftover bytes
                response[r] = ((byte) (xorSum[r] ^ binner[iterationOnBinner * 4 + r]));
            }
            for (r = remainder; r < 4; r++) { //  xor sum with 0xAB filling in the quartet
                response[r] = ((byte) (xorSum[r] ^ (byte) (0xab)));
            }
            return response;
        }

        private byte[] xorThis(byte[] xorSum, byte[] binner, int iterationsOnBinner) {
            byte[] response = new byte[4];
            int j;
            for (j = 0; j < 4; j++) { //  xor sum for full quartets
                response[j] = ((byte) (xorSum[j] ^ binner[iterationsOnBinner * 4 + j]));
            }
            return response;
        }
    }
