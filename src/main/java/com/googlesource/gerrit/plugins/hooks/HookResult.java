// Copyright (C) 2016 The Android Open Source Project
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

public class HookResult {
  private int exitValue = -1;
  private String output;
  private String executionError;

  HookResult(int exitValue, String output) {
    this.exitValue = exitValue;
    this.output = output;
  }

  HookResult(String output, String executionError) {
    this.output = output;
    this.executionError = executionError;
  }

  public int getExitValue() {
    return exitValue;
  }

  public void setExitValue(int exitValue) {
    this.exitValue = exitValue;
  }

  public String getOutput() {
    return output;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (output != null && output.length() != 0) {
      sb.append(output);

      if (executionError != null) {
        sb.append(" - ");
      }
    }

    if (executionError != null ) {
      sb.append(executionError);
    }

    return sb.toString();
  }
}
