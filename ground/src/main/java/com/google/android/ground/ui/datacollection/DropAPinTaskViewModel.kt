/*
 * Copyright 2023 Google LLC
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
package com.google.android.ground.ui.datacollection

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.google.android.ground.model.geometry.Point
import com.google.android.ground.model.submission.LocationTaskData
import com.google.android.ground.persistence.uuid.OfflineUuidGenerator
import com.google.android.ground.rx.annotations.Hot
import com.google.android.ground.ui.editsubmission.AbstractTaskViewModel
import com.google.android.ground.ui.map.CameraPosition
import com.google.android.ground.ui.map.Feature
import com.google.android.ground.ui.map.FeatureType
import java8.util.Optional
import javax.inject.Inject

class DropAPinTaskViewModel
@Inject
constructor(resources: Resources, private val uuidGenerator: OfflineUuidGenerator) :
  AbstractTaskViewModel(resources) {

  val features: @Hot MutableLiveData<Set<Feature>> = MutableLiveData()

  fun updateResponse(position: CameraPosition) {
    setResponse(Optional.of(LocationTaskData(position)))
    features.postValue(setOf(createFeature(Point(position.target))))
  }

  /** Creates a new map [Feature] representing the point placed by the user. */
  private fun createFeature(point: Point): Feature =
    Feature(
      id = uuidGenerator.generateUuid(),
      type = FeatureType.USER_POINT.ordinal,
      geometry = point
    )
}
