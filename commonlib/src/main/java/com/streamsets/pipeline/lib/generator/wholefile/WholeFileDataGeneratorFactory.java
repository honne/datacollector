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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.lib.generator.wholefile;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.streamsets.pipeline.lib.generator.DataGenerator;
import com.streamsets.pipeline.lib.generator.DataGeneratorFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public class WholeFileDataGeneratorFactory extends DataGeneratorFactory {
  public static final Map<String, Object> CONFIGS = ImmutableMap.of();
  public static final Set<Class<? extends Enum>> MODES = ImmutableSet.of();

  public WholeFileDataGeneratorFactory(Settings settings) {
    super(settings);
  }

  @Override
  public DataGenerator getGenerator(OutputStream os) throws IOException {
    return new WholeFileDataGenerator(getSettings().getContext(), os);
  }
}
