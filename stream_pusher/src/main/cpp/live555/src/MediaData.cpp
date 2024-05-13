#include "include/MediaData.h"

void MediaData::putBuffer(const unsigned char *data, int length) {
    frameBuffer.insert(frameBuffer.end(), data, data + length);
}

u_int8_t* MediaData::getFrameBytes() {
    // Return a pointer to the beginning of the frameBuffer data
    return frameBuffer.data();
}

unsigned MediaData::getFrameSize() {
    // Return the size of the frameBuffer data
    return frameBuffer.size();
}