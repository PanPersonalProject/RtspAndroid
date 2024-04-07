/**********
This library is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the
Free Software Foundation; either version 2.1 of the License, or (at your
option) any later version. (See <http://www.gnu.org/copyleft/lesser.html>.)
This library is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
more details.
You should have received a copy of the GNU Lesser General Public License
along with this library; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
**********/
// Copyright (c) 1996-2013, Live Networks, Inc.  All rights reserved
// LIVE555 Media Server
// main program

#include <BasicUsageEnvironment.hh>
#include "WW_H264VideoSource.h"
#include "WW_H264VideoServerMediaSubsession.h"
#include "announceURL.hh"

void startRtspServer2(const char *ip, int port) {
    // Begin by setting up our usage environment:
    TaskScheduler* scheduler = BasicTaskScheduler::createNew();
    UsageEnvironment* env = BasicUsageEnvironment::createNew(*scheduler);

    UserAuthenticationDatabase* authDB = NULL;
#ifdef ACCESS_CONTROL
    // To implement client access control to the RTSP server, do the following:
	authDB = new UserAuthenticationDatabase;
	authDB->addUserRecord("username1", "password1"); // replace these with real strings
	// Repeat the above with each <username>, <password> that you wish to allow
	// access to the server.
#endif

    // Create the RTSP server:
    RTSPServer* rtspServer = RTSPServer::createNew(*env, 8554, authDB);
    if (rtspServer == NULL) {
        *env << "Failed to create RTSP server: " << env->getResultMsg() << "\n";
        exit(1);
    }

    // Add live stream

    WW_H264VideoSource * videoSource = 0;

    ServerMediaSession * sms = ServerMediaSession::createNew(*env, "testStream", 0, "ww live test");
    sms->addSubsession(WW_H264VideoServerMediaSubsession::createNew(*env, videoSource));
    rtspServer->addServerMediaSession(sms);

    printRtspUrl(ip, port, "testStream");

    // Run loop
    env->taskScheduler().doEventLoop();

    rtspServer->removeServerMediaSession(sms);

    Medium::close(rtspServer);

    env->reclaim();

    delete scheduler;

}