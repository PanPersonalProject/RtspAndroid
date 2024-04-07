/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/4/6
 * Description: Add a brief description of the file here.
 ******************************************************************************************/
//CameraSource.hh
#ifndef _DEVICE_SOURCE1_HH
#define _DEVICE_SOURCE1_HH

#ifndef _FRAMED_SOURCE_HH

#include <jni.h>
#include "FramedSource.hh"
#endif

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
    virtual int getNextFrame(int8_t* buf);
    //virtual void doStopGettingFrames(); // optional

private:
    static void deliverFrame0(void* clientData);
    void deliverFrame();

private:
    static unsigned referenceCount; // used to count how many instances of this class currently exist
};

#endif
