/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mediatek.net.cts;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class ListeningPortsTest extends TestCase {

    /** Address patterns used to check whether we're checking the right column in /proc/net. */
    private static final List<String> ADDRESS_PATTERNS = new ArrayList<String>(2);

    static {
        ADDRESS_PATTERNS.add("[0-9A-F]{8}:[0-9A-F]{4}");
        ADDRESS_PATTERNS.add("[0-9A-F]{32}:[0-9A-F]{4}");
    }

    /** Ports that are allowed to be listening on the emulator. */
    private static final List<String> EXCEPTION_PATTERNS = new ArrayList<String>(6);

    static {
        // IPv4 exceptions
        EXCEPTION_PATTERNS.add("00000000:15B3"); // 0.0.0.0:5555   - emulator port
        EXCEPTION_PATTERNS.add("0F02000A:15B3"); // 10.0.2.15:5555 - net forwarding for emulator
        EXCEPTION_PATTERNS.add("[0-9A-F]{6}7F:[0-9A-F]{4}"); // IPv4 Loopback

        // IPv6 exceptions
        EXCEPTION_PATTERNS.add("[0]{25}1[0]{6}:[0-9A-F]{4}"); // IPv6 Loopback
        EXCEPTION_PATTERNS.add("[0]{16}[0]{4}[0]{4}[0-9A-F]{6}7F:[0-9A-F]{4}"); // IPv4-6 Conversion
        EXCEPTION_PATTERNS.add("[0]{16}[F]{4}[0]{4}[0-9A-F]{6}7F:[0-9A-F]{4}"); // IPv4-6 Conversion
    }

    public void testNoListeningTcpPorts() {
        assertNoListeningPorts("/proc/net/tcp", true);
    }

    public void testNoListeningTcp6Ports() {
        assertNoListeningPorts("/proc/net/tcp6", true);
    }

    public void testNoListeningUdpPorts() throws Exception {
        assertNoListeningUdpPorts("/proc/net/udp");
    }

    public void testNoListeningUdp6Ports() throws Exception {
        assertNoListeningUdpPorts("/proc/net/udp6");
    }

    private static final int RETRIES_MAX = 6;

    /**
     * UDP tests can be flaky due to DNS lookups.  Compensate.
     */
    private static void assertNoListeningUdpPorts(String procFilePath) throws Exception {
        for (int i = 0; i < RETRIES_MAX; i++) {
            try {
                assertNoListeningPorts(procFilePath, false);
                return;
            } catch (ListeningPortsAssertionError e) {
                if (i == RETRIES_MAX - 1) {
                    throw e;
                }
                Thread.sleep(2 * 1000 * i);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private static void assertNoListeningPorts(String procFilePath, boolean isTcp) {

        /*
         * Sample output of "cat /proc/net/tcp" on emulator:
         *
         * sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  ...
         * 0: 0100007F:13AD 00000000:0000 0A 00000000:00000000 00:00000000 00000000     0   ...
         * 1: 00000000:15B3 00000000:0000 0A 00000000:00000000 00:00000000 00000000     0   ...
         * 2: 0F02000A:15B3 0202000A:CE8A 01 00000000:00000000 00:00000000 00000000     0   ...
         *
         */

        File procFile = new File(procFilePath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(procFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // Skip column headers
                if (line.startsWith("sl")) {
                    continue;
                }

                String[] fields = line.split("\\s+");
                final int expectedNumColumns = 12;
                assertTrue(procFilePath + " should have at least " + expectedNumColumns
                        + " columns of output " + fields, fields.length >= expectedNumColumns);

                String localAddress = fields[1];
                String state = fields[3];
                String uid = fields[7];

                assertTrue(procFilePath + " should have an IP address in the second column",
                        isAddress(localAddress));

                String localIp = localAddress.split(":")[0];
                int localPort = Integer.parseInt(localAddress.split(":")[1], 16);

                if (!isException(localAddress) && isPortListening(state, isTcp)) {
                    throw new ListeningPortsAssertionError(
                            "Found port listening on addr=" + localIp + ", port="
                                + localPort + ", UID=" + uid + " in " + procFilePath);
                }
            }
        } catch (FileNotFoundException notFound) {
            fail("Could not open file " + procFilePath + " to check for listening ports.");
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static boolean isAddress(String localAddress) {
        return isPatternMatch(ADDRESS_PATTERNS, localAddress);
    }

    private static boolean isException(String localAddress) {
        return isPatternMatch(EXCEPTION_PATTERNS, localAddress);
    }

    private static boolean isPatternMatch(List<String> patterns, String input) {
        for (String pattern : patterns) {
            if (Pattern.matches(pattern, input)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPortListening(String state, boolean isTcp) {
        // 0A = TCP_LISTEN from include/net/tcp_states.h
        String listeningState = isTcp ? "0A" : "07";
        return listeningState.equals(state);
    }

    private static class ListeningPortsAssertionError extends AssertionFailedError {
        private ListeningPortsAssertionError(String msg) {
            super(msg);
        }
    }
}
