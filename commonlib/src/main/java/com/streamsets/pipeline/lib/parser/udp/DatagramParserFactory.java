/**
 * Copyright 2016 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.lib.parser.udp;

import com.google.common.collect.ImmutableSet;
import com.streamsets.pipeline.api.impl.Utils;
import com.streamsets.pipeline.config.DatagramMode;
import com.streamsets.pipeline.lib.parser.DataParser;
import com.streamsets.pipeline.lib.parser.DataParserException;
import com.streamsets.pipeline.lib.parser.DataParserFactory;
import com.streamsets.pipeline.lib.parser.udp.collectd.CollectdParser;
import com.streamsets.pipeline.lib.parser.udp.netflow.NetflowParser;
import com.streamsets.pipeline.lib.parser.udp.syslog.SyslogParser;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DatagramParserFactory extends DataParserFactory {

  private static final String KEY_PREFIX = "collectd.";
  static final String CONVERT_TIME_KEY = KEY_PREFIX + "convertTime";
  static final boolean CONVERT_TIME_DEFAULT = false;
  static final String AUTH_FILE_PATH_KEY = KEY_PREFIX + "authFilePath";
  static final String TYPES_DB_PATH_KEY = KEY_PREFIX + "typesDbPath";
  static final String EXCLUDE_INTERVAL_KEY = KEY_PREFIX + "excludeInterval";
  static final boolean EXCLUDE_INTERVAL_DEFAULT = true;

  public static final Map<String, Object> CONFIGS;
  public static final Set<Class<? extends Enum>> MODES = (Set) ImmutableSet.of(DatagramMode.class);

  static {
    Map<String, Object> configs = new HashMap<>();
    configs.put(CONVERT_TIME_KEY, CONVERT_TIME_DEFAULT);
    configs.put(AUTH_FILE_PATH_KEY, null);
    configs.put(TYPES_DB_PATH_KEY, null);
    configs.put(EXCLUDE_INTERVAL_KEY, EXCLUDE_INTERVAL_DEFAULT);
    CONFIGS = Collections.unmodifiableMap(configs);
  }

  private final DatagramMode datagramMode;

  public DatagramParserFactory(Settings settings) {
    super(settings);
    datagramMode = settings.getMode(DatagramMode.class);
  }

  @Override
  public DataParser getParser(String id, InputStream is, String offset) throws DataParserException {
    return new DatagramParser(is, datagramMode, getDatagramParser(datagramMode, getSettings()));
  }

  private AbstractParser getDatagramParser(DatagramMode datagramMode, Settings settings) {
    AbstractParser parser;
    switch (datagramMode) {
      case SYSLOG:
        parser = new SyslogParser(settings.getContext(), settings.getCharset());
        break;
      case NETFLOW:
        parser = new NetflowParser(settings.getContext());
        break;
      case COLLECTD:
        parser = new CollectdParser(
            settings.getContext(),
            (Boolean) settings.getConfig(CONVERT_TIME_KEY),
            (String) settings.getConfig(TYPES_DB_PATH_KEY),
            (Boolean) settings.getConfig(EXCLUDE_INTERVAL_KEY),
            (String) settings.getConfig(AUTH_FILE_PATH_KEY),
            settings.getCharset()
        );
        break;
      default:
        throw new IllegalStateException(Utils.format("Unexpected UDP Message type {}", datagramMode.name()));
    }
    return parser;
  }

  @Override
  public DataParser getParser(String id, Reader reader, long offset) throws DataParserException {
    throw new UnsupportedOperationException();
  }

}