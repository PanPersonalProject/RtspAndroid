/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/4/5
 * Description: Add a brief description of the file here.
 ******************************************************************************************/
#include "WW_H264VideoServerMediaSubsession.h"

WW_H264VideoServerMediaSubsession::WW_H264VideoServerMediaSubsession(UsageEnvironment & env, FramedSource * source) : OnDemandServerMediaSubsession(env, True)
{
    m_pSource = source;
    m_pSDPLine = 0;
}

WW_H264VideoServerMediaSubsession::~WW_H264VideoServerMediaSubsession(void)
{
    if (m_pSDPLine)
    {
        free(m_pSDPLine);
    }
}

WW_H264VideoServerMediaSubsession * WW_H264VideoServerMediaSubsession::createNew(UsageEnvironment & env, FramedSource * source)
{
    return new WW_H264VideoServerMediaSubsession(env, source);
}

FramedSource * WW_H264VideoServerMediaSubsession::createNewStreamSource(unsigned clientSessionId, unsigned & estBitrate)
{
    return H264VideoStreamFramer::createNew(envir(), new WW_H264VideoSource(envir()));
}

RTPSink * WW_H264VideoServerMediaSubsession::createNewRTPSink(Groupsock * rtpGroupsock, unsigned char rtpPayloadTypeIfDynamic, FramedSource * inputSource)
{
    return H264VideoRTPSink::createNew(envir(), rtpGroupsock, rtpPayloadTypeIfDynamic);
}

char const * WW_H264VideoServerMediaSubsession::getAuxSDPLine(RTPSink * rtpSink, FramedSource * inputSource)
{
    if (m_pSDPLine)
    {
        return m_pSDPLine;
    }

    m_pDummyRTPSink = rtpSink;

    //mp_dummy_rtpsink->startPlaying(*source, afterPlayingDummy, this);
    m_pDummyRTPSink->startPlaying(*inputSource, 0, 0);

    chkForAuxSDPLine(this);

    m_done = 0;

    envir().taskScheduler().doEventLoop(&m_done);

    m_pSDPLine = strdup(m_pDummyRTPSink->auxSDPLine());

    m_pDummyRTPSink->stopPlaying();

    return m_pSDPLine;
}

void WW_H264VideoServerMediaSubsession::afterPlayingDummy(void * ptr)
{
    WW_H264VideoServerMediaSubsession * This = (WW_H264VideoServerMediaSubsession *)ptr;

    This->m_done = 0xff;
}

void WW_H264VideoServerMediaSubsession::chkForAuxSDPLine(void * ptr)
{
    WW_H264VideoServerMediaSubsession * This = (WW_H264VideoServerMediaSubsession *)ptr;

    This->chkForAuxSDPLine1();
}

void WW_H264VideoServerMediaSubsession::chkForAuxSDPLine1()
{
    if (m_pDummyRTPSink->auxSDPLine())
    {
        m_done = 0xff;
    }
    else
    {
        double delay = 1000.0 / (FRAME_PER_SEC);  // ms
        int to_delay = delay * 1000;  // us

        nextTask() = envir().taskScheduler().scheduleDelayedTask(to_delay, chkForAuxSDPLine, this);
    }
}