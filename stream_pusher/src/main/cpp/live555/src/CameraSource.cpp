/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/4/6
 * Description: Add a brief description of the file here.
 ******************************************************************************************/
#include "CameraSource.h"
//CameraSource.cpp
#include "GroupsockHelper.hh" // for "gettimeofday()"
#include "ByteBuffer.h"
#include "FileUtil.h"

CameraSource*
CameraSource::createNew(UsageEnvironment& env) {
    return new CameraSource(env);
}
extern  ByteBuffer videoBufferQueue;
EventTriggerId CameraSource::eventTriggerId = 0;
bool testWriteBufferToVideoFile = false;//将视频帧保存到document目录下，用于检测编码后的视频帧数据是否正常

unsigned CameraSource::referenceCount = 0;

CameraSource::CameraSource(UsageEnvironment& env)
        : FramedSource(env) {
    if (referenceCount == 0) {
        // Any global initialization of the device would be done here:
        //%%% TO BE WRITTEN %%%
    }
    ++referenceCount;

    if (eventTriggerId == 0) {
        eventTriggerId = envir().taskScheduler().createEventTrigger(deliverFrame0);
    }
}

CameraSource::~CameraSource() {
    --referenceCount;
    if (referenceCount == 0) {
        envir().taskScheduler().deleteEventTrigger(eventTriggerId);
        eventTriggerId = 0;
    }
}
MediaData CameraSource::getNextFrame() {
    MediaData mediaData;
    videoBufferQueue.ReadData(&mediaData);
    if(testWriteBufferToVideoFile){
        FileUtil::writeDataToFile(mediaData.getFrameBytes(), mediaData.getFrameSize());
    }
    return mediaData;
}

void CameraSource::doGetNextFrame() {
    MediaData mediaData=getNextFrame();
    deliverFrame(mediaData);
}

void CameraSource::deliverFrame0(void* clientData) {
    ((CameraSource *) clientData)->deliverFrame(MediaData());
}

void CameraSource::deliverFrame(MediaData data) {
    if (!isCurrentlyAwaitingData()) return; // we're not ready for the data yet



    u_int8_t* newFrameDataStart = data.getFrameBytes();
    unsigned newFrameSize =data.getFrameSize();

    // Deliver the data here:
    if (newFrameSize > fMaxSize) {
        fFrameSize = fMaxSize;
        fNumTruncatedBytes = newFrameSize - fMaxSize;
    } else {
        fFrameSize = newFrameSize;
    }
    gettimeofday(&fPresentationTime, NULL); // If you have a more accurate time - e.g., from an encoder - then use that instead.
    // If the device is *not* a 'live source' (e.g., it comes instead from a file or buffer), then set "fDurationInMicroseconds" here.
    memmove(fTo, newFrameDataStart, fFrameSize);

    // After delivering the data, inform the reader that it is now available:
    FramedSource::afterGetting(this);
}
