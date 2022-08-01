/*
 * Copyright 2020 Google LLC
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

package com.google.android.ground.persistence.remote.firestore.schema;

import static com.google.android.ground.util.ImmutableListCollector.toImmutableList;
import static java8.util.stream.StreamSupport.stream;

import com.google.android.ground.model.User;
import com.google.android.ground.model.locationofinterest.Point;
import com.google.android.ground.model.mutation.LocationOfInterestMutation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.firestore.GeoPoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import timber.log.Timber;

/**
 * Converts between Firestore maps used to merge updates and {@link LocationOfInterestMutation}
 * instances.
 */
class LoiMutationConverter {

  /**
   * Returns a map containing key-value pairs usable by Firestore constructed from the provided
   * mutation.
   */
  static ImmutableMap<String, Object> toMap(LocationOfInterestMutation mutation, User user) {
    ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
    map.put(LoiConverter.JOB_ID, mutation.getJobId());
    mutation
        .getLocation()
        .map(LoiMutationConverter::toGeoPoint)
        .ifPresent(point -> map.put(LoiConverter.LOCATION, point));
    Map<String, Object> geometry = new HashMap<>();
    geometry.put(LoiConverter.GEOMETRY_COORDINATES, toGeoPointList(mutation.getPolygonVertices()));
    geometry.put(LoiConverter.GEOMETRY_TYPE, LoiConverter.POLYGON_TYPE);
    map.put(LoiConverter.GEOMETRY, geometry);

    AuditInfoNestedObject auditInfo = AuditInfoConverter.fromMutationAndUser(mutation, user);
    switch (mutation.getType()) {
      case CREATE:
        map.put(LoiConverter.CREATED, auditInfo);
        map.put(LoiConverter.LAST_MODIFIED, auditInfo);
        break;
      case UPDATE:
        map.put(LoiConverter.LAST_MODIFIED, auditInfo);
        break;
      case DELETE:
      case UNKNOWN:
        // TODO.
        throw new UnsupportedOperationException();
      default:
        Timber.e("Unhandled state: %s", mutation.getType());
        break;
    }
    return map.build();
  }

  private static GeoPoint toGeoPoint(Point point) {
    return new GeoPoint(point.getLatitude(), point.getLongitude());
  }

  private static List<GeoPoint> toGeoPointList(ImmutableList<Point> point) {
    return stream(point).map(LoiMutationConverter::toGeoPoint).collect(toImmutableList());
  }
}