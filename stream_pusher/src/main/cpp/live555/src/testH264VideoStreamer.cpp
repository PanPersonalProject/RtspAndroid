/**********
This library is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the
Free Software Foundation; either version 3 of the License, or (at your
option) any later version. (See <http://www.gnu.org/copyleft/lesser.html>.)

This library is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
more details.

You should have received a copy of the GNU Lesser General Public License
along with this library; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
**********/
// Copyright (c) 1996-2024, Live Networks, Inc.  All rights reserved
// A test program that reads a H.264 Elementary Stream video file
// and streams it using RTP
// main program
//
// NOTE: For this application to work, the H.264 Elementary Stream video file *must* contain SPS and PPS NAL units,
// ideally at or near the start of the file.  These SPS and PPS NAL units are used to specify 'configuration' information
// that is set in the output stream's SDP description (by the RTSP server that is built in to this application).
// Note also that - unlike some other "*Streamer" demo applications - the resulting stream can be received only using a
// RTSP client (such as "openRTSP")

#include <liveMedia.hh>

#include <BasicUsageEnvironment.hh>
#include "include/announceURL.hh"
#include "../../Base/include/AndroidLog.h"
#include "CameraSource.h"
#include <GroupsockHelper.hh>

UsageEnvironment* env;
char const* inputFileName = "test.264";
H264VideoStreamFramer* videoSource;
RTPSink* videoSink;

void play(); // forward


void startRtspServer(const char *ip, int port) {
    // Begin by setting up our usage environment:
    TaskScheduler* scheduler = BasicTaskScheduler::createNew();
    env = BasicUsageEnvironment::createNew(*scheduler);

    // Create 'groupsocks' for RTP and RTCP:
    struct sockaddr_storage destinationAddress;
    destinationAddress.ss_family = AF_INET;
    ((struct sockaddr_in&)destinationAddress).sin_addr.s_addr = chooseRandomIPv4SSMAddress(*env);
    // Note: This is a multicast address.  If you wish instead to stream
    // using unicast, then you should use the "testOnDemandRTSPServer"
    // test program - not this test program - as a model.

    const unsigned short rtpPortNum = 18888;
    const unsigned short rtcpPortNum = rtpPortNum+1;
    const unsigned char ttl = 255;

    const Port rtpPort(rtpPortNum);
    const Port rtcpPort(rtcpPortNum);

    Groupsock rtpGroupsock(*env, destinationAddress, rtpPort, ttl);
    rtpGroupsock.multicastSendOnly(); // we're a SSM source
    Groupsock rtcpGroupsock(*env, destinationAddress, rtcpPort, ttl);
    rtcpGroupsock.multicastSendOnly(); // we're a SSM source

    // Create a 'H264 Video RTP' sink from the RTP 'groupsock':
    OutPacketBuffer::maxSize = 100000;
    videoSink = H264VideoRTPSink::createNew(*env, &rtpGroupsock, 96);

    // Create (and start) a 'RTCP instance' for this RTP sink:
    const unsigned estimatedSessionBandwidth = 500; // in kbps; for RTCP b/w share
    const unsigned maxCNAMElen = 100;
    unsigned char CNAME[maxCNAMElen+1];
    gethostname((char*)CNAME, maxCNAMElen);
    CNAME[maxCNAMElen] = '\0'; // just in case
    RTCPInstance* rtcp
            = RTCPInstance::createNew(*env, &rtcpGroupsock,
                                      estimatedSessionBandwidth, CNAME,
                                      videoSink, NULL /* we're a server */,
                                      True /* we're a SSM source */);
    // Note: This starts RTCP running automatically

    RTSPServer* rtspServer = RTSPServer::createNew(*env, port);
    if (rtspServer == NULL) {
        LOGE("Failed to create RTSP server: %s", env->getResultMsg());
        return;
    }
    ServerMediaSession* sms
            = ServerMediaSession::createNew(*env, "testStream", inputFileName,
                                            "Session streamed by \"testH264VideoStreamer\"",
                                            True /*SSM*/);
    sms->addSubsession(PassiveServerMediaSubsession::createNew(*videoSink, rtcp));
    rtspServer->addServerMediaSession(sms);
    printRtspUrl(ip, port, "testStream");
    LOGI("Beginning streaming...\n");
    play();

    env->taskScheduler().doEventLoop(); // does not return

}

void afterPlaying(void* /*clientData*/) {
    LOGI("...done reading from file\n");
    videoSink->stopPlaying();
    Medium::close(videoSource);
    // Note that this also closes the input file that this source read from.

    // Start playing once again:
    play();
}

void play() {
    CameraSource* devSource
            = CameraSource::createNew(*env);
    if (devSource == NULL)
    {

        LOGE("Unable to open source");
        exit(1);
    }

    FramedSource* videoES = devSource;

    // Create a framer for the Video Elementary Stream:
    videoSource = H264VideoStreamFramer::createNew(*env, videoES);

    // Finally, start playing:
    LOGV("Beginning to read from camera...");
    videoSink->startPlaying(*videoSource, afterPlaying, videoSink);
}
