#include "include/ByteBuffer.h"
#include "../../Base/include/AndroidLog.h"

void ByteBuffer::ReadData(MediaData *pVector) {
    count--;
    blockingReaderWriterQueue.wait_dequeue(*pVector);
}

void ByteBuffer::WriteData(MediaData *pVector) {
    count++;
    blockingReaderWriterQueue.enqueue(*pVector);
//    LOG("length =%d", length);
}

void ByteBuffer::clear() {
    count = 0;
    blockingReaderWriterQueue = moodycamel::BlockingReaderWriterQueue<MediaData>();
    LOGI("ByteBuffer clear finished");
}
