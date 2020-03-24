/*
 * Copyright (C) 2019 Qunar, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.pjmike.netty.sky;

import com.google.common.collect.ImmutableList;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HeartbeatProcessor implements Processor<String> {


    @Override
    public List<Integer> types() {
        return ImmutableList.of(ResponseCode.RESP_TYPE_HEARTBEAT.getCode());
    }

    @Override
    public void process(String header, String command) {
        log.info("receive heartbeat response, {}");

    }
}
