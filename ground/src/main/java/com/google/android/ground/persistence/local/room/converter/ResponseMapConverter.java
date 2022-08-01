/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.ground.persistence.local.room.converter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.ground.model.job.Job;
import com.google.android.ground.model.submission.ResponseMap;
import com.google.android.ground.model.task.Task;
import com.google.android.ground.persistence.local.LocalDataConsistencyException;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

/** Converts between {@link ResponseMap} and JSON strings used to represent them in the local db. */
public class ResponseMapConverter {

  @Nullable
  public static String toString(@NonNull ResponseMap responseDeltas) {
    JSONObject json = new JSONObject();
    for (String taskId : responseDeltas.taskIds()) {
      try {
        json.put(
            taskId,
            responseDeltas
                .getResponse(taskId)
                .map(ResponseJsonConverter::toJsonObject)
                .orElse(null));
      } catch (JSONException e) {
        Timber.e(e, "Error building JSON");
      }
    }
    return json.toString();
  }

  @NonNull
  public static ResponseMap fromString(Job job, @Nullable String jsonString) {
    ResponseMap.Builder map = ResponseMap.builder();
    if (jsonString == null) {
      return map.build();
    }
    try {
      JSONObject jsonObject = new JSONObject(jsonString);
      Iterator<String> keys = jsonObject.keys();
      while (keys.hasNext()) {
        try {
          String taskId = keys.next();
          Task task =
              job.getTask(taskId)
                  .orElseThrow(
                      () -> new LocalDataConsistencyException("Unknown task id " + taskId));
          ResponseJsonConverter.toResponse(task, jsonObject.get(taskId))
              .ifPresent(response -> map.putResponse(taskId, response));
        } catch (LocalDataConsistencyException e) {
          Timber.d("Bad response in local db: " + e.getMessage());
        }
      }
    } catch (JSONException e) {
      Timber.e(e, "Error parsing JSON string");
    }
    return map.build();
  }
}