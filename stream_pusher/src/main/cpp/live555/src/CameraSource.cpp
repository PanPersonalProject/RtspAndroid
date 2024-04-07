/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/4/6
 * Description: Add a brief description of the file here.
 ******************************************************************************************/
#include "CameraSource.h"
//CameraSource.cpp
#include "GroupsockHelper.hh" // for "gettimeofday()"
static int8_t buf[8192*30];
static int count = 0;
CameraSource*
CameraSource::createNew(UsageEnvironment& env) {
    return new CameraSource(env);
}

EventTriggerId CameraSource::eventTriggerId = 0;

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

void CameraSource::doGetNextFrame() {
    count = getNextFrame(buf);
    deliverFrame();
}

void CameraSource::deliverFrame0(void* clientData) {
    ((CameraSource*)clientData)->deliverFrame();
}

void CameraSource::deliverFrame() {
    if (!isCurrentlyAwaitingData()) return; // we're not ready for the data yet

    u_int8_t* newFrameDataStart = (u_int8_t*)buf; //%%% TO BE WRITTEN %%%
    unsigned newFrameSize = static_cast<unsigned int>(count); //%%% TO BE WRITTEN %%%

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
