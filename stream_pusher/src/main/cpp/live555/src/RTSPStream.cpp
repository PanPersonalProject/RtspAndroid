/********************************************************************
filename:   RTSPStream.cpp
created:    2013-08-01
author:     firehood
purpose:    通过live555实现H264 RTSP直播
*********************************************************************/
#include "RTSPStream.h"
#include "AndroidLog.h"

#ifdef WIN32
#else
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <limits.h>
#include <errno.h>
#endif

#define FIFO_NAME    "/data/user/0/pan.project.fastrtsplive/files/my_fifo"
#define BUFFERSIZE   PIPE_BUF

CRTSPStream::CRTSPStream(void)
{

}

CRTSPStream::~CRTSPStream(void)
{

}

bool CRTSPStream::Init()
{
    if(access(FIFO_NAME,F_OK) == -1)
    {
        int res = mkfifo(FIFO_NAME,0666);
        if(res != 0)
        {
            printf("[RTSPStream] Create fifo failed.\n");
            return false;
        }
    }
    return true;
}


void CRTSPStream::Uninit()
{

}

bool CRTSPStream::SendH264File(const char *pFileName)
{
    if(pFileName == NULL)
    {
        return false;
    }
    FILE *fp = fopen(pFileName, "rb");
    if(!fp)
    {
        printf("[RTSPStream] error:open file %s failed!",pFileName);
    }
    fseek(fp, 0, SEEK_SET);

    unsigned char *buffer  = new unsigned char[FILEBUFSIZE];
    int pos = 0;
    while(1)
    {
        int readlen = fread(buffer+pos, sizeof(unsigned char), FILEBUFSIZE-pos, fp);

        if(readlen<=0)
        {
            break;
        }

        readlen+=pos;

        int writelen = SendH264Data(buffer,readlen);
        if(writelen<=0)
        {
            break;
        }
        memcpy(buffer,buffer+writelen,readlen-writelen);
        pos = readlen-writelen;

        mSleep(25);
    }
    fclose(fp);
    delete[] buffer;
    return true;
}

// 发送H264数据帧
int CRTSPStream::SendH264Data(const unsigned char *data,unsigned int size)
{
    if(data == NULL)
    {
        return 0;
    }
//    logBytes(size, data);

    // open pipe with non_block mode
    int pipe_fd = open(FIFO_NAME, O_WRONLY|O_NONBLOCK);
    LOGI("[RTSPStream] open fifo result = [%d]\n",pipe_fd);
    if(pipe_fd == -1)
    {
        return 0;
    }

    int send_size = 0;
    int remain_size = size;
    while(send_size < size)
    {
        int data_len = (remain_size<BUFFERSIZE) ? remain_size : BUFFERSIZE;
        int len = write(pipe_fd,data+send_size,data_len);
        if(len == -1)
        {
            static int resend_conut = 0;
            if(errno == EAGAIN && ++resend_conut<=3)
            {
                printf("[RTSPStream] write fifo error,resend..\n");
                continue;
            }
            resend_conut = 0;
            printf("[RTSPStream] write fifo error,errorcode[%d],send_size[%d]\n",errno,send_size);
            break;
        }
        else
        {
            send_size+= len;
            remain_size-= len;
        }
    }
    close(pipe_fd);
    //printf("[RTSPStream] SendH264Data datalen[%d], sendsize = [%d]\n",size,send_size);
    return 0;
}