#ifndef _WW_H264VideoSource_H
#define _WW_H264VideoSource_H

#include "liveMedia.hh"
#include "BasicUsageEnvironment.hh"
#include "GroupsockHelper.hh"
#include "FramedSource.hh"

#define FRAME_PER_SEC 25

class WW_H264VideoSource : public FramedSource
{
public:
    WW_H264VideoSource(UsageEnvironment & env);
    ~WW_H264VideoSource(void);

public:
    virtual void doGetNextFrame();
    virtual unsigned int maxFrameSize() const;

    static void getNextFrame(void * ptr);
    void GetFrameData();

private:
    void *m_pToken;
    char *m_pFrameBuffer;
    int  m_hFifo;
};

#endif