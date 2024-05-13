//
// Created by Pan on 2024/4/20.
//
#pragma once

#include <vector>

class MediaData {
public:
    void putBuffer(const unsigned char *data, int length);

    std::vector<uint8_t> frameBuffer;

    u_int8_t *getFrameBytes();

    unsigned getFrameSize();

};
