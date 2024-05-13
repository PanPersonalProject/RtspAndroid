/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/4/6
 * Description: Add a brief description of the file here.
 ******************************************************************************************/
//CameraSource.hh
#pragma once
#include <jni.h>
#include "FramedSource.hh"
#include "MediaData.h"


class CameraSource: public FramedSource {
public:
    static CameraSource* createNew(UsageEnvironment& env);

public:
    static EventTriggerId eventTriggerId;
    // Note that this is defined here to be a static class variable, because this code is intended to illustrate how to
    // encapsulate a *single* device - not a set of devices.
    // You can, however, redefine this to be a non-static member variable.

protected:
    CameraSource(UsageEnvironment& env);
    // called only by createNew(), or by subclass constructors
    virtual ~CameraSource();

private:
    // redefined virtual functions:
    virtual void doGetNextFrame();
    virtual MediaData getNextFrame();
    //virtual void doStopGettingFrames(); // optional

private:
    static void deliverFrame0(void* clientData);
    void deliverFrame(MediaData data);

private:
    static unsigned referenceCount; // used to length how many instances of this class currently exist
};

