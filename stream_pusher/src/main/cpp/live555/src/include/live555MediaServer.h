/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/4/5
 * Description: Add a brief description of the file here.
 ******************************************************************************************/

#ifndef FASTRTSPLIVE_LIVE555MEDIASERVER_H
#define FASTRTSPLIVE_LIVE555MEDIASERVER_H
#include <BasicUsageEnvironment.hh>
#include <jni.h>
#include "WW_H264VideoSource.h"
#include "WW_H264VideoServerMediaSubsession.h"

void startRtspServer2(const char *string, jint i);
#endif //FASTRTSPLIVE_LIVE555MEDIASERVER_H
