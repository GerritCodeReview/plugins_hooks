// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.hooks;

import com.google.gerrit.metrics.Counter1;
import com.google.gerrit.metrics.Description;
import com.google.gerrit.metrics.Field;
import com.google.gerrit.metrics.MetricMaker;
import com.google.gerrit.metrics.Timer1;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HookMetrics {
  private final Timer1<String> latency;
  private final Counter1<String> count;
  private final Counter1<String> error;
  private final Counter1<String> timeout;

  @Inject
  HookMetrics(MetricMaker metricMaker) {
    Field<String> field = Field.ofString("hook");
    latency =
        metricMaker.newTimer(
            "latency",
            new Description("Time spent executing a hook")
                .setCumulative()
                .setUnit(Description.Units.MILLISECONDS),
            field);
    count = metricMaker.newCounter("count", new Description("Hook executions").setRate(), field);
    error =
        metricMaker.newCounter("error", new Description("Hook execution errors").setRate(), field);
    timeout =
        metricMaker.newCounter(
            "timeout", new Description("Hook execution timeouts").setRate(), field);
  }

  public Timer1.Context start(String name) {
    return latency.start(name);
  }

  public void count(String name) {
    count.increment(name);
  }

  public void error(String name) {
    error.increment(name);
  }

  public void timeout(String name) {
    timeout.increment(name);
  }
}
