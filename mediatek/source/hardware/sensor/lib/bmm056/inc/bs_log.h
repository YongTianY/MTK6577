/*
 * =====================================================================================
 * Copyright (C) 2011 Bosch Sensortec GmbH
 *
 *       Filename:  bs_log.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  05/05/2011 03:55:41 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Zhengguang.Guo@bosch-sensortec.com
 *        Company:  
 *
 * =====================================================================================
 */


/*************************************************************************************************/
/*  Disclaimer
*
* Common:
* Bosch Sensortec products are developed for the consumer goods industry. They may only be used
* within the parameters of the respective valid product data sheet.  Bosch Sensortec products are
* provided with the express understanding that there is no warranty of fitness for a particular purpose.
* They are not fit for use in life-sustaining, safety or security sensitive systems or any system or device
* that may lead to bodily harm or property damage if the system or device malfunctions. In addition,
* Bosch Sensortec products are not fit for use in products which interact with motor vehicle systems.
* The resale and/or use of products are at the purchasers own risk and his own responsibility. The
* examination of fitness for the intended use is the sole responsibility of the Purchaser.
*
* The purchaser shall indemnify Bosch Sensortec from all third party claims, including any claims for
* incidental, or consequential damages, arising from any product use not covered by the parameters of
* the respective valid product data sheet or not approved by Bosch Sensortec and reimburse Bosch
* Sensortec for all costs in connection with such claims.
*
* The purchaser must monitor the market for the purchased products, particularly with regard to
* product safety and inform Bosch Sensortec without delay of all security relevant incidents.
*
* Engineering Samples are marked with an asterisk (*) or (e). Samples may vary from the valid
* technical specifications of the product series. They are therefore not intended or fit for resale to third
* parties or for use in end products. Their sole purpose is internal client testing. The testing of an
* engineering sample may in no way replace the testing of a product series. Bosch Sensortec
* assumes no liability for the use of engineering samples. By accepting the engineering samples, the
* Purchaser agrees to indemnify Bosch Sensortec from all claims arising from the use of engineering
* samples.
*
* Special:
* This software module (hereinafter called "Software") and any information on application-sheets
* (hereinafter called "Information") is provided free of charge for the sole purpose to support your
* application work. The Software and Information is subject to the following terms and conditions:
*
* The Software is specifically designed for the exclusive use for Bosch Sensortec products by
* personnel who have special experience and training. Do not use this Software if you do not have the
* proper experience or training.
*
* This Software package is provided `` as is `` and without any expressed or implied warranties,
* including without limitation, the implied warranties of merchantability and fitness for a particular
* purpose.
*
* Bosch Sensortec and their representatives and agents deny any liability for the functional impairment
* of this Software in terms of fitness, performance and safety. Bosch Sensortec and their
* representatives and agents shall not be liable for any direct or indirect damages or injury, except as
* otherwise stipulated in mandatory applicable law.
*
* The Information provided is believed to be accurate and reliable. Bosch Sensortec assumes no
* responsibility for the consequences of use of such Information nor for any infringement of patents or
* other rights of third parties which may result from its use. No license is granted by implication or
* otherwise under any patent or patent rights of Bosch. Specifications mentioned in the Information are
* subject to change without notice.
*
* It is not allowed to deliver the source code of the Software to any third party without permission of
* Bosch Sensortec.
*/


#ifndef __BS_LOG_H
#define __BS_LOG_H

#include <stdio.h>
#include <string.h>
#include <errno.h>

#ifdef CFG_LOG_WITH_TIME
#include "util_misc.h"
#define GET_TIME_TICK() (get_time_tick())
#else
#define GET_TIME_TICK() (0)
#endif

#define LOG_LEVEL_Q 0	/* quiet */
#define LOG_LEVEL_E 3
#define LOG_LEVEL_W 4
#define LOG_LEVEL_N 5
#define LOG_LEVEL_I 6
#define LOG_LEVEL_D 7

#define CFG_LOG_LEVEL LOG_LEVEL_D

#ifndef CFG_LOG_LEVEL
#define CFG_LOG_LEVEL LOG_LEVEL_I
#endif

#ifndef LOG_TAG_MODULE
#define LOG_TAG_MODULE
#endif

#ifndef CFG_TARGET_OS_ANDROID
#ifdef CFG_LOG_TO_LOGCAT
#error "Unsupported option!"
#endif
#endif

#if (defined CFG_LOG_TO_LOGCAT && defined CFG_LOG_TO_STDOUT) ||\
	(defined CFG_LOG_TO_LOGCAT && defined CFG_LOG_TO_FILE) || \
	(defined CFG_LOG_TO_FILE && defined CFG_LOG_TO_STDOUT)
#error "Conflicting options!"
#endif

#define CFG_LOG_TO_LOGCAT

#ifdef CFG_LOG_TO_LOGCAT

#include <cutils/log.h>
#define BS_LOG LOGD

#else

#include <stdio.h>
#define BS_LOG printf

#endif


#if (CFG_LOG_LEVEL >= LOG_LEVEL_E)
#define PERR(fmt, args...) BS_LOG("\n" "[%u]" "[E]" "BS_ERR" LOG_TAG_MODULE "<%s><%d>" fmt "<reason> %s" "\n", GET_TIME_TICK(), __FUNCTION__, __LINE__, ##args, (char *)strerror(errno))
#else
#define PERR(fmt, args...)
#endif 

#if (CFG_LOG_LEVEL >= LOG_LEVEL_W)
#define PWARN(fmt, args...) BS_LOG("\n" "[%u]" "[W]" "BS_WARN" LOG_TAG_MODULE "<%s><%d>" fmt "\n", GET_TIME_TICK(), __FUNCTION__, __LINE__, ##args)
#else
#define PWARN(fmt, args...)
#endif


#if (CFG_LOG_LEVEL >= LOG_LEVEL_N)
#define PNOTICE(fmt, args...) BS_LOG("\n" "[%u]" "[N]" "BS_NOTICE" LOG_TAG_MODULE "<%s><%d>" fmt "\n", GET_TIME_TICK(), __FUNCTION__, __LINE__, ##args)
#else
#define PNOTICE(fmt, args...)
#endif

#if (CFG_LOG_LEVEL >= LOG_LEVEL_I)
#define PINFO(fmt, args...) BS_LOG("\n" "[%u]" "[I]" "BS_INFO" LOG_TAG_MODULE "<%s><%d>" fmt "\n", GET_TIME_TICK(), __FUNCTION__, __LINE__, ##args)
#else
#define PINFO(fmt, args...)
#endif

#if (CFG_LOG_LEVEL >= LOG_LEVEL_D)
#define PDEBUG(fmt, args...) BS_LOG("\n" "[%u]" "[D]" "BS_DBG" LOG_TAG_MODULE "<%s><%d>" fmt "\n", GET_TIME_TICK(), __FUNCTION__, __LINE__, ##args)
#else
#define PDEBUG(fmt, args...)
#endif

#endif
