#pragma once

#include <readerwriterqueue.h>
#include "MediaData.h"

class ByteBuffer {
public:
    int count;

    void ReadData(MediaData *pVector);

    void WriteData(MediaData *pVector);

    void clear();

private:
    moodycamel::BlockingReaderWriterQueue<MediaData> blockingReaderWriterQueue;
};

