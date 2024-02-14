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
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
**********/
// Copyright (c) 1996-2024, Live Networks, Inc.  All rights reserved
// A common function that outputs the URL(s) that can be used to access a stream
// served by a RTSP server.
// Implementation

#include "include/announceURL.hh"
#include "../../Base/include/AndroidLog.h"
#include <GroupsockHelper.hh> // for "weHaveAnIPv*Address()"

void announceURL(RTSPServer *rtspServer, ServerMediaSession *sms) {
    if (rtspServer == NULL || sms == NULL) return; // sanity check

    UsageEnvironment &env = rtspServer->envir();

    LOGI("Play this stream using the URL ");
    if (weHaveAnIPv4Address(env)) {
        char *url = rtspServer->ipv4rtspURL(sms);
        LOGI("\"%s\"", url);
        delete[] url;
        if (weHaveAnIPv6Address(env)) LOGI(" or ");
    }else{
        LOGI(" not weHaveAnIPv4Address ");
    }

    if (weHaveAnIPv6Address(env)) {
        char *url = rtspServer->ipv6rtspURL(sms);
        LOGI("\"%s\"", url);
        delete[] url;
    }else{
        LOGI(" not weHaveAnIPv6Address ");
    }


    char *url = rtspServer->ipv4rtspURL(sms);
    LOGI("\"%s\"", url);
    delete[] url;
    if (weHaveAnIPv6Address(env)) LOGI(" or ");
    LOGI("\n");
}