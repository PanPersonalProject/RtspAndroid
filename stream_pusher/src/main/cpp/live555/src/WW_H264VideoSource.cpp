/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/4/5
 * Description: Add a brief description of the file here.
 ******************************************************************************************/
#include "WW_H264VideoSource.h"
#include <stdio.h>
#ifdef WIN32
#include <windows.h>
#else
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <limits.h>
#endif

#define FIFO_NAME     "/data/user/0/pan.project.fastrtsplive/files/my_fifo"
#define BUFFER_SIZE   PIPE_BUF
#define REV_BUF_SIZE  (1024*1024)

#ifdef WIN32
#define mSleep(ms)    Sleep(ms)
#else
#define mSleep(ms)    usleep(ms*1000)
#endif


WW_H264VideoSource::WW_H264VideoSource(UsageEnvironment & env) :
        FramedSource(env),
        m_pToken(0),
        m_pFrameBuffer(0),
        m_hFifo(0)
{
    m_hFifo = open(FIFO_NAME,O_RDONLY);
    printf("[MEDIA SERVER] open fifo result = [%d]\n",m_hFifo);
    if(m_hFifo == -1)
    {
        return;
    }

    m_pFrameBuffer = new char[REV_BUF_SIZE];
    if(m_pFrameBuffer == NULL)
    {
        printf("[MEDIA SERVER] error malloc data buffer failed\n");
        return;
    }
    memset(m_pFrameBuffer,0,REV_BUF_SIZE);
}

WW_H264VideoSource::~WW_H264VideoSource(void)
{
    if(m_hFifo)
    {
        ::close(m_hFifo);
    }

    envir().taskScheduler().unscheduleDelayedTask(m_pToken);

    if(m_pFrameBuffer)
    {
        delete[] m_pFrameBuffer;
        m_pFrameBuffer = NULL;
    }

    printf("[MEDIA SERVER] rtsp connection closed\n");
}

void WW_H264VideoSource::doGetNextFrame()
{
    // 根据 fps，计算等待时间
    double delay = 1000.0 / (FRAME_PER_SEC * 2);  // ms
    int to_delay = delay * 1000;  // us

    m_pToken = envir().taskScheduler().scheduleDelayedTask(to_delay, getNextFrame, this);
}

unsigned int WW_H264VideoSource::maxFrameSize() const
{
    return 1024*200;
}

void WW_H264VideoSource::getNextFrame(void * ptr)
{
    ((WW_H264VideoSource *)ptr)->GetFrameData();
}

void WW_H264VideoSource::GetFrameData()
{
    gettimeofday(&fPresentationTime, 0);

    fFrameSize = 0;

    int len = 0;
    unsigned char buffer[BUFFER_SIZE] = {0};
    while((len = read(m_hFifo,buffer,BUFFER_SIZE))>0)
    {
        memcpy(m_pFrameBuffer+fFrameSize,buffer,len);
        fFrameSize+=len;
    }
    //printf("[MEDIA SERVER] GetFrameData len = [%d],fMaxSize = [%d]\n",fFrameSize,fMaxSize);

    // fill frame data
    memcpy(fTo,m_pFrameBuffer,fFrameSize);

    if (fFrameSize > fMaxSize)
    {
        fNumTruncatedBytes = fFrameSize - fMaxSize;
        fFrameSize = fMaxSize;
    }
    else
    {
        fNumTruncatedBytes = 0;
    }

    afterGetting(this);
}